package com.zerooneblog.api.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    

    // Whitelist
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(
        Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp")
    );
    
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = new HashSet<>(
        Arrays.asList(".mp4", ".webm", ".mov", ".avi")
    );
    
    private static final Set<String> ALLOWED_IMAGE_MIME_TYPES = new HashSet<>(
        Arrays.asList(
            "image/jpeg", 
            "image/png", 
            "image/gif", 
            "image/webp"
        )
    );
    
    private static final Set<String> ALLOWED_VIDEO_MIME_TYPES = new HashSet<>(
        Arrays.asList(
            "video/mp4", 
            "video/webm", 
            "video/quicktime",
            "video/x-msvideo"
        )
    );
    
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB
    
    private final Path fileStorageLocation;

    public FileStorageService(@Value("${upload.dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }
        
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("File must have a valid name");
        }
        
        String cleaned = StringUtils.cleanPath(originalFileName);
        
        if (cleaned.contains("..") || cleaned.contains("/") || cleaned.contains("\\")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence: " + cleaned);
        }
        
        String fileExtension = extractFileExtension(cleaned);
        if (fileExtension.isEmpty()) {
            throw new IllegalArgumentException("File must have a valid extension");
        }
        
        String contentType = file.getContentType();
        validateFileType(fileExtension, contentType, file.getSize());
        
        try {
            String uniqueFileName = generateUniqueFileName(fileExtension);
            
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            
            if (!targetLocation.normalize().startsWith(this.fileStorageLocation)) {
                throw new RuntimeException("Cannot store file outside designated directory");
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            
            return uniqueFileName;
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + cleaned + ". Please try again!", ex);
        }
    }


    private void validateFileType(String extension, String mimeType, long fileSize) {
        String lowerExtension = extension.toLowerCase();
        
        // Check if it's an allowed image
        if (ALLOWED_IMAGE_EXTENSIONS.contains(lowerExtension)) {
            if (mimeType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(mimeType.toLowerCase())) {
                throw new IllegalArgumentException(
                    "Invalid image file. Extension is " + extension + " but MIME type is " + mimeType
                );
            }
            if (fileSize > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException(
                    "Image file size exceeds maximum allowed size of " + (MAX_IMAGE_SIZE / 1024 / 1024) + "MB"
                );
            }
            return;
        }
        
        // Check if it's an allowed video
        if (ALLOWED_VIDEO_EXTENSIONS.contains(lowerExtension)) {
            if (mimeType == null || !ALLOWED_VIDEO_MIME_TYPES.contains(mimeType.toLowerCase())) {
                throw new IllegalArgumentException(
                    "Invalid video file. Extension is " + extension + " but MIME type is " + mimeType
                );
            }
            if (fileSize > MAX_VIDEO_SIZE) {
                throw new IllegalArgumentException(
                    "Video file size exceeds maximum allowed size of " + (MAX_VIDEO_SIZE / 1024 / 1024) + "MB"
                );
            }
            return;
        }
        
        throw new IllegalArgumentException(
            "File type not allowed. Allowed types: " + 
            ALLOWED_IMAGE_EXTENSIONS + " (images), " + 
            ALLOWED_VIDEO_EXTENSIONS + " (videos)"
        );
    }


    private String extractFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex).toLowerCase();
    }


    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString() + extension.toLowerCase();
    }


    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            System.err.println("Attempted to delete file with invalid name: " + fileName);
            return;
        }
        
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            
            if (!filePath.startsWith(this.fileStorageLocation)) {
                System.err.println("Attempted to delete file outside storage directory: " + fileName);
                return;
            }
            
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            System.err.println("Could not delete file " + fileName + ": " + ex.getMessage());
        }
    }
    

    public boolean isAllowedFileType(String extension) {
        String lowerExt = extension.toLowerCase();
        return ALLOWED_IMAGE_EXTENSIONS.contains(lowerExt) || 
               ALLOWED_VIDEO_EXTENSIONS.contains(lowerExt);
    }
}

