package com.silver.smith.forge;

import com.silver.smith.common.BizException;
import com.silver.smith.common.Dict;
import com.silver.smith.mold.Mold;
import com.silver.smith.mold.MoldRepository;
import com.silver.smith.silver.SilverStock;
import com.silver.smith.silver.SilverStockRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForgeOrderService {

    private final ForgeOrderRepository repo;
    private final SilverStockRepository silverRepo;
    private final MoldRepository moldRepo;

    public ForgeOrderService(ForgeOrderRepository repo,
                             SilverStockRepository silverRepo,
                             MoldRepository moldRepo) {
        this.repo = repo;
        this.silverRepo = silverRepo;
        this.moldRepo = moldRepo;
    }

    public List<ForgeOrder> list() {
        return repo.findAll();
    }

    /** 校验被引用的银料：必须存在、纯度已检、且非报废/熔炼中。 */
    private void checkSilver(Long silverId) {
        if (silverId == null) {
            throw new BizException("锻造工单必须指定银料");
        }
        SilverStock s = silverRepo.findById(silverId)
                .orElseThrow(() -> new BizException("银料不存在"));
        if (!Boolean.TRUE.equals(s.purityCheck)) {
            throw new BizException("银料纯度未检，不可用于锻造");
        }
        if (Dict.SILVER_UNUSABLE.contains(s.status)) {
            throw new BizException("银料状态不可用（报废/熔炼中），不可用于锻造");
        }
    }

    /** 校验被引用的模具：必须存在、非报废、非待修。 */
    private void checkMold(Long moldId) {
        if (moldId == null) {
            throw new BizException("锻造工单必须指定模具");
        }
        Mold m = moldRepo.findById(moldId)
                .orElseThrow(() -> new BizException("模具不存在"));
        if ("报废".equals(m.status)) {
            throw new BizException("模具已报废，不可用于锻造");
        }
        if ("待修".equals(m.status)) {
            throw new BizException("模具待修，不可用于生产");
        }
    }

    public ForgeOrder create(ForgeOrder in) {
        if (!Dict.isValidForgeProduct(in.product)) {
            throw new BizException("锻造产品必须是：手镯 / 戒指 / 项链 / 耳环 / 吊坠");
        }
        if (in.planWeight == null || in.planWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("计划重量必须大于 0 克");
        }
        if (!Dict.isValidForgeCraft(in.craft)) {
            throw new BizException("锻造工艺必须是：錾刻 / 花丝 / 烧蓝");
        }
        checkSilver(in.silverId);
        checkMold(in.moldId);
        ForgeOrder o = new ForgeOrder();
        o.product = in.product;
        o.silverId = in.silverId;
        o.moldId = in.moldId;
        o.planWeight = in.planWeight;
        o.actualWeight = (in.actualWeight == null) ? null : in.actualWeight;
        o.status = (in.status == null || in.status.isBlank()) ? "开料" : in.status;
        if (!Dict.isValidForgeStatus(o.status)) {
            throw new BizException("锻造工单状态非法");
        }
        o.craft = in.craft;
        o.createdAt = LocalDateTime.now();
        o.updatedAt = LocalDateTime.now();
        return repo.save(o);
    }

    public ForgeOrder update(Long id, ForgeOrder in) {
        ForgeOrder o = repo.findById(id).orElseThrow(() -> new BizException("锻造工单不存在"));
        if (in.product != null) {
            if (!Dict.isValidForgeProduct(in.product)) {
                throw new BizException("锻造产品必须是：手镯 / 戒指 / 项链 / 耳环 / 吊坠");
            }
            o.product = in.product;
        }
        if (in.planWeight != null) {
            if (in.planWeight.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("计划重量必须大于 0 克");
            }
            o.planWeight = in.planWeight;
        }
        if (in.actualWeight != null) {
            o.actualWeight = in.actualWeight;
        }
        if (in.craft != null && !in.craft.isBlank()) {
            if (!Dict.isValidForgeCraft(in.craft)) {
                throw new BizException("锻造工艺必须是：錾刻 / 花丝 / 烧蓝");
            }
            o.craft = in.craft;
        }
        if (in.silverId != null) {
            checkSilver(in.silverId);
            o.silverId = in.silverId;
        }
        if (in.moldId != null) {
            checkMold(in.moldId);
            o.moldId = in.moldId;
        }
        o.updatedAt = LocalDateTime.now();
        return repo.save(o);
    }

    /** 状态流转：完成前必须经历质检且已填实际重量；返工只能回到锻打；完成为终态。 */
    public ForgeOrder updateStatus(Long id, String to) {
        ForgeOrder o = repo.findById(id).orElseThrow(() -> new BizException("锻造工单不存在"));
        if (!Dict.isValidForgeStatus(to)) {
            throw new BizException("锻造工单状态非法");
        }
        String from = o.status;
        if ("完成".equals(to)) {
            if (!"质检".equals(from)) {
                throw new BizException("完成前必须经历质检（需先将状态流转到质检）");
            }
            if (o.actualWeight == null) {
                throw new BizException("完成前必须填写实际重量");
            }
            o.status = to;
            o.updatedAt = LocalDateTime.now();
            return repo.save(o);
        }
        if (!Dict.canTransferForge(from, to)) {
            throw new BizException("锻造工单状态流转非法：须按 开料→锻打→抛光→质检→完成 顺序，且返工只能回到锻打");
        }
        o.status = to;
        o.updatedAt = LocalDateTime.now();
        return repo.save(o);
    }
}
