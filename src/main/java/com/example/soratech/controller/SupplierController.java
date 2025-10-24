package com.example.soratech.controller;

import com.example.soratech.model.Supplier;
import com.example.soratech.service.SupplierService;
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
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public String listSuppliers(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String query,
                               @RequestParam(defaultValue = "name") String sortBy,
                               @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Supplier> suppliersPage;

        if (query != null && !query.isEmpty()) {
            suppliersPage = supplierService.searchByName(query, pageable);
        } else {
            suppliersPage = supplierService.findAllActive(pageable);
        }

        model.addAttribute("suppliers", suppliersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", suppliersPage.getTotalPages());
        model.addAttribute("query", query);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalElements", suppliersPage.getTotalElements());
        return "suppliers/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "suppliers/form";
    }

    @PostMapping("/create")
    public String createSupplier(@Valid @ModelAttribute Supplier supplier,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("supplier", supplier);
            return "suppliers/form";
        }
        
        try {
            supplierService.save(supplier);
            return "redirect:/admin?entity=suppliers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при сохранении поставщика: " + e.getMessage());
            model.addAttribute("supplier", supplier);
            return "suppliers/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.findById(id);
        model.addAttribute("supplier", supplier);
        return "suppliers/form";
    }

    @PostMapping("/edit/{id}")
    public String updateSupplier(@PathVariable Long id,
                                @Valid @ModelAttribute Supplier supplier,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("supplier", supplier);
            return "suppliers/form";
        }
        
        try {
            supplierService.update(id, supplier);
            return "redirect:/admin?entity=suppliers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении поставщика: " + e.getMessage());
            model.addAttribute("supplier", supplier);
            return "suppliers/form";
        }
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.findById(id);
        model.addAttribute("supplier", supplier);
        return "suppliers/details";
    }

    @PostMapping("/logic-delete/{id}")
    public String logicDelete(@PathVariable Long id) {
        supplierService.logicDelete(id);
        return "redirect:/admin?entity=suppliers";
    }

    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam(required = false) List<Long> ids,
                                @RequestParam String action) {
        if (ids != null && !ids.isEmpty()) {
            if ("physical".equals(action)) {
                supplierService.deleteAllByIds(ids);
            } else {
                supplierService.logicDeleteAllByIds(ids);
            }
        }
        return "redirect:/admin?entity=suppliers";
    }

    @GetMapping("/deleted")
    public String deletedSuppliers(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Supplier> deletedPage = supplierService.findAllDeleted(pageable);
        model.addAttribute("deletedSuppliers", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());
        return "suppliers/deleted";
    }

    @PostMapping("/restore/{id}")
    public String restoreSupplier(@PathVariable Long id) {
        supplierService.restore(id);
        return "redirect:/admin?entity=suppliers&view=deleted";
    }

    @PostMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id) {
        supplierService.delete(id);
        return "redirect:/admin?entity=suppliers&view=deleted";
    }
}


