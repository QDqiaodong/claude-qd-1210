package com.silver.smith.mold;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MoldRepository extends JpaRepository<Mold, Long> {
    List<Mold> findByStatus(String status);
}
