package com.silver.smith.mold;

import com.silver.smith.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/molds")
public class MoldController {

    private final MoldService service;

    public MoldController(MoldService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Mold>> list() {
        return ApiResponse.success(service.list());
    }

    @PostMapping
    public ApiResponse<Mold> create(@RequestBody Mold in) {
        return ApiResponse.success(service.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<Mold> update(@PathVariable Long id, @RequestBody Mold in) {
        return ApiResponse.success(service.update(id, in));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<Mold> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.success(service.updateStatus(id, status));
    }
}
