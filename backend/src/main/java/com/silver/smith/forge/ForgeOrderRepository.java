package com.silver.smith.forge;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForgeOrderRepository extends JpaRepository<ForgeOrder, Long> {
    List<ForgeOrder> findByStatus(String status);
}
