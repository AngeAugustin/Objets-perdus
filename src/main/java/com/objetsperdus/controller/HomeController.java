package com.objetsperdus.controller;

import com.objetsperdus.service.FoundItemService;
import com.objetsperdus.service.LostItemService;
import com.objetsperdus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    private final NotificationService notificationService;
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentLostItems", lostItemService.getRecentLostItems().stream()
                .map(lostItemService::mapToItemResponse)
                .toList());
                
        model.addAttribute("recentFoundItems", foundItemService.getRecentFoundItems().stream()
                .map(foundItemService::mapToItemResponse)
                .toList());
                
        model.addAttribute("unreadNotifications", notificationService.countUnreadNotifications());
        
        return "home";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}