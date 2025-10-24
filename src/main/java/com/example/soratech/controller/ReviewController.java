package com.example.soratech.controller;

import com.example.soratech.model.Review;
import com.example.soratech.service.ReviewService;
import com.example.soratech.service.ProductService;
import com.example.soratech.service.UserService;
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

import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, ProductService productService, UserService userService) {
        this.reviewService = reviewService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping
    public String listReviews(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) Long productId,
                             @RequestParam(required = false) Long userId,
                             @RequestParam(required = false) Integer rating,
                             @RequestParam(defaultValue = "createdAt") String sortBy,
                             @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviewsPage;

        if (productId != null) {
            reviewsPage = reviewService.findByProductId(productId, pageable);
        } else if (userId != null) {
            reviewsPage = reviewService.findByUserId(userId, pageable);
        } else if (rating != null) {
            reviewsPage = reviewService.findByRating(rating, pageable);
        } else {
            reviewsPage = reviewService.findAllActive(pageable);
        }

        model.addAttribute("reviews", reviewsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewsPage.getTotalPages());
        model.addAttribute("productId", productId);
        model.addAttribute("userId", userId);
        model.addAttribute("rating", rating);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalElements", reviewsPage.getTotalElements());
        model.addAttribute("products", productService.findAllActive());
        model.addAttribute("users", userService.findAllActive());
        return "reviews/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("review", new Review());
        model.addAttribute("products", productService.findAllActive());
        model.addAttribute("users", userService.findAllActive());
        return "reviews/form";
    }

    @PostMapping("/create")
    public String createReview(@Valid @ModelAttribute Review review,
                              BindingResult result,
                              @RequestParam Long productId,
                              @RequestParam Long userId,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("review", review);
            model.addAttribute("products", productService.findAllActive());
            model.addAttribute("users", userService.findAllActive());
            return "reviews/form";
        }
        
        try {
            reviewService.save(review, productId, userId);
            return RedirectHelper.getEntityRedirect("reviews");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при сохранении отзыва: " + e.getMessage());
            model.addAttribute("review", review);
            model.addAttribute("products", productService.findAllActive());
            model.addAttribute("users", userService.findAllActive());
            return "reviews/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Review review = reviewService.findById(id);
        model.addAttribute("review", review);
        model.addAttribute("products", productService.findAllActive());
        model.addAttribute("users", userService.findAllActive());
        return "reviews/form";
    }

    @PostMapping("/edit/{id}")
    public String updateReview(@PathVariable Long id,
                              @Valid @ModelAttribute Review review,
                              BindingResult result,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("review", review);
            model.addAttribute("products", productService.findAllActive());
            model.addAttribute("users", userService.findAllActive());
            return "reviews/form";
        }
        
        try {
            reviewService.update(id, review);
            return RedirectHelper.getEntityRedirect("reviews");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении отзыва: " + e.getMessage());
            model.addAttribute("review", review);
            model.addAttribute("products", productService.findAllActive());
            model.addAttribute("users", userService.findAllActive());
            return "reviews/form";
        }
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        Review review = reviewService.findById(id);
        model.addAttribute("review", review);
        return "reviews/details";
    }

    @PostMapping("/logic-delete/{id}")
    public String logicDelete(@PathVariable Long id) {
        reviewService.logicDelete(id);
        return RedirectHelper.getEntityRedirect("reviews");
    }

    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam(required = false) List<Long> ids,
                                @RequestParam String action) {
        if (ids != null && !ids.isEmpty()) {
            if ("physical".equals(action)) {
                reviewService.deleteAllByIds(ids);
            } else {
                reviewService.logicDeleteAllByIds(ids);
            }
        }
        return RedirectHelper.getEntityRedirect("reviews");
    }

    @GetMapping("/deleted")
    public String deletedReviews(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> deletedPage = reviewService.findAllDeleted(pageable);
        model.addAttribute("deletedReviews", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());
        return "reviews/deleted";
    }

    @PostMapping("/restore/{id}")
    public String restoreReview(@PathVariable Long id) {
        reviewService.restore(id);
        return RedirectHelper.getEntityRedirect("reviews", true);
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
        return RedirectHelper.getEntityRedirect("reviews", true);
    }
}


