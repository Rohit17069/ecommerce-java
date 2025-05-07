package com.bootcamp.ecommerce_rohit.services;

import com.bootcamp.ecommerce_rohit.exceptionsHandling.InvalidParametersException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class ImageUtils {



//        private static final List<String> ALLOWED_IMAGE_FORMATS = Arrays.asList("image/jpeg", "image/png");

    public void saveImageOnServer(MultipartFile profileImage, String imageId, String where) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new RuntimeException("No file uploaded");
        }

        String fileType = profileImage.getContentType(); // e.g., image/jpeg, image/png

        // Supported types
        Map<String, String> mimeToExtension = Map.of(
                "image/jpeg", "jpg",
                "image/jpg", "jpg",
                "image/png", "png",
                "image/bmp", "bmp"
        );

        if (!mimeToExtension.containsKey(fileType)) {
            throw new InvalidParametersException("Invalid image format. Only JPG, JPEG, PNG, BMP are allowed");
        }

        String extension = mimeToExtension.get(fileType);
        String fileName = imageId + "." + extension;

        Path uploadPath = Paths.get("uploads/" + where);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.list(uploadPath)
                    .filter(p -> p.getFileName().toString().matches(imageId + "\\.(jpg|jpeg|png|bmp)"))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                        }
                    });
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }


    public String getImageURL(String imageId,String where) {
        Path folderPath = Paths.get("uploads/"+where+"/");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, imageId + "*")) {
            for (Path entry : stream) {
                return entry.toAbsolutePath().toString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }
    public List<String> getImageURLs(String imageId, String where) {
        Path folderPath = Paths.get("uploads/" + where + "/");
        List<String> matchingFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                if (fileName.contains(imageId)) {
                    matchingFiles.add(entry.toAbsolutePath().toString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to list image files: " + e.getMessage(), e);
        }

        return matchingFiles;
    }


}
