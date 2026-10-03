package com.silver.smith.custom;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 客户定制：客户图样定制银饰，含定金、总价与交付状态流转。 */
@Entity
@Table(name = "custom_order")
public class CustomOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String customer;
    public String design;
    public BigDecimal deposit;
    public BigDecimal total;
    public String status;
    public LocalDate dueDate;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}
