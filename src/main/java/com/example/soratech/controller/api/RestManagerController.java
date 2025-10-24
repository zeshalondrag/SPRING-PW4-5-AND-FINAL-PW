package com.example.soratech.controller.api;

import com.example.soratech.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "Manager API", description = "REST API для панели менеджера (требуется роль Менеджер или Администратор)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasAnyRole('Администратор', 'Менеджер')")
public class RestManagerController {

    private final ProductDetailsService productDetailsService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final CategoryService categoryService;
    private final ManufacturerService manufacturerService;
    private final UserService userService;

    public RestManagerController(ProductDetailsService productDetailsService,
                                 ProductService productService,
                                 OrderService orderService,
                                 ReviewService reviewService,
                                 CategoryService categoryService,
                                 ManufacturerService manufacturerService,
                                 UserService userService) {
        this.productDetailsService = productDetailsService;
        this.productService = productService;
        this.orderService = orderService;
        this.reviewService = reviewService;
        this.categoryService = categoryService;
        this.manufacturerService = manufacturerService;
        this.userService = userService;
    }

    // Get single item by ID
    @GetMapping("/{entity}/{id}")
    public ResponseEntity<?> getById(@PathVariable String entity, @PathVariable Long id) {
        try {
            Object result = switch (entity) {
                case "productdetails" -> productDetailsService.findById(id);
                case "products" -> productService.findById(id);
                case "orders" -> orderService.findById(id);
                case "reviews" -> reviewService.findById(id);
                default -> throw new IllegalArgumentException("Unknown entity: " + entity);
            };
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ProductDetails
    @GetMapping("/productdetails")
    public ResponseEntity<?> getProductDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean deleted) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<?> result;
            if (deleted) {
                result = productDetailsService.findAllDeleted(pageable);
            } else if (productId != null) {
                result = productDetailsService.findByProductId(productId, pageable);
            } else if (query != null && !query.isEmpty()) {
                result = productDetailsService.searchByCharacteristicName(query, pageable);
            } else {
                result = productDetailsService.findAllActive(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Products
    @GetMapping("/products")
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long manufacturerId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean deleted) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<?> result;
            if (deleted) {
                result = productService.findAllDeleted(pageable);
            } else if (query != null && !query.isEmpty()) {
                result = productService.searchByName(query, pageable);
            } else {
                result = productService.findAllActive(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Orders
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean deleted) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<?> result;
            if (deleted) {
                result = orderService.findAllDeleted(pageable);
            } else if (userId != null) {
                result = orderService.findByUserId(userId, pageable);
            } else if (status != null && !status.isEmpty()) {
                result = orderService.findByStatus(status, pageable);
            } else {
                result = orderService.findAllActive(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Reviews
    @GetMapping("/reviews")
    public ResponseEntity<?> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean deleted) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<?> result;
            if (deleted) {
                result = reviewService.findAllDeleted(pageable);
            } else if (productId != null) {
                result = reviewService.findByProductId(productId, pageable);
            } else if (userId != null) {
                result = reviewService.findByUserId(userId, pageable);
            } else if (rating != null) {
                result = reviewService.findByRating(rating, pageable);
            } else {
                result = reviewService.findAllActive(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Helper endpoints for select options
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(@RequestParam(defaultValue = "1000") int size) {
        try {
            Pageable pageable = PageRequest.of(0, size);
            Page<?> result = categoryService.findAllActive(pageable);
            return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<?> getManufacturers(@RequestParam(defaultValue = "1000") int size) {
        try {
            Pageable pageable = PageRequest.of(0, size);
            Page<?> result = manufacturerService.findAllActive(pageable);
            return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestParam(defaultValue = "1000") int size) {
        try {
            Pageable pageable = PageRequest.of(0, size);
            Page<?> result = userService.findAllActive(pageable);
            return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

