package com.example.soratech.controller;

import com.example.soratech.model.Category;
import com.example.soratech.service.CategoryService;
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
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String query,
                                @RequestParam(defaultValue = "name") String sortBy,
                                @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Category> categoriesPage;

        if (query != null && !query.isEmpty()) {
            categoriesPage = categoryService.searchByName(query, pageable);
        } else {
            categoriesPage = categoryService.findAllActive(pageable);
        }

        model.addAttribute("categories", categoriesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoriesPage.getTotalPages());
        model.addAttribute("query", query);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalElements", categoriesPage.getTotalElements());
        return "categories/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute Category category,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("category", category);
            return "categories/form";
        }
        
        try {
            categoryService.save(category);
            return "redirect:/admin?entity=categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при сохранении категории: " + e.getMessage());
            model.addAttribute("category", category);
            return "categories/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        model.addAttribute("category", category);
        return "categories/form";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id,
                                @Valid @ModelAttribute Category category,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("category", category);
            return "categories/form";
        }
        
        try {
            categoryService.update(id, category);
            return "redirect:/admin?entity=categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении категории: " + e.getMessage());
            model.addAttribute("category", category);
            return "categories/form";
        }
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        model.addAttribute("category", category);
        return "categories/details";
    }

    @PostMapping("/logic-delete/{id}")
    public String logicDelete(@PathVariable Long id) {
        categoryService.logicDelete(id);
        return "redirect:/admin?entity=categories";
    }

    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam(required = false) List<Long> ids,
                                @RequestParam String action) {
        if (ids != null && !ids.isEmpty()) {
            if ("physical".equals(action)) {
                categoryService.deleteAllByIds(ids);
            } else {
                categoryService.logicDeleteAllByIds(ids);
            }
        }
        return "redirect:/admin?entity=categories";
    }

    @GetMapping("/deleted")
    public String deletedCategories(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> deletedPage = categoryService.findAllDeleted(pageable);
        model.addAttribute("deletedCategories", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());
        return "categories/deleted";
    }

    @PostMapping("/restore/{id}")
    public String restoreCategory(@PathVariable Long id) {
        categoryService.restore(id);
        return "redirect:/admin?entity=categories&view=deleted";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/admin?entity=categories&view=deleted";
    }
}


