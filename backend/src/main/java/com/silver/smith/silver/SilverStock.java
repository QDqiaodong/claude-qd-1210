package com.silver.smith.silver;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 银料台账：登记入库的银料（合金、重量、来源、状态、纯度是否检测）。 */
@Entity
@Table(name = "silver_stock")
public class SilverStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String alloy;
    public BigDecimal weight;
    public String source;
    public String status;
    public Boolean purityCheck;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}
