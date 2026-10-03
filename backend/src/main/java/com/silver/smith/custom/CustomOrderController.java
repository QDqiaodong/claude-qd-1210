package com.silver.smith.custom;

import com.silver.smith.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customs")
public class CustomOrderController {

    private final CustomOrderService service;

    public CustomOrderController(CustomOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<CustomOrder>> list() {
        return ApiResponse.success(service.list());
    }

    @PostMapping
    public ApiResponse<CustomOrder> create(@RequestBody CustomOrder in) {
        return ApiResponse.success(service.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomOrder> update(@PathVariable Long id, @RequestBody CustomOrder in) {
        return ApiResponse.success(service.update(id, in));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<CustomOrder> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.success(service.updateStatus(id, status));
    }
}
