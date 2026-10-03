package com.silver.smith.mold;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 工具模具：锻造所用的錾子 / 砧 / 花丝模 / 抛光轮，记录磨损度与维护日期。 */
@Entity
@Table(name = "mold")
public class Mold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;
    public String type;
    public Integer wear;
    public LocalDate lastMaintain;
    public String status;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}
