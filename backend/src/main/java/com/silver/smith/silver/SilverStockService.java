package com.silver.smith.silver;

import com.silver.smith.common.BizException;
import com.silver.smith.common.Dict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SilverStockService {

    private final SilverStockRepository repo;

    public SilverStockService(SilverStockRepository repo) {
        this.repo = repo;
    }

    public List<SilverStock> list() {
        return repo.findAll();
    }

    public SilverStock create(SilverStock in) {
        if (!Dict.isValidSilverAlloy(in.alloy)) {
            throw new BizException("银料合金必须是：999 / 925 / 苗银");
        }
        if (in.weight == null || in.weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("银料重量必须大于 0 克");
        }
        if (!Dict.isValidSilverSource(in.source)) {
            throw new BizException("银料来源必须是：采购 / 回收");
        }
        SilverStock s = new SilverStock();
        s.alloy = in.alloy;
        s.weight = in.weight;
        s.source = in.source;
        s.status = (in.status == null || in.status.isBlank()) ? "在用" : in.status;
        if (!Dict.isValidSilverStatus(s.status)) {
            throw new BizException("银料状态非法");
        }
        s.purityCheck = (in.purityCheck == null) ? Boolean.FALSE : in.purityCheck;
        s.createdAt = LocalDateTime.now();
        s.updatedAt = LocalDateTime.now();
        return repo.save(s);
    }

    public SilverStock update(Long id, SilverStock in) {
        SilverStock s = repo.findById(id).orElseThrow(() -> new BizException("银料不存在"));
        if (in.alloy != null) {
            if (!Dict.isValidSilverAlloy(in.alloy)) {
                throw new BizException("银料合金必须是：999 / 925 / 苗银");
            }
            s.alloy = in.alloy;
        }
        if (in.weight != null) {
            if (in.weight.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("银料重量必须大于 0 克");
            }
            s.weight = in.weight;
        }
        if (in.source != null && !in.source.isBlank()) {
            if (!Dict.isValidSilverSource(in.source)) {
                throw new BizException("银料来源必须是：采购 / 回收");
            }
            s.source = in.source;
        }
        if (in.purityCheck != null) {
            s.purityCheck = in.purityCheck;
        }
        if (in.status != null && !in.status.isBlank()) {
            if (!Dict.canTransferSilver(s.status, in.status)) {
                throw new BizException("银料状态流转非法：报废不可恢复，且须按 在用→熔炼中→已成材→报废 顺序流转");
            }
            s.status = in.status;
        }
        s.updatedAt = LocalDateTime.now();
        return repo.save(s);
    }

    /** 状态流转：报废为终态不可恢复。 */
    public SilverStock updateStatus(Long id, String to) {
        SilverStock s = repo.findById(id).orElseThrow(() -> new BizException("银料不存在"));
        if (!Dict.canTransferSilver(s.status, to)) {
            throw new BizException("银料状态流转非法：报废不可恢复，且须按 在用→熔炼中→已成材→报废 顺序流转");
        }
        s.status = to;
        s.updatedAt = LocalDateTime.now();
        return repo.save(s);
    }
}
