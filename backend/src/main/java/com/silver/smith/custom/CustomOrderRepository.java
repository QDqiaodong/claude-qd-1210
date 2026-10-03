package com.silver.smith.custom;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {
    List<CustomOrder> findByStatus(String status);
}
