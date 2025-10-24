package com.example.soratech.controller;

import com.example.soratech.model.User;
import com.example.soratech.service.UserService;
import com.example.soratech.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String listUsers(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String query,
                           @RequestParam(required = false) String searchType,
                           @RequestParam(defaultValue = "name") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> usersPage;

        if (query != null && !query.isEmpty()) {
            if ("email".equals(searchType)) {
                usersPage = userService.searchByEmail(query, pageable);
            } else {
                usersPage = userService.searchByName(query, pageable);
            }
        } else {
            usersPage = userService.findAllActive(pageable);
        }

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("query", query);
        model.addAttribute("searchType", searchType);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalElements", usersPage.getTotalElements());
        return "users/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.findAllActive());
        return "users/form";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute User user,
                            BindingResult result,
                            @RequestParam(required = false) Long roleId,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("roles", roleService.findAllActive());
            return "users/form";
        }
        
        try {
            userService.save(user, roleId);
            return "redirect:/admin?entity=users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при сохранении пользователя: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("roles", roleService.findAllActive());
            return "users/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.findAllActive());
        return "users/form";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                            @Valid @ModelAttribute User user,
                            BindingResult result,
                            @RequestParam(required = false) Long roleId,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("roles", roleService.findAllActive());
            return "users/form";
        }
        
        try {
            userService.update(id, user, roleId);
            return "redirect:/admin?entity=users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении пользователя: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("roles", roleService.findAllActive());
            return "users/form";
        }
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "users/details";
    }

    @PostMapping("/logic-delete/{id}")
    public String logicDelete(@PathVariable Long id) {
        userService.logicDelete(id);
        return "redirect:/admin?entity=users";
    }

    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam(required = false) List<Long> ids,
                                @RequestParam String action) {
        if (ids != null && !ids.isEmpty()) {
            if ("physical".equals(action)) {
                userService.deleteAllByIds(ids);
            } else {
                userService.logicDeleteAllByIds(ids);
            }
        }
        return "redirect:/admin?entity=users";
    }

    @GetMapping("/deleted")
    public String deletedUsers(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> deletedPage = userService.findAllDeleted(pageable);
        model.addAttribute("deletedUsers", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());
        return "users/deleted";
    }

    @PostMapping("/restore/{id}")
    public String restoreUser(@PathVariable Long id) {
        userService.restore(id);
        return "redirect:/admin?entity=users&view=deleted";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin?entity=users&view=deleted";
    }
}


