package com.objetsperdus.controller;

import com.objetsperdus.dto.ItemDto.FoundItemRequest;
import com.objetsperdus.dto.ItemDto.ItemResponse;
import com.objetsperdus.dto.ItemDto.ItemSearchCriteria;
import com.objetsperdus.model.FoundItem;
import com.objetsperdus.model.ItemCategory;
import com.objetsperdus.service.FoundItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/found-items")
@RequiredArgsConstructor
public class FoundItemController {
    
    private final FoundItemService foundItemService;
    
    @GetMapping
    public String listFoundItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute("searchCriteria") ItemSearchCriteria searchCriteria,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FoundItem> foundItemPage = foundItemService.searchFoundItems(searchCriteria, pageable);
        
        Page<ItemResponse> responsePage = foundItemPage.map(foundItemService::mapToItemResponse);
        
        model.addAttribute("foundItems", responsePage);
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", foundItemPage.getTotalPages());
        
        return "found-items/list";
    }
    
    @GetMapping("/{id}")
    public String viewFoundItem(@PathVariable Long id, Model model) {
        FoundItem foundItem = foundItemService.getFoundItemById(id);
        model.addAttribute("item", foundItemService.mapToItemResponse(foundItem));
        
        return "found-items/view";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("foundItemRequest", new FoundItemRequest());
        model.addAttribute("categories", ItemCategory.values());
        
        return "found-items/create";
    }
    
    @PostMapping("/create")
    public String createFoundItem(
            @Valid @ModelAttribute("foundItemRequest") FoundItemRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("categories", ItemCategory.values());
            return "found-items/create";
        }
        
        try {
            FoundItem foundItem = foundItemService.createFoundItem(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Votre déclaration d'objet trouvé a été créée et est en attente de validation.");
            
            return "redirect:/user/found-items";
        } catch (Exception e) {
            model.addAttribute("categories", ItemCategory.values());
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de la création: " + e.getMessage());
            
            return "found-items/create";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            FoundItem foundItem = foundItemService.getFoundItemById(id);
            
            FoundItemRequest foundItemRequest = FoundItemRequest.builder()
                    .title(foundItem.getTitle())
                    .description(foundItem.getDescription())
                    .category(foundItem.getCategory())
                    .foundDate(foundItem.getFoundDate())
                    .location(foundItem.getLocation())
                    .build();
                    
            model.addAttribute("foundItemRequest", foundItemRequest);
            model.addAttribute("itemId", id);
            model.addAttribute("categories", ItemCategory.values());
            
            return "found-items/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Impossible de modifier cet objet: " + e.getMessage());
            return "redirect:/user/found-items";
        }
    }
    
    @PostMapping("/edit/{id}")
    public String updateFoundItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("foundItemRequest") FoundItemRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("itemId", id);
            model.addAttribute("categories", ItemCategory.values());
            return "found-items/edit";
        }
        
        try {
            foundItemService.updateFoundItem(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "L'objet trouvé a été mis à jour avec succès.");
            
            return "redirect:/user/found-items";
        } catch (Exception e) {
            model.addAttribute("itemId", id);
            model.addAttribute("categories", ItemCategory.values());
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de la mise à jour: " + e.getMessage());
            
            return "found-items/edit";
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteFoundItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            foundItemService.deleteFoundItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "L'objet trouvé a été supprimé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Une erreur s'est produite lors de la suppression: " + e.getMessage());
        }
        
        return "redirect:/user/found-items";
    }
    
    @PostMapping("/claim/{id}")
    public String claimFoundItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            foundItemService.claimFoundItem(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Votre demande de réclamation a été envoyée. Le propriétaire actuel sera notifié.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Une erreur s'est produite lors de la réclamation: " + e.getMessage());
        }
        
        return "redirect:/found-items/" + id;
    }
}