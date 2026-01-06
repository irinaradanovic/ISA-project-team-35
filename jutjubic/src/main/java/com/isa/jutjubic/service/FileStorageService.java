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


@Service
public class FileStorageService {

      private final Path rootUploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
    //private final Path rootUploadDir = Paths.get("uploads"); // relativno od root-a projekta

    @Autowired
    private CacheManager cacheManager;

    public String saveFile(MultipartFile file, String subFolder) throws IOException {
        Path folderPath = rootUploadDir.resolve(subFolder);

        // kreiraj folder ako ne postoji
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        Path filePath = folderPath.resolve(file.getOriginalFilename());
        file.transferTo(filePath.toFile());

        //return filePath.toString();
        //vrati relativni path za čuvanje u bazi
        return rootUploadDir.getFileName().resolve(subFolder).resolve(file.getOriginalFilename()).toString();
    }

    @Cacheable(value = "thumbnails", key = "#path")
    public byte[] loadThumbnail(String path) {
        try {
            System.out.println("Reading thumbnail from disk: " + path); // vidi da li se kešira
            //return Files.readAllBytes(Paths.get(path));
            return Files.readAllBytes(Paths.get(System.getProperty("user.dir")).resolve(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
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



}
