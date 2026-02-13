package com.isa.jutjubic.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.isa.jutjubic.dto.UploadEventDto;
import com.isa.jutjubic.mq.UploadEventProtoMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ComparisonService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Čuvamo: [0]=sizeBytes, [1]=deserNanos
    private final List<long[]> jsonStats = new ArrayList<>();
    private final List<long[]> protoStats = new ArrayList<>();

    @RabbitListener(queues = "video.json.queue")
    public void receiveJson(byte[] data) {
        try {
            long start = System.nanoTime();
            UploadEventDto event = objectMapper.readValue(data, UploadEventDto.class);
            long deserTime = System.nanoTime() - start;

            synchronized (jsonStats) {
                jsonStats.add(new long[]{data.length, deserTime});
                System.out.printf("[JSON RECV] videoId=%d, size=%d B, deserTime=%d µs%n",
                        event.getId(), data.length, deserTime / 1000);
            }

            checkAndReport();

        } catch (Exception e) {
            System.err.println("JSON deser error: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "video.proto.queue")
    public void receiveProto(byte[] data) {
        try {
            long start = System.nanoTime();
            UploadEventProtoMessage.UploadEventProto event =
                    UploadEventProtoMessage.UploadEventProto.parseFrom(data);
            long deserTime = System.nanoTime() - start;

            synchronized (protoStats) {
                protoStats.add(new long[]{data.length, deserTime});
                System.out.printf("[PROTO RECV] videoId=%d, size=%d B, deserTime=%d µs%n",
                        event.getId(), data.length, deserTime / 1000);
            }

            checkAndReport();

        } catch (Exception e) {
            System.err.println("Proto deser error: " + e.getMessage());
        }
    }

    private synchronized void checkAndReport() {
        int jsonCount = jsonStats.size();
        int protoCount = protoStats.size();

        // Ispiši izveštaj kad imamo bar 50 od oba formata
        if (jsonCount >= 50 && protoCount >= 50
                && (jsonCount == 50 || protoCount == 50
                || jsonCount % 10 == 0 || protoCount % 10 == 0)) {

            int n = Math.min(jsonCount, protoCount);
            printReport(n);
        }
    }

    private void printReport(int n) {
        double avgJsonSize    = jsonStats.stream().mapToLong(a -> a[0]).average().orElse(0);
        double avgJsonDeser   = jsonStats.stream().mapToLong(a -> a[1]).average().orElse(0) / 1000.0;
        double avgProtoSize   = protoStats.stream().mapToLong(a -> a[0]).average().orElse(0);
        double avgProtoDeser  = protoStats.stream().mapToLong(a -> a[1]).average().orElse(0) / 1000.0;

        double sizeDiff = ((avgProtoSize / avgJsonSize) - 1) * 100;
        double deserDiff = ((avgProtoDeser / avgJsonDeser) - 1) * 100;

        System.out.println("\n========================================");
        System.out.println("   REZULTAT POREĐENJA (" + n + " poruka)");
        System.out.println("========================================");
        System.out.printf("%-12s | %14s | %16s%n", "Format", "Avg Size (B)", "Avg Deser (µs)");
        System.out.println("-------------|----------------|------------------");
        System.out.printf("%-12s | %14.1f | %16.2f%n", "JSON", avgJsonSize, avgJsonDeser);
        System.out.printf("%-12s | %14.1f | %16.2f%n", "Protobuf", avgProtoSize, avgProtoDeser);
        System.out.println("---------------------------------------------------");
        System.out.printf("Protobuf je %+.1f%% veličinom od JSON-a%n", sizeDiff);
        System.out.printf("Protobuf je %+.1f%% brzinom deserijalizacije od JSON-a%n", deserDiff);
        System.out.println("(negativno = Protobuf bolji, pozitivno = JSON bolji)");
        System.out.println("========================================\n");
    }
}
