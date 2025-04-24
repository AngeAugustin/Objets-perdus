package com.objetsperdus.service;

import com.objetsperdus.dto.ItemDto.FoundItemRequest;
import com.objetsperdus.dto.ItemDto.ItemResponse;
import com.objetsperdus.dto.ItemDto.ItemSearchCriteria;
import com.objetsperdus.exception.ResourceNotFoundException;
import com.objetsperdus.model.FoundItem;
import com.objetsperdus.model.ItemStatus;
import com.objetsperdus.model.Role;
import com.objetsperdus.model.User;
import com.objetsperdus.repository.FoundItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoundItemService {
    
    private final FoundItemRepository foundItemRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    
    @Transactional
    public FoundItem createFoundItem(FoundItemRequest request) {
        User currentUser = userService.getCurrentUser();
        
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageUrl = fileStorageService.storeFile(request.getImage());
        }
        
        FoundItem foundItem = FoundItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .foundDate(request.getFoundDate())
                .location(request.getLocation())
                .imageUrl(imageUrl)
                .user(currentUser)
                .build();
                
        return foundItemRepository.save(foundItem);
    }
    
    @Transactional(readOnly = true)
    public FoundItem getFoundItemById(Long id) {
        return foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Objet trouvé non trouvé avec l'id: " + id));
    }
    
    @Transactional(readOnly = true)
    public Page<FoundItem> getAllApprovedFoundItems(Pageable pageable) {
        return foundItemRepository.findByStatus(ItemStatus.APPROVED, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<FoundItem> getAllPendingFoundItems(Pageable pageable) {
        return foundItemRepository.findByStatus(ItemStatus.PENDING, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<FoundItem> getUserFoundItems(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return foundItemRepository.findByUser(currentUser, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<FoundItem> searchFoundItems(ItemSearchCriteria criteria, Pageable pageable) {
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            return foundItemRepository.searchByKeyword(ItemStatus.APPROVED, criteria.getKeyword(), pageable);
        }
        
        if (criteria.getCategory() != null && criteria.getStartDate() != null && criteria.getEndDate() != null) {
            return foundItemRepository.findByFilters(
                    ItemStatus.APPROVED,
                    criteria.getCategory(),
                    criteria.getStartDate(),
                    criteria.getEndDate(),
                    pageable
            );
        }
        
        if (criteria.getCategory() != null) {
            return foundItemRepository.findByStatusAndCategory(ItemStatus.APPROVED, criteria.getCategory(), pageable);
        }
        
        return getAllApprovedFoundItems(pageable);
    }
    
    @Transactional
    public FoundItem updateFoundItem(Long id, FoundItemRequest request) {
        FoundItem foundItem = getFoundItemById(id);
        User currentUser = userService.getCurrentUser();
        
        if (!foundItem.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à modifier cet objet trouvé");
        }
        
        if (foundItem.getStatus() != ItemStatus.PENDING) {
            throw new IllegalStateException("Vous ne pouvez modifier que les objets en attente de validation");
        }
        
        foundItem.setTitle(request.getTitle());
        foundItem.setDescription(request.getDescription());
        foundItem.setCategory(request.getCategory());
        foundItem.setFoundDate(request.getFoundDate());
        foundItem.setLocation(request.getLocation());
        
        // Mise à jour de l'image si une nouvelle est fournie
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            // Supprimer l'ancienne image si elle existe
            if (foundItem.getImageUrl() != null) {
                fileStorageService.deleteFile(foundItem.getImageUrl());
            }
            String imageUrl = fileStorageService.storeFile(request.getImage());
            foundItem.setImageUrl(imageUrl);
        }
        
        return foundItemRepository.save(foundItem);
    }
    
    @Transactional
    public void deleteFoundItem(Long id) {
        FoundItem foundItem = getFoundItemById(id);
        User currentUser = userService.getCurrentUser();
        
        if (!foundItem.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à supprimer cet objet trouvé");
        }
        
        // Supprimer l'image si elle existe
        if (foundItem.getImageUrl() != null) {
            fileStorageService.deleteFile(foundItem.getImageUrl());
        }
        
        foundItemRepository.delete(foundItem);
    }
    
    @Transactional
    public FoundItem approveFoundItem(Long id) {
        FoundItem foundItem = getFoundItemById(id);
        
        if (foundItem.getStatus() != ItemStatus.PENDING) {
            throw new IllegalStateException("Seuls les objets en attente peuvent être approuvés");
        }
        
        foundItem.setStatus(ItemStatus.APPROVED);
        foundItem.setValidatedAt(LocalDateTime.now());
        
        // Notification au créateur de l'annonce
        notificationService.createNotification(
                foundItem.getUser(),
                "Annonce approuvée",
                "Votre annonce d'objet trouvé '" + foundItem.getTitle() + "' a été approuvée."
        );
        
        return foundItemRepository.save(foundItem);
    }
    
    @Transactional
    public FoundItem rejectFoundItem(Long id, String reason) {
        FoundItem foundItem = getFoundItemById(id);
        
        if (foundItem.getStatus() != ItemStatus.PENDING) {
            throw new IllegalStateException("Seuls les objets en attente peuvent être rejetés");
        }
        
        foundItem.setStatus(ItemStatus.REJECTED);
        
        // Notification au créateur de l'annonce
        notificationService.createNotification(
                foundItem.getUser(),
                "Annonce rejetée",
                "Votre annonce d'objet trouvé '" + foundItem.getTitle() + "' a été rejetée. Raison: " + reason
        );
        
        return foundItemRepository.save(foundItem);
    }
    
    @Transactional
    public FoundItem claimFoundItem(Long id) {
        FoundItem foundItem = getFoundItemById(id);
        User currentUser = userService.getCurrentUser();
        
        if (foundItem.getStatus() != ItemStatus.APPROVED) {
            throw new IllegalStateException("Seuls les objets approuvés peuvent être réclamés");
        }
        
        foundItem.setStatus(ItemStatus.CLAIMED);
        
        // Notification au propriétaire de l'objet trouvé
        notificationService.createNotification(
                foundItem.getUser(),
                "Objet réclamé",
                "L'objet trouvé '" + foundItem.getTitle() + "' a été réclamé par " + 
                currentUser.getFirstname() + " " + currentUser.getLastname() + 
                ". Veuillez le contacter à l'adresse " + currentUser.getEmail()
        );
        
        return foundItemRepository.save(foundItem);
    }
    
    @Transactional(readOnly = true)
    public List<FoundItem> getRecentFoundItems() {
        return foundItemRepository.findTop5ByStatusOrderByCreatedAtDesc(ItemStatus.APPROVED);
    }
    
    public ItemResponse mapToItemResponse(FoundItem foundItem) {
        User currentUser = null;
        boolean isOwner = false;
        
        try {
            currentUser = userService.getCurrentUser();
            isOwner = foundItem.getUser().getId().equals(currentUser.getId());
        } catch (Exception e) {
            // Utilisateur non connecté
        }
        
        return ItemResponse.builder()
                .id(foundItem.getId())
                .title(foundItem.getTitle())
                .description(foundItem.getDescription())
                .category(foundItem.getCategory())
                .categoryDisplayName(foundItem.getCategory().getDisplayName())
                .date(foundItem.getFoundDate())
                .location(foundItem.getLocation())
                .imageUrl(foundItem.getImageUrl())
                .status(foundItem.getStatus())
                .statusDisplayName(foundItem.getStatus().getDisplayName())
                .ownerName(foundItem.getUser().getFirstname() + " " + foundItem.getUser().getLastname())
                .ownerEmail(isOwner ? foundItem.getUser().getEmail() : null)
                .ownerPhoneNumber(isOwner ? foundItem.getUser().getPhoneNumber() : null)
                .isOwner(isOwner)
                .build();
    }
}