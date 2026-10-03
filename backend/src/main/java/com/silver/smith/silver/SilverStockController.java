package com.silver.smith.silver;

import com.silver.smith.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/silvers")
public class SilverStockController {

    private final SilverStockService service;

    public SilverStockController(SilverStockService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SilverStock>> list() {
        return ApiResponse.success(service.list());
    }

    @PostMapping
    public ApiResponse<SilverStock> create(@RequestBody SilverStock in) {
        return ApiResponse.success(service.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<SilverStock> update(@PathVariable Long id, @RequestBody SilverStock in) {
        return ApiResponse.success(service.update(id, in));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<SilverStock> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.success(service.updateStatus(id, status));
    }
}
