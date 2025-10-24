package com.example.soratech.controller;

import com.example.soratech.model.ProductDetails;
import com.example.soratech.service.ProductDetailsService;
import com.example.soratech.service.ProductService;
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

@Controller
@RequestMapping("/productdetails")
public class ProductDetailsController {

    private final ProductDetailsService productDetailsService;
    private final ProductService productService;

    public ProductDetailsController(ProductDetailsService productDetailsService, ProductService productService) {
        this.productDetailsService = productDetailsService;
        this.productService = productService;
    }

    @GetMapping
    public String listProductDetails(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String query,
                                     @RequestParam(defaultValue = "id") String sortBy,
                                     @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductDetails> detailsPage;

        if (query != null && !query.isEmpty()) {
            detailsPage = productDetailsService.searchByCharacteristicName(query, pageable);
        } else {
            detailsPage = productDetailsService.findAllActive(pageable);
        }

        model.addAttribute("productDetails", detailsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", detailsPage.getTotalPages());
        model.addAttribute("query", query);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalElements", detailsPage.getTotalElements());
        return "productdetails/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("productDetails", new ProductDetails());
        model.addAttribute("products", productService.findAllActive());
        return "productdetails/form";
    }

    @PostMapping("/create")
    public String createProductDetails(@Valid @ModelAttribute ProductDetails productDetails,
                                       BindingResult result,
                                       @RequestParam Long productId,
                                       Model model) {
        if (result.hasErrors()) {
            model.addAttribute("productDetails", productDetails);
            model.addAttribute("products", productService.findAllActive());
            return "productdetails/form";
        }
        
        try {
            productDetails.setProduct(productService.findById(productId));
            productDetailsService.save(productDetails);
            return RedirectHelper.getEntityRedirect("productdetails");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при сохранении: " + e.getMessage());
            model.addAttribute("productDetails", productDetails);
            model.addAttribute("products", productService.findAllActive());
            return "productdetails/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ProductDetails productDetails = productDetailsService.findById(id);
        model.addAttribute("productDetails", productDetails);
        model.addAttribute("products", productService.findAllActive());
        return "productdetails/form";
    }

    @PostMapping("/edit/{id}")
    public String updateProductDetails(@PathVariable Long id,
                                       @Valid @ModelAttribute ProductDetails productDetails,
                                       BindingResult result,
                                       @RequestParam Long productId,
                                       Model model) {
        if (result.hasErrors()) {
            model.addAttribute("productDetails", productDetails);
            model.addAttribute("products", productService.findAllActive());
            return "productdetails/form";
        }
        
        try {
            productDetails.setProduct(productService.findById(productId));
            productDetailsService.update(id, productDetails);
            return RedirectHelper.getEntityRedirect("productdetails");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении: " + e.getMessage());
            model.addAttribute("productDetails", productDetails);
            model.addAttribute("products", productService.findAllActive());
            return "productdetails/form";
        }
    }

    @PostMapping("/logic-delete/{id}")
    public String logicDelete(@PathVariable Long id) {
        productDetailsService.logicDelete(id);
        return "redirect:/admin?entity=productdetails";
    }

    @PostMapping("/delete/{id}")
    public String deleteProductDetails(@PathVariable Long id) {
        productDetailsService.delete(id);
        return RedirectHelper.getEntityRedirect("productdetails", true);
    }

    @GetMapping("/deleted")
    public String deletedProductDetails(Model model,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDetails> deletedPage = productDetailsService.findAllDeleted(pageable);
        model.addAttribute("deletedProductDetails", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());
        return "productdetails/deleted";
    }

    @PostMapping("/restore/{id}")
    public String restoreProductDetails(@PathVariable Long id) {
        productDetailsService.restore(id);
        return RedirectHelper.getEntityRedirect("productdetails", true);
    }
}

