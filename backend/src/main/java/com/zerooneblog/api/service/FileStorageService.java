package com.zerooneblog.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation =Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        }catch(Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored", ex);
        }
    }
    public String storeFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new RuntimeException("Could not store file. The file must have a name.");
        }
        String cleanedFileName = StringUtils.cleanPath(originalFileName);
        try {
            if (cleanedFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + cleanedFileName);
            }
            String fileExtension = "";
            try {
                fileExtension = cleanedFileName.substring(cleanedFileName.lastIndexOf("."));
            }catch (Exception e) {
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        }catch (IOException ex) {
            throw new RuntimeException("Could not store file " + cleanedFileName + ". Please try again!", ex);
        }
    }
    public void deleteFile(String fileName) {
    try {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        Files.deleteIfExists(filePath);
    } catch (IOException ex) {
        System.err.println("Could not delete file " + fileName + ": " + ex.getMessage());
    }
}
}
