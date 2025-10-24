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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "Admin API", description = "REST API для панели администратора (требуется роль Администратор)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin")
public class RestAdminController {

    private final UserService userService;
    private final RoleService roleService;
    private final CategoryService categoryService;
    private final ManufacturerService manufacturerService;
    private final SupplierService supplierService;
    private final ProductDetailsService productDetailsService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;

    public RestAdminController(UserService userService,
                               RoleService roleService,
                               CategoryService categoryService,
                               ManufacturerService manufacturerService,
                               SupplierService supplierService,
                               ProductDetailsService productDetailsService,
                               ProductService productService,
                               OrderService orderService,
                               ReviewService reviewService) {
        this.userService = userService;
        this.roleService = roleService;
        this.categoryService = categoryService;
        this.manufacturerService = manufacturerService;
        this.supplierService = supplierService;
        this.productDetailsService = productDetailsService;
        this.productService = productService;
        this.orderService = orderService;
        this.reviewService = reviewService;
    }

    // Get single item by ID
    @GetMapping("/{entity}/{id}")
    public ResponseEntity<?> getById(@PathVariable String entity, @PathVariable Long id) {
        try {
            Object result = switch (entity) {
                case "users" -> userService.findById(id);
                case "roles" -> roleService.findById(id);
                case "categories" -> categoryService.findById(id);
                case "manufacturers" -> manufacturerService.findById(id);
                case "suppliers" -> supplierService.findById(id);
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

    // Users
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String searchType,
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
                result = userService.findAllDeleted(pageable);
            } else if (query != null && !query.isEmpty()) {
                result = "email".equals(searchType) ? 
                    userService.searchByEmail(query, pageable) : 
                    userService.searchByName(query, pageable);
            } else {
                result = userService.findAllActive(pageable);
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

    // Roles
    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
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
                result = roleService.findAllDeleted(pageable);
            } else if (query != null && !query.isEmpty()) {
                result = roleService.searchByName(query, pageable);
            } else {
                result = roleService.findAllActive(pageable);
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

    // Categories
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
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
                result = categoryService.findAllDeleted(pageable);
            } else if (query != null && !query.isEmpty()) {
                result = categoryService.searchByName(query, pageable);
            } else {
                result = categoryService.findAllActive(pageable);
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

    // Manufacturers
    @GetMapping("/manufacturers")
    public ResponseEntity<?> getManufacturers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String country,
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
                result = manufacturerService.findAllDeleted(pageable);
            } else if (query != null && !query.isEmpty()) {
                result = manufacturerService.searchByName(query, pageable);
            } else if (country != null && !country.isEmpty()) {
                result = manufacturerService.filterByCountry(country, pageable);
            } else {
                result = manufacturerService.findAllActive(pageable);
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

    // Suppliers
    @GetMapping("/suppliers")
    public ResponseEntity<?> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
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
                result = supplierService.findAllDeleted(pageable);
            } else if (query != null && !query.isEmpty()) {
                result = supplierService.searchByName(query, pageable);
            } else {
                result = supplierService.findAllActive(pageable);
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
            } else if (categoryId != null) {
                result = productService.filterByCategory(categoryId, pageable);
            } else if (manufacturerId != null) {
                result = productService.filterByManufacturer(manufacturerId, pageable);
            } else if (minPrice != null || maxPrice != null) {
                result = productService.filterByPriceRange(minPrice, maxPrice, pageable);
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
}

