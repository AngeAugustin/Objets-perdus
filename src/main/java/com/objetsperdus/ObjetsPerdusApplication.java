package com.objetsperdus;

import com.objetsperdus.model.Role;
import com.objetsperdus.model.User;
import com.objetsperdus.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ObjetsPerdusApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObjetsPerdusApplication.class, args);
    }
    
    /**
     * Initialisation des données pour le développement
     */
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Créer un utilisateur admin s'il n'existe pas déjà
            if (!userRepository.existsByEmail("admin@example.com")) {
                User admin = User.builder()
                        .firstname("Admin")
                        .lastname("System")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .build();
                        
                userRepository.save(admin);
            }
            
            // Créer un utilisateur standard s'il n'existe pas déjà
            if (!userRepository.existsByEmail("user@example.com")) {
                User user = User.builder()
                        .firstname("Utilisateur")
                        .lastname("Test")
                        .email("user@example.com")
                        .password(passwordEncoder.encode("user123"))
                        .phoneNumber("0123456789")
                        .role(Role.USER)
                        .build();
                        
                userRepository.save(user);
            }
        };
    }
}