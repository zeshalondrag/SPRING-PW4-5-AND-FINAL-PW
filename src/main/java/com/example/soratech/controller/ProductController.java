package com.example.soratech.controller;

import com.example.soratech.model.Product;
import com.example.soratech.service.CategoryService;
import com.example.soratech.service.ManufacturerService;
import com.example.soratech.service.ProductService;
import com.example.soratech.service.SupplierService;
import com.example.soratech.util.RedirectHelper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ManufacturerService manufacturerService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;

    public ProductController(ProductService productService,
                             ManufacturerService manufacturerService,
                             CategoryService categoryService,
                             SupplierService supplierService) {
        this.productService = productService;
        this.manufacturerService = manufacturerService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
    }

    @GetMapping
    public String listProducts(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String query,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) Long manufacturerId,
                               @RequestParam(required = false) Long supplierId,
                               @RequestParam(required = false) BigDecimal minPrice,
                               @RequestParam(required = false) BigDecimal maxPrice,
                               @RequestParam(defaultValue = "name") String sortBy,
                               @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productsPage;

        if (query != null && !query.isEmpty()) {
            productsPage = productService.searchByName(query, pageable);
        } else if (categoryId != null) {
            productsPage = productService.filterByCategory(categoryId, pageable);
        } else if (manufacturerId != null) {
            productsPage = productService.filterByManufacturer(manufacturerId, pageable);
        } else if (supplierId != null) {
            productsPage = productService.filterBySupplier(supplierId, pageable);
        } else if (minPrice != null || maxPrice != null) {
            productsPage = productService.filterByPriceRange(minPrice, maxPrice, pageable);
        } else {
            productsPage = productService.findAllActive(pageable);
        }

        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("query", query);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("manufacturerId", manufacturerId);
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalElements", productsPage.getTotalElements());
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("manufacturers", manufacturerService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAllActive());
        return "products/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("manufacturers", manufacturerService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAllActive());
        return "products/form";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute Product product,
                               BindingResult result,
                               @RequestParam Long manufacturerId,
                               @RequestParam Long categoryId,
                               @RequestParam(required = false) List<Long> supplierIds,
                               Model model) {        
        if (result.hasErrors()) {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("manufacturers", manufacturerService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }
        
        try {
            productService.save(product, manufacturerId, categoryId, supplierIds);
            return RedirectHelper.getEntityRedirect("products");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при сохранении продукта: " + e.getMessage());
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("manufacturers", manufacturerService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("manufacturers", manufacturerService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAllActive());
        return "products/form";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                               @Valid @ModelAttribute Product product,
                               BindingResult result,
                               @RequestParam Long manufacturerId,
                               @RequestParam Long categoryId,
                               @RequestParam(required = false) List<Long> supplierIds,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("manufacturers", manufacturerService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }
        
        try {
            productService.update(id, product, manufacturerId, categoryId, supplierIds);
            return RedirectHelper.getEntityRedirect("products");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении продукта: " + e.getMessage());
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("manufacturers", manufacturerService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "products/details";
    }

    @PostMapping("/logic-delete/{id}")
    public String logicDelete(@PathVariable Long id) {
        productService.logicDelete(id);
        return "redirect:/admin?entity=products";
    }

    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam(required = false) List<Long> ids,
                                @RequestParam String action) {
        if (ids != null && !ids.isEmpty()) {
            if ("physical".equals(action)) {
                productService.deleteAllByIds(ids);
            } else {
                productService.logicDeleteAllByIds(ids);
            }
        }
        return "redirect:/admin?entity=products";
    }

    @GetMapping("/deleted")
    public String deletedProducts(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> deletedPage = productService.findAllDeleted(pageable);
        model.addAttribute("deletedProducts", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());
        return "products/deleted";
    }

    @PostMapping("/restore/{id}")
    public String restoreProduct(@PathVariable Long id) {
        productService.restore(id);
        return RedirectHelper.getEntityRedirect("products", true);
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return RedirectHelper.getEntityRedirect("products", true);
    }
}


