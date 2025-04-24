package com.objetsperdus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Le prénom est obligatoire")
        private String firstname;
        
        @NotBlank(message = "Le nom est obligatoire")
        private String lastname;
        
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Veuillez fournir un email valide")
        private String email;
        
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        private String password;
        
        private String phoneNumber;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Veuillez fournir un email valide")
        private String email;
        
        @NotBlank(message = "Le mot de passe est obligatoire")
        private String password;
    }
}