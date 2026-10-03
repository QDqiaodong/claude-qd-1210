package com.silver.smith.custom;

import com.silver.smith.common.BizException;
import com.silver.smith.common.Dict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomOrderService {

    private final CustomOrderRepository repo;

    public CustomOrderService(CustomOrderRepository repo) {
        this.repo = repo;
    }

    public List<CustomOrder> list() {
        return repo.findAll();
    }

    public CustomOrder create(CustomOrder in) {
        if (in.customer == null || in.customer.isBlank()) {
            throw new BizException("客户姓名不能为空");
        }
        if (in.design == null || in.design.isBlank()) {
            throw new BizException("定制图样不能为空");
        }
        if (in.deposit == null || in.deposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("定金不可为负");
        }
        if (in.total == null || in.total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("总价必须大于 0");
        }
        if (in.deposit.compareTo(in.total) > 0) {
            throw new BizException("定金不可超过总价");
        }
        CustomOrder o = new CustomOrder();
        o.customer = in.customer;
        o.design = in.design;
        o.deposit = in.deposit;
        o.total = in.total;
        o.status = (in.status == null || in.status.isBlank()) ? "待确认" : in.status;
        if (!Dict.isValidCustomStatus(o.status)) {
            throw new BizException("定制订单状态非法");
        }
        o.dueDate = in.dueDate;
        o.createdAt = LocalDateTime.now();
        o.updatedAt = LocalDateTime.now();
        return repo.save(o);
    }

    public CustomOrder update(Long id, CustomOrder in) {
        CustomOrder o = repo.findById(id).orElseThrow(() -> new BizException("定制订单不存在"));
        if (in.customer != null && !in.customer.isBlank()) {
            o.customer = in.customer;
        }
        if (in.design != null && !in.design.isBlank()) {
            o.design = in.design;
        }
        if (in.deposit != null) {
            if (in.deposit.compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException("定金不可为负");
            }
            o.deposit = in.deposit;
        }
        if (in.total != null) {
            if (in.total.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("总价必须大于 0");
            }
            o.total = in.total;
        }
        if ((o.deposit != null && o.total != null) && o.deposit.compareTo(o.total) > 0) {
            throw new BizException("定金不可超过总价");
        }
        if (in.dueDate != null) {
            o.dueDate = in.dueDate;
        }
        if (in.status != null && !in.status.isBlank()) {
            if (!Dict.canTransferCustom(o.status, in.status)) {
                throw new BizException("定制订单状态流转非法：已交付 / 退订为终态，且须按 待确认→制作中→待取→已交付 顺序流转");
            }
            o.status = in.status;
        }
        o.updatedAt = LocalDateTime.now();
        return repo.save(o);
    }

    /**
     * 状态流转：退订不退定金（定金保持原值，记已没收）；已交付 / 退订为终态不可变更。
     */
    public CustomOrder updateStatus(Long id, String to) {
        CustomOrder o = repo.findById(id).orElseThrow(() -> new BizException("定制订单不存在"));
        if (!Dict.canTransferCustom(o.status, to)) {
            throw new BizException("定制订单状态流转非法：已交付 / 退订为终态，且须按 待确认→制作中→待取→已交付 顺序流转");
        }
        o.status = to;
        o.updatedAt = LocalDateTime.now();
        return repo.save(o);
    }
}
