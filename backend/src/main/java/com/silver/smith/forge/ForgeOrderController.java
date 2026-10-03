package com.silver.smith.forge;

import com.silver.smith.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forges")
public class ForgeOrderController {

    private final ForgeOrderService service;

    public ForgeOrderController(ForgeOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ForgeOrder>> list() {
        return ApiResponse.success(service.list());
    }

    @PostMapping
    public ApiResponse<ForgeOrder> create(@RequestBody ForgeOrder in) {
        return ApiResponse.success(service.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<ForgeOrder> update(@PathVariable Long id, @RequestBody ForgeOrder in) {
        return ApiResponse.success(service.update(id, in));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<ForgeOrder> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.success(service.updateStatus(id, status));
    }
}
