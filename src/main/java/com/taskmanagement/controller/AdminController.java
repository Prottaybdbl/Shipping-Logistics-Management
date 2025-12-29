package com.taskmanagement.controller;

import com.taskmanagement.entity.Institute;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.UserRole;
import com.taskmanagement.service.InstituteService;
import com.taskmanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final InstituteService instituteService;
    private final UserService userService;

    public AdminController(InstituteService instituteService, UserService userService) {
        this.instituteService = instituteService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalInstitutes", instituteService.findAll().size());
        model.addAttribute("activeInstitutes", instituteService.countActive());
        return "admin/dashboard";
    }

    // Institute Management
    @GetMapping("/institutes")
    public String listInstitutes(Model model) {
        model.addAttribute("institutes", instituteService.findAll());
        return "admin/institute-management";
    }

    @GetMapping("/institutes/new")
    public String newInstituteForm(Model model) {
        model.addAttribute("institute", new Institute());
        return "admin/institute-form";
    }

    @PostMapping("/institutes")
    public String createInstitute(@ModelAttribute Institute institute, RedirectAttributes redirectAttributes) {
        if (instituteService.existsByName(institute.getName())) {
            redirectAttributes.addFlashAttribute("error", "Institute name already exists");
            return "redirect:/admin/institutes/new";
        }
        instituteService.save(institute);
        redirectAttributes.addFlashAttribute("success", "Institute created successfully");
        return "redirect:/admin/institutes";
    }

    @GetMapping("/institutes/{id}/edit")
    public String editInstituteForm(@PathVariable Long id, Model model) {
        Institute institute = instituteService.findById(id)
            .orElseThrow(() -> new RuntimeException("Institute not found"));
        model.addAttribute("institute", institute);
        return "admin/institute-form";
    }

    @PostMapping("/institutes/{id}")
    public String updateInstitute(@PathVariable Long id, @ModelAttribute Institute institute, RedirectAttributes redirectAttributes) {
        institute.setId(id);
        instituteService.save(institute);
        redirectAttributes.addFlashAttribute("success", "Institute updated successfully");
        return "redirect:/admin/institutes";
    }

    @PostMapping("/institutes/{id}/delete")
    public String deleteInstitute(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        instituteService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Institute deleted successfully");
        return "redirect:/admin/institutes";
    }

    // User Management
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/user-management";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("institutes", instituteService.findAll());
        model.addAttribute("roles", UserRole.values());
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user, @RequestParam String password, RedirectAttributes redirectAttributes) {
        if (userService.existsByEmail(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/admin/users/new";
        }
        userService.createUser(user, password);
        redirectAttributes.addFlashAttribute("success", "User created successfully");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("institutes", instituteService.findAll());
        model.addAttribute("roles", UserRole.values());
        return "admin/user-form";
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, 
                            @RequestParam(required = false) String password, 
                            RedirectAttributes redirectAttributes) {
        User existing = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setId(id);
        if (password != null && !password.isEmpty()) {
            userService.createUser(user, password);
        } else {
            user.setPassword(existing.getPassword());
            userService.save(user);
        }
        redirectAttributes.addFlashAttribute("success", "User updated successfully");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        return "redirect:/admin/users";
    }
}
