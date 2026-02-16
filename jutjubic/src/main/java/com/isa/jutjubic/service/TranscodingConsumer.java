package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.TranscodeRequestDto;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class TranscodingConsumer {

    private final VideoPostRepository videoPostRepository;

    @Value("${transcoding.ffmpeg-path}")
    private String ffmpegPath;

    public TranscodingConsumer(VideoPostRepository videoPostRepository) {
        this.videoPostRepository = videoPostRepository;
    }

    @RabbitListener(queues = "${transcoding.queue}", id = "transcodeConsumerA")
    public void consumeA(TranscodeRequestDto request,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        handle(request, channel, tag, "A");
    }

    @RabbitListener(queues = "${transcoding.queue}", id = "transcodeConsumerB")
    public void consumeB(TranscodeRequestDto request,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        handle(request, channel, tag, "B");
    }

    private void handle(TranscodeRequestDto request, Channel channel, long tag, String consumerId) throws Exception {
        Integer videoId = request.getVideoPostId();
        System.out.println("Consuming videoId=" + request.getVideoPostId());
        int updated = videoPostRepository.updateStatusIfCurrent(
                videoId,
                VideoPost.VideoStatus.PENDING,
                VideoPost.VideoStatus.PROCESSING

        );
        if (updated == 0) {
            System.out.println("videoId=" + videoId + " already claimed by another consumer");
            return;
        }

        //System.out.println("updateStatusIfCurrent videoId=" + videoId + " updated=" + updated);

//        if (updated == 0) {
//            return;
//        }
       // System.out.println("Consuming videoId=" + videoId + " updated=" + updated);

        try {
            Path input = Paths.get(System.getProperty("user.dir")).resolve(request.getInputPath()).normalize();
            Path output = Paths.get(System.getProperty("user.dir")).resolve(request.getOutputPath()).normalize();
            Files.createDirectories(output.getParent());

            if (!Files.exists(input)) {
                videoPostRepository.updateStatus(videoId, VideoPost.VideoStatus.FAILED);
                return;
            }

            List<String> cmd = new ArrayList<>();
            cmd.add(ffmpegPath);
            cmd.add("-y");
            cmd.add("-i");
            cmd.add(input.toString());
            cmd.addAll(request.getFfmpegArgs());
            cmd.add(output.toString());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) { }
            }

            int exit = process.waitFor();
            if (exit == 0) {
                videoPostRepository.updateVideoPath(videoId, request.getOutputPath());
                videoPostRepository.updateStatus(videoId, VideoPost.VideoStatus.READY);
            } else {
                videoPostRepository.updateStatus(videoId, VideoPost.VideoStatus.FAILED);
            }
        } catch (Exception ex) {
            videoPostRepository.updateStatus(videoId, VideoPost.VideoStatus.FAILED);
        }
    }
}