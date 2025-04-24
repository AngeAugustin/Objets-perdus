package com.objetsperdus.service;

import com.objetsperdus.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    private final Path fileStorageLocation;
    
    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();
                
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Impossible de créer le répertoire où les fichiers téléchargés seront stockés.", ex);
        }
    }
    
    public String storeFile(MultipartFile file) {
        // Normalisation du nom de fichier
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        if (originalFileName.contains("..")) {
            throw new FileStorageException("Désolé! Le nom du fichier contient un chemin invalide " + originalFileName);
        }
        
        // Générer un nom de fichier unique avec UUID
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            extension = originalFileName.substring(i);
        }
        String fileName = UUID.randomUUID().toString() + extension;
        
        try {
            // Copier le fichier dans le répertoire de destination
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de stocker le fichier " + fileName + ". Veuillez réessayer!", ex);
        }
    }
    
    public void deleteFile(String fileName) {
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.deleteIfExists(targetLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de supprimer le fichier " + fileName, ex);
        }
    }
    
    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName);
    }
}