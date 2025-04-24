package com.objetsperdus.controller;

import com.objetsperdus.dto.ItemDto.ItemResponse;
import com.objetsperdus.model.FoundItem;
import com.objetsperdus.model.LostItem;
import com.objetsperdus.model.Notification;
import com.objetsperdus.model.User;
import com.objetsperdus.service.FoundItemService;
import com.objetsperdus.service.LostItemService;
import com.objetsperdus.service.NotificationService;
import com.objetsperdus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    private final NotificationService notificationService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        
        Pageable topItems = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        
        Page<LostItem> lostItems = lostItemService.getUserLostItems(topItems);
        Page<FoundItem> foundItems = foundItemService.getUserFoundItems(topItems);
        
        model.addAttribute("lostItems", lostItems.map(lostItemService::mapToItemResponse));
        model.addAttribute("foundItems", foundItems.map(foundItemService::mapToItemResponse));
        model.addAttribute("unreadNotifications", notificationService.countUnreadNotifications());
        
        return "user/dashboard";
    }
    
    @GetMapping("/lost-items")
    public String userLostItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LostItem> lostItemPage = lostItemService.getUserLostItems(pageable);
        
        Page<ItemResponse> responsePage = lostItemPage.map(lostItemService::mapToItemResponse);
        
        model.addAttribute("lostItems", responsePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", lostItemPage.getTotalPages());
        
        return "user/lost-items";
    }
    
    @GetMapping("/found-items")
    public String userFoundItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FoundItem> foundItemPage = foundItemService.getUserFoundItems(pageable);
        
        Page<ItemResponse> responsePage = foundItemPage.map(foundItemService::mapToItemResponse);
        
        model.addAttribute("foundItems", responsePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", foundItemPage.getTotalPages());
        
        return "user/found-items";
    }
    
    @GetMapping("/notifications")
    public String userNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationPage = notificationService.getUserNotifications(pageable);
        
        model.addAttribute("notifications", notificationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notificationPage.getTotalPages());
        
        return "user/notifications";
    }
    
    @PostMapping("/notifications/{id}/mark-read")
    @ResponseBody
    public String markNotificationAsRead(@PathVariable Long id) {
        try {
            notificationService.markNotificationAsRead(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
    
    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsAsRead(RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAllNotificationsAsRead();
            redirectAttributes.addFlashAttribute("successMessage", "Toutes les notifications ont été marquées comme lues.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur s'est produite: " + e.getMessage());
        }
        
        return "redirect:/user/notifications";
    }
    
    @GetMapping("/profile")
    public String userProfile(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        
        return "user/profile";
    }
}