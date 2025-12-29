package com.taskmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        // Render the Thymeleaf template located at: src/main/resources/templates/auth/login.html
        return "auth/login";
    }

    @GetMapping("/")
    public String rootToBoards() {
        // Optional: send root to the main app page after login
        return "redirect:/boards";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        // SecurityConfig redirects here on 403; redirect to login with a flag
        return "redirect:/login?error=forbidden";
    }
}
