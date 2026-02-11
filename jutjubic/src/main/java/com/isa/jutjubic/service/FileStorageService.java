package com.isa.jutjubic.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;


@Service
public class FileStorageService {

      private final Path rootUploadDir = Paths.get(System.getProperty("user.dir"), "uploads");

    @Autowired
    private CacheManager cacheManager;

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private static final int THUMBNAIL_MAX_AGE_DAYS = 30;
    private static final float THUMBNAIL_QUALITY = 0.6f;
    private static final String THUMBNAILS_DIR = "thumbnails";
    private static final String THUMBNAILS_COMPRESSED_DIR = "thumbnails-compressed";

    public String saveFile(MultipartFile file, String subFolder) throws IOException {
        //  simulacija sporog uploada - samo za testiranje
       /* try {
            System.out.println("Simulating slow upload for: " + file.getOriginalFilename());
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted");
        }  */

        Path folderPath = rootUploadDir.resolve(subFolder);

        // kreiraj folder ako ne postoji
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        Path filePath = folderPath.resolve(file.getOriginalFilename());
        file.transferTo(filePath.toFile());

        //return filePath.toString();
        //vrati relativni path za cuvanje u bazi
        return rootUploadDir.getFileName().resolve(subFolder).resolve(file.getOriginalFilename()).toString();
    }


    @Cacheable(value = "thumbnails", key = "#path")
    public byte[] loadThumbnail(String path) {
        try {
            System.out.println("--- IZVRŠAVAM ČITANJE SA DISKA ZA: " + path);
            return Files.readAllBytes(Paths.get(System.getProperty("user.dir")).resolve(path));
        } catch (IOException e) {
            throw new RuntimeException("Neuspešno čitanje fajla: " + path, e);
        }
    }

    @CacheEvict(value = "thumbnails", key = "#path")
    public void deleteFile(String path) throws IOException {
        //Path filePath = Paths.get(path);
        Path filePath = Paths.get(System.getProperty("user.dir")).resolve(path);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void compressOldThumbnails() {
        Path thumbnailsDir = rootUploadDir.resolve(THUMBNAILS_DIR);
        if (!Files.exists(thumbnailsDir)) {
            return;
        }

        Path compressedDir = rootUploadDir.resolve(THUMBNAILS_COMPRESSED_DIR);
        try {
            Files.createDirectories(compressedDir);
        } catch (IOException e) {
            logger.warn("Failed to create compressed thumbnails directory: {}", compressedDir, e);
            return;
        }

        Instant cutoff = Instant.now().minus(THUMBNAIL_MAX_AGE_DAYS, ChronoUnit.DAYS);

        try (Stream<Path> stream = Files.list(thumbnailsDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isImageFile(path.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            FileTime lastModified = Files.getLastModifiedTime(path);
                            if (lastModified.toInstant().isAfter(cutoff)) {
                                return;
                            }

                            Path compressedPath = compressedDir.resolve(path.getFileName().toString());
                            if (Files.exists(compressedPath)) {
                                return;
                            }

                            Thumbnails.of(path.toFile())
                                    .scale(1.0)
                                    .outputQuality(THUMBNAIL_QUALITY)
                                    .toFile(compressedPath.toFile());
                        } catch (IOException e) {
                            logger.warn("Failed to compress thumbnail: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            logger.warn("Failed to scan thumbnails directory: {}", thumbnailsDir, e);
        }
    }

    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }

}
