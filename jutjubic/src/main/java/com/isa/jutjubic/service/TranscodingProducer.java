package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.TranscodeRequestDto;
import org.slf4j.Logger;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TranscodingProducer {

    private final RabbitTemplate rabbitTemplate;
    private final TranscodingPresetRegistry presetRegistry;

    @Value("${transcoding.exchange}")
    private String exchange;

    @Value("${transcoding.routing-key}")
    private String routingKey;

    public TranscodingProducer(RabbitTemplate rabbitTemplate, TranscodingPresetRegistry presetRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        this.presetRegistry = presetRegistry;
    }

    public void enqueue(Integer videoPostId, String inputPath, String outputPath) {
        System.out.println("Enqueue videoId=" + videoPostId + " input=" + inputPath + " output=" + outputPath);
        TranscodingPreset preset = presetRegistry.getDefaultPreset();

        TranscodeRequestDto request = new TranscodeRequestDto();
        request.setMessageId(UUID.randomUUID().toString());
        request.setVideoPostId(videoPostId);
        request.setInputPath(inputPath);
        request.setOutputPath(outputPath);
        request.setPresetName(preset.name());
        request.setFfmpegArgs(preset.ffmpegArgs());

        rabbitTemplate.convertAndSend(exchange, routingKey, request, message -> {
            message.getMessageProperties().setMessageId(request.getMessageId());
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
    }
}