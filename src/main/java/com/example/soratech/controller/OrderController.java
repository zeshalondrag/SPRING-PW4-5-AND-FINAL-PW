package com.example.soratech.controller;

import com.example.soratech.model.Order;
import com.example.soratech.service.OrderService;
import com.example.soratech.service.UserService;
import com.example.soratech.util.RedirectHelper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String listOrders(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) Long userId,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                            @RequestParam(defaultValue = "createdAt") String sortBy,
                            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> ordersPage;

        if (userId != null) {
            ordersPage = orderService.findByUserId(userId, pageable);
        } else if (status != null && !status.isEmpty()) {
            ordersPage = orderService.findByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            ordersPage = orderService.findByDateRange(startDate, endDate, pageable);
        } else {
            ordersPage = orderService.findAllActive(pageable);
        }

        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("userId", userId);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalElements", ordersPage.getTotalElements());
        model.addAttribute("users", userService.findAllActive());
        return "orders/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("users", userService.findAllActive());
        return "orders/form";
    }

    @PostMapping("/create")
    public String createOrder(@Valid @ModelAttribute Order order,
                             BindingResult result,
                             @RequestParam Long userId,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("order", order);
            model.addAttribute("users", userService.findAllActive());
            return "orders/form";
        }
        
        try {
            orderService.save(order, userId);
            return RedirectHelper.getEntityRedirect("orders");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при сохранении заказа: " + e.getMessage());
            model.addAttribute("order", order);
            model.addAttribute("users", userService.findAllActive());
            return "orders/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("users", userService.findAllActive());
        return "orders/form";
    }

    @PostMapping("/edit/{id}")
    public String updateOrder(@PathVariable Long id,
                             @Valid @ModelAttribute Order order,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("order", order);
            model.addAttribute("users", userService.findAllActive());
            return "orders/form";
        }
        
        try {
            orderService.update(id, order);
            return RedirectHelper.getEntityRedirect("orders");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении заказа: " + e.getMessage());
            model.addAttribute("order", order);
            model.addAttribute("users", userService.findAllActive());
            return "orders/form";
        }
    }

    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return RedirectHelper.getEntityRedirect("orders");
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        return "orders/details";
    }

    @PostMapping("/logic-delete/{id}")
    public String logicDelete(@PathVariable Long id) {
        orderService.logicDelete(id);
        return RedirectHelper.getEntityRedirect("orders");
    }

    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam(required = false) List<Long> ids,
                                @RequestParam String action) {
        if (ids != null && !ids.isEmpty()) {
            if ("physical".equals(action)) {
                orderService.deleteAllByIds(ids);
            } else {
                orderService.logicDeleteAllByIds(ids);
            }
        }
        return RedirectHelper.getEntityRedirect("orders");
    }

    @GetMapping("/deleted")
    public String deletedOrders(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> deletedPage = orderService.findAllDeleted(pageable);
        model.addAttribute("deletedOrders", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());
        return "orders/deleted";
    }

    @PostMapping("/restore/{id}")
    public String restoreOrder(@PathVariable Long id) {
        orderService.restore(id);
        return RedirectHelper.getEntityRedirect("orders", true);
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return RedirectHelper.getEntityRedirect("orders", true);
    }
}


