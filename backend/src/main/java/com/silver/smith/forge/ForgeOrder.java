package com.silver.smith.forge;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 锻造工单：使用某块银料 + 某副模具锻造指定银饰，经历开料→锻打→抛光→质检→完成（可返工）。 */
@Entity
@Table(name = "forge_order")
public class ForgeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String product;
    public Long silverId;
    public Long moldId;
    public BigDecimal planWeight;
    public BigDecimal actualWeight;
    public String status;
    public String craft;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}
