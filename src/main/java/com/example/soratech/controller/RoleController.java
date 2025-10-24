package com.example.soratech.controller;

import com.example.soratech.model.Role;
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
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public String listRoles(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String query,
                           @RequestParam(defaultValue = "name") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Role> rolesPage;

        if (query != null && !query.isEmpty()) {
            rolesPage = roleService.searchByName(query, pageable);
        } else {
            rolesPage = roleService.findAllActive(pageable);
        }

        model.addAttribute("roles", rolesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rolesPage.getTotalPages());
        model.addAttribute("query", query);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalElements", rolesPage.getTotalElements());
        return "roles/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("role", new Role());
        return "roles/form";
    }

    @PostMapping("/create")
    public String createRole(@Valid @ModelAttribute Role role,
                            BindingResult result,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("role", role);
            return "roles/form";
        }
        
        try {
            roleService.save(role);
            return "redirect:/admin?entity=roles";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при сохранении роли: " + e.getMessage());
            model.addAttribute("role", role);
            return "roles/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Role role = roleService.findById(id);
        model.addAttribute("role", role);
        return "roles/form";
    }

    @PostMapping("/edit/{id}")
    public String updateRole(@PathVariable Long id,
                            @Valid @ModelAttribute Role role,
                            BindingResult result,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("role", role);
            return "roles/form";
        }
        
        try {
            roleService.update(id, role);
            return "redirect:/admin?entity=roles";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении роли: " + e.getMessage());
            model.addAttribute("role", role);
            return "roles/form";
        }
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        Role role = roleService.findById(id);
        model.addAttribute("role", role);
        return "roles/details";
    }

    @PostMapping("/logic-delete/{id}")
    public String logicDelete(@PathVariable Long id) {
        roleService.logicDelete(id);
        return "redirect:/admin?entity=roles";
    }

    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam(required = false) List<Long> ids,
                                @RequestParam String action) {
        if (ids != null && !ids.isEmpty()) {
            if ("physical".equals(action)) {
                roleService.deleteAllByIds(ids);
            } else {
                roleService.logicDeleteAllByIds(ids);
            }
        }
        return "redirect:/admin?entity=roles";
    }

    @GetMapping("/deleted")
    public String deletedRoles(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Role> deletedPage = roleService.findAllDeleted(pageable);
        model.addAttribute("deletedRoles", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());
        return "roles/deleted";
    }

    @PostMapping("/restore/{id}")
    public String restoreRole(@PathVariable Long id) {
        roleService.restore(id);
        return "redirect:/admin?entity=roles&view=deleted";
    }

    @PostMapping("/delete/{id}")
    public String deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return "redirect:/admin?entity=roles&view=deleted";
    }
}


