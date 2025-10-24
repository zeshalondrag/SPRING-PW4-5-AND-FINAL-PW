package com.example.soratech.controller;

import com.example.soratech.model.Manufacturer;
import com.example.soratech.service.ManufacturerService;
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
@RequestMapping("/manufacturers")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping
    public String listManufacturers(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String query,
                                   @RequestParam(required = false) String country,
                                   @RequestParam(defaultValue = "name") String sortBy,
                                   @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Manufacturer> manufacturersPage;

        if (query != null && !query.isEmpty()) {
            manufacturersPage = manufacturerService.searchByName(query, pageable);
        } else if (country != null && !country.isEmpty()) {
            manufacturersPage = manufacturerService.filterByCountry(country, pageable);
        } else {
            manufacturersPage = manufacturerService.findAllActive(pageable);
        }

        model.addAttribute("manufacturers", manufacturersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", manufacturersPage.getTotalPages());
        model.addAttribute("query", query);
        model.addAttribute("country", country);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalElements", manufacturersPage.getTotalElements());
        return "manufacturers/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("manufacturer", new Manufacturer());
        return "manufacturers/form";
    }

    @PostMapping("/create")
    public String createManufacturer(@Valid @ModelAttribute Manufacturer manufacturer,
                                    BindingResult result,
                                    Model model) {
        if (result.hasErrors()) {
            model.addAttribute("manufacturer", manufacturer);
            return "manufacturers/form";
        }
        
        try {
            manufacturerService.save(manufacturer);
            return "redirect:/admin?entity=manufacturers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при сохранении производителя: " + e.getMessage());
            model.addAttribute("manufacturer", manufacturer);
            return "manufacturers/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Manufacturer manufacturer = manufacturerService.findById(id);
        model.addAttribute("manufacturer", manufacturer);
        return "manufacturers/form";
    }

    @PostMapping("/edit/{id}")
    public String updateManufacturer(@PathVariable Long id,
                                    @Valid @ModelAttribute Manufacturer manufacturer,
                                    BindingResult result,
                                    Model model) {
        if (result.hasErrors()) {
            model.addAttribute("manufacturer", manufacturer);
            return "manufacturers/form";
        }
        
        try {
            manufacturerService.update(id, manufacturer);
            return "redirect:/admin?entity=manufacturers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении производителя: " + e.getMessage());
            model.addAttribute("manufacturer", manufacturer);
            return "manufacturers/form";
        }
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        Manufacturer manufacturer = manufacturerService.findById(id);
        model.addAttribute("manufacturer", manufacturer);
        return "manufacturers/details";
    }

    @PostMapping("/logic-delete/{id}")
    public String logicDelete(@PathVariable Long id) {
        manufacturerService.logicDelete(id);
        return "redirect:/admin?entity=manufacturers";
    }

    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam(required = false) List<Long> ids,
                                @RequestParam String action) {
        if (ids != null && !ids.isEmpty()) {
            if ("physical".equals(action)) {
                manufacturerService.deleteAllByIds(ids);
            } else {
                manufacturerService.logicDeleteAllByIds(ids);
            }
        }
        return "redirect:/admin?entity=manufacturers";
    }

    @GetMapping("/deleted")
    public String deletedManufacturers(Model model,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Manufacturer> deletedPage = manufacturerService.findAllDeleted(pageable);
        model.addAttribute("deletedManufacturers", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());
        return "manufacturers/deleted";
    }

    @PostMapping("/restore/{id}")
    public String restoreManufacturer(@PathVariable Long id) {
        manufacturerService.restore(id);
        return "redirect:/admin?entity=manufacturers&view=deleted";
    }

    @PostMapping("/delete/{id}")
    public String deleteManufacturer(@PathVariable Long id) {
        manufacturerService.delete(id);
        return "redirect:/admin?entity=manufacturers&view=deleted";
    }
}


