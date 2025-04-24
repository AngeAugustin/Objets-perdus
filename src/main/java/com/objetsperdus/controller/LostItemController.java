package com.objetsperdus.controller;

import com.objetsperdus.dto.ItemDto.ItemResponse;
import com.objetsperdus.dto.ItemDto.ItemSearchCriteria;
import com.objetsperdus.dto.ItemDto.LostItemRequest;
import com.objetsperdus.model.ItemCategory;
import com.objetsperdus.model.LostItem;
import com.objetsperdus.service.LostItemService;
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
@RequestMapping("/lost-items")
@RequiredArgsConstructor
public class LostItemController {
    
    private final LostItemService lostItemService;
    
    @GetMapping
    public String listLostItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute("searchCriteria") ItemSearchCriteria searchCriteria,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LostItem> lostItemPage = lostItemService.searchLostItems(searchCriteria, pageable);
        
        Page<ItemResponse> responsePage = lostItemPage.map(lostItemService::mapToItemResponse);
        
        model.addAttribute("lostItems", responsePage);
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", lostItemPage.getTotalPages());
        
        return "lost-items/list";
    }
    
    @GetMapping("/{id}")
    public String viewLostItem(@PathVariable Long id, Model model) {
        LostItem lostItem = lostItemService.getLostItemById(id);
        model.addAttribute("item", lostItemService.mapToItemResponse(lostItem));
        
        return "lost-items/view";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("lostItemRequest", new LostItemRequest());
        model.addAttribute("categories", ItemCategory.values());
        
        return "lost-items/create";
    }
    
    @PostMapping("/create")
    public String createLostItem(
            @Valid @ModelAttribute("lostItemRequest") LostItemRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("categories", ItemCategory.values());
            return "lost-items/create";
        }
        
        try {
            LostItem lostItem = lostItemService.createLostItem(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Votre déclaration d'objet perdu a été créée et est en attente de validation.");
            
            return "redirect:/user/lost-items";
        } catch (Exception e) {
            model.addAttribute("categories", ItemCategory.values());
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de la création: " + e.getMessage());
            
            return "lost-items/create";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            LostItem lostItem = lostItemService.getLostItemById(id);
            
            LostItemRequest lostItemRequest = LostItemRequest.builder()
                    .title(lostItem.getTitle())
                    .description(lostItem.getDescription())
                    .category(lostItem.getCategory())
                    .lostDate(lostItem.getLostDate())
                    .location(lostItem.getLocation())
                    .build();
                    
            model.addAttribute("lostItemRequest", lostItemRequest);
            model.addAttribute("itemId", id);
            model.addAttribute("categories", ItemCategory.values());
            
            return "lost-items/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Impossible de modifier cet objet: " + e.getMessage());
            return "redirect:/user/lost-items";
        }
    }
    
    @PostMapping("/edit/{id}")
    public String updateLostItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("lostItemRequest") LostItemRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("itemId", id);
            model.addAttribute("categories", ItemCategory.values());
            return "lost-items/edit";
        }
        
        try {
            lostItemService.updateLostItem(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "L'objet perdu a été mis à jour avec succès.");
            
            return "redirect:/user/lost-items";
        } catch (Exception e) {
            model.addAttribute("itemId", id);
            model.addAttribute("categories", ItemCategory.values());
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de la mise à jour: " + e.getMessage());
            
            return "lost-items/edit";
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteLostItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            lostItemService.deleteLostItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "L'objet perdu a été supprimé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Une erreur s'est produite lors de la suppression: " + e.getMessage());
        }
        
        return "redirect:/user/lost-items";
    }
}