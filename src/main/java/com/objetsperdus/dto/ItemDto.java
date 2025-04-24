package com.objetsperdus.dto;

import com.objetsperdus.model.ItemCategory;
import com.objetsperdus.model.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class ItemDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LostItemRequest {
        @NotBlank(message = "Le titre est obligatoire")
        private String title;
        
        @NotBlank(message = "La description est obligatoire")
        private String description;
        
        @NotNull(message = "La catégorie est obligatoire")
        private ItemCategory category;
        
        @NotNull(message = "La date de perte est obligatoire")
        @PastOrPresent(message = "La date ne peut pas être dans le futur")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate lostDate;
        
        @NotBlank(message = "Le lieu est obligatoire")
        private String location;
        
        private MultipartFile image;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoundItemRequest {
        @NotBlank(message = "Le titre est obligatoire")
        private String title;
        
        @NotBlank(message = "La description est obligatoire")
        private String description;
        
        @NotNull(message = "La catégorie est obligatoire")
        private ItemCategory category;
        
        @NotNull(message = "La date de découverte est obligatoire")
        @PastOrPresent(message = "La date ne peut pas être dans le futur")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate foundDate;
        
        @NotBlank(message = "Le lieu est obligatoire")
        private String location;
        
        private MultipartFile image;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        private Long id;
        private String title;
        private String description;
        private ItemCategory category;
        private String categoryDisplayName;
        private LocalDate date; // lostDate or foundDate
        private String location;
        private String imageUrl;
        private ItemStatus status;
        private String statusDisplayName;
        private String ownerName;
        private String ownerEmail;
        private String ownerPhoneNumber;
        private boolean isOwner;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemSearchCriteria {
        private String keyword;
        private ItemCategory category;
        
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }
}