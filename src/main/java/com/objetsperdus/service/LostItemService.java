package com.objetsperdus.service;

import com.objetsperdus.dto.ItemDto.ItemResponse;
import com.objetsperdus.dto.ItemDto.ItemSearchCriteria;
import com.objetsperdus.dto.ItemDto.LostItemRequest;
import com.objetsperdus.exception.ResourceNotFoundException;
import com.objetsperdus.model.ItemStatus;
import com.objetsperdus.model.LostItem;
import com.objetsperdus.model.Notification;
import com.objetsperdus.model.Role;
import com.objetsperdus.model.User;
import com.objetsperdus.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemService {
    
    private final LostItemRepository lostItemRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    
    @Transactional
    public LostItem createLostItem(LostItemRequest request) {
        User currentUser = userService.getCurrentUser();
        
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageUrl = fileStorageService.storeFile(request.getImage());
        }
        
        LostItem lostItem = LostItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .lostDate(request.getLostDate())
                .location(request.getLocation())
                .imageUrl(imageUrl)
                .user(currentUser)
                .build();
                
        return lostItemRepository.save(lostItem);
    }
    
    @Transactional(readOnly = true)
    public LostItem getLostItemById(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Objet perdu non trouvé avec l'id: " + id));
    }
    
    @Transactional(readOnly = true)
    public Page<LostItem> getAllApprovedLostItems(Pageable pageable) {
        return lostItemRepository.findByStatus(ItemStatus.APPROVED, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<LostItem> getAllPendingLostItems(Pageable pageable) {
        return lostItemRepository.findByStatus(ItemStatus.PENDING, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<LostItem> getUserLostItems(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return lostItemRepository.findByUser(currentUser, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<LostItem> searchLostItems(ItemSearchCriteria criteria, Pageable pageable) {
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            return lostItemRepository.searchByKeyword(ItemStatus.APPROVED, criteria.getKeyword(), pageable);
        }
        
        if (criteria.getCategory() != null && criteria.getStartDate() != null && criteria.getEndDate() != null) {
            return lostItemRepository.findByFilters(
                    ItemStatus.APPROVED,
                    criteria.getCategory(),
                    criteria.getStartDate(),
                    criteria.getEndDate(),
                    pageable
            );
        }
        
        if (criteria.getCategory() != null) {
            return lostItemRepository.findByStatusAndCategory(ItemStatus.APPROVED, criteria.getCategory(), pageable);
        }
        
        return getAllApprovedLostItems(pageable);
    }
    
    @Transactional
    public LostItem updateLostItem(Long id, LostItemRequest request) {
        LostItem lostItem = getLostItemById(id);
        User currentUser = userService.getCurrentUser();
        
        if (!lostItem.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à modifier cet objet perdu");
        }
        
        if (lostItem.getStatus() != ItemStatus.PENDING) {
            throw new IllegalStateException("Vous ne pouvez modifier que les objets en attente de validation");
        }
        
        lostItem.setTitle(request.getTitle());
        lostItem.setDescription(request.getDescription());
        lostItem.setCategory(request.getCategory());
        lostItem.setLostDate(request.getLostDate());
        lostItem.setLocation(request.getLocation());
        
        // Mise à jour de l'image si une nouvelle est fournie
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            // Supprimer l'ancienne image si elle existe
            if (lostItem.getImageUrl() != null) {
                fileStorageService.deleteFile(lostItem.getImageUrl());
            }
            String imageUrl = fileStorageService.storeFile(request.getImage());
            lostItem.setImageUrl(imageUrl);
        }
        
        return lostItemRepository.save(lostItem);
    }
    
    @Transactional
    public void deleteLostItem(Long id) {
        LostItem lostItem = getLostItemById(id);
        User currentUser = userService.getCurrentUser();
        
        if (!lostItem.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à supprimer cet objet perdu");
        }
        
        // Supprimer l'image si elle existe
        if (lostItem.getImageUrl() != null) {
            fileStorageService.deleteFile(lostItem.getImageUrl());
        }
        
        lostItemRepository.delete(lostItem);
    }
    
    @Transactional
    public LostItem approveLostItem(Long id) {
        LostItem lostItem = getLostItemById(id);
        
        if (lostItem.getStatus() != ItemStatus.PENDING) {
            throw new IllegalStateException("Seuls les objets en attente peuvent être approuvés");
        }
        
        lostItem.setStatus(ItemStatus.APPROVED);
        lostItem.setValidatedAt(LocalDateTime.now());
        
        // Notification au créateur de l'annonce
        notificationService.createNotification(
                lostItem.getUser(),
                "Annonce approuvée",
                "Votre annonce d'objet perdu '" + lostItem.getTitle() + "' a été approuvée."
        );
        
        return lostItemRepository.save(lostItem);
    }
    
    @Transactional
    public LostItem rejectLostItem(Long id, String reason) {
        LostItem lostItem = getLostItemById(id);
        
        if (lostItem.getStatus() != ItemStatus.PENDING) {
            throw new IllegalStateException("Seuls les objets en attente peuvent être rejetés");
        }
        
        lostItem.setStatus(ItemStatus.REJECTED);
        
        // Notification au créateur de l'annonce
        notificationService.createNotification(
                lostItem.getUser(),
                "Annonce rejetée",
                "Votre annonce d'objet perdu '" + lostItem.getTitle() + "' a été rejetée. Raison: " + reason
        );
        
        return lostItemRepository.save(lostItem);
    }
    
    @Transactional(readOnly = true)
    public List<LostItem> getRecentLostItems() {
        return lostItemRepository.findTop5ByStatusOrderByCreatedAtDesc(ItemStatus.APPROVED);
    }
    
    public ItemResponse mapToItemResponse(LostItem lostItem) {
        User currentUser = null;
        boolean isOwner = false;
        
        try {
            currentUser = userService.getCurrentUser();
            isOwner = lostItem.getUser().getId().equals(currentUser.getId());
        } catch (Exception e) {
            // Utilisateur non connecté
        }
        
        return ItemResponse.builder()
                .id(lostItem.getId())
                .title(lostItem.getTitle())
                .description(lostItem.getDescription())
                .category(lostItem.getCategory())
                .categoryDisplayName(lostItem.getCategory().getDisplayName())
                .date(lostItem.getLostDate())
                .location(lostItem.getLocation())
                .imageUrl(lostItem.getImageUrl())
                .status(lostItem.getStatus())
                .statusDisplayName(lostItem.getStatus().getDisplayName())
                .ownerName(lostItem.getUser().getFirstname() + " " + lostItem.getUser().getLastname())
                .ownerEmail(isOwner ? lostItem.getUser().getEmail() : null)
                .ownerPhoneNumber(isOwner ? lostItem.getUser().getPhoneNumber() : null)
                .isOwner(isOwner)
                .build();
    }
}