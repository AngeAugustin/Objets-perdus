package com.objetsperdus.controller;

import com.objetsperdus.dto.ItemDto.ItemResponse;
import com.objetsperdus.model.FoundItem;
import com.objetsperdus.model.LostItem;
import com.objetsperdus.service.FoundItemService;
import com.objetsperdus.service.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    
    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "admin/dashboard";
    }
    
    @GetMapping("/lost-items/pending")
    public String pendingLostItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LostItem> lostItemPage = lostItemService.getAllPendingLostItems(pageable);
        
        Page<ItemResponse> responsePage = lostItemPage.map(lostItemService::mapToItemResponse);
        
        model.addAttribute("lostItems", responsePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", lostItemPage.getTotalPages());
        
        return "admin/lost-items-pending";
    }
    
    @GetMapping("/found-items/pending")
    public String pendingFoundItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FoundItem> foundItemPage = foundItemService.getAllPendingFoundItems(pageable);
        
        Page<ItemResponse> responsePage = foundItemPage.map(foundItemService::mapToItemResponse);
        
        model.addAttribute("foundItems", responsePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", foundItemPage.getTotalPages());
        
        return "admin/found-items-pending";
    }
    
    @PostMapping("/lost-items/{id}/approve")
    public String approveLostItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            lostItemService.approveLostItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "L'objet perdu a été approuvé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Une erreur s'est produite lors de l'approbation: " + e.getMessage());
        }
        
        return "redirect:/admin/lost-items/pending";
    }
    
    @PostMapping("/lost-items/{id}/reject")
    public String rejectLostItem(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        
        if (reason == null || reason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Veuillez fournir une raison pour le rejet.");
            return "redirect:/admin/lost-items/pending";
        }
        
        try {
            lostItemService.rejectLostItem(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "L'objet perdu a été rejeté avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Une erreur s'est produite lors du rejet: " + e.getMessage());
        }
        
        return "redirect:/admin/lost-items/pending";
    }
    
    @PostMapping("/found-items/{id}/approve")
    public String approveFoundItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            foundItemService.approveFoundItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "L'objet trouvé a été approuvé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Une erreur s'est produite lors de l'approbation: " + e.getMessage());
        }
        
        return "redirect:/admin/found-items/pending";
    }
    
    @PostMapping("/found-items/{id}/reject")
    public String rejectFoundItem(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        
        if (reason == null || reason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Veuillez fournir une raison pour le rejet.");
            return "redirect:/admin/found-items/pending";
        }
        
        try {
            foundItemService.rejectFoundItem(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "L'objet trouvé a été rejeté avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Une erreur s'est produite lors du rejet: " + e.getMessage());
        }
        
        return "redirect:/admin/found-items/pending";
    }
}