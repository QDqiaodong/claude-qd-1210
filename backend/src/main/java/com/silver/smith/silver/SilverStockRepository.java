package com.silver.smith.silver;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SilverStockRepository extends JpaRepository<SilverStock, Long> {
    List<SilverStock> findByStatus(String status);
}
