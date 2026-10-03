package com.silver.smith.mold;

import com.silver.smith.common.BizException;
import com.silver.smith.common.Dict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MoldService {

    private final MoldRepository repo;

    public MoldService(MoldRepository repo) {
        this.repo = repo;
    }

    public List<Mold> list() {
        return repo.findAll();
    }

    /** 磨损度 >80 且状态为「良好」时自动置为「待修」。 */
    private String resolveStatus(int wear, String status) {
        if (wear > 80 && "良好".equals(status)) {
            return "待修";
        }
        return status;
    }

    public Mold create(Mold in) {
        if (in.name == null || in.name.isBlank()) {
            throw new BizException("模具名称不能为空");
        }
        if (!Dict.isValidMoldType(in.type)) {
            throw new BizException("模具类型必须是：錾子 / 砧 / 花丝模 / 抛光轮");
        }
        if (in.wear == null) {
            throw new BizException("磨损度必填");
        }
        if (in.wear < 0 || in.wear > 100) {
            throw new BizException("磨损度必须在 0-100 之间");
        }
        String status = (in.status == null || in.status.isBlank()) ? "良好" : in.status;
        if (!Dict.isValidMoldStatus(status)) {
            throw new BizException("模具状态非法");
        }
        status = resolveStatus(in.wear, status);
        Mold m = new Mold();
        m.name = in.name;
        m.type = in.type;
        m.wear = in.wear;
        m.lastMaintain = in.lastMaintain;
        m.status = status;
        m.createdAt = LocalDateTime.now();
        m.updatedAt = LocalDateTime.now();
        return repo.save(m);
    }

    public Mold update(Long id, Mold in) {
        Mold m = repo.findById(id).orElseThrow(() -> new BizException("模具不存在"));
        if (in.name != null && !in.name.isBlank()) {
            m.name = in.name;
        }
        if (in.type != null && !in.type.isBlank()) {
            if (!Dict.isValidMoldType(in.type)) {
                throw new BizException("模具类型必须是：錾子 / 砧 / 花丝模 / 抛光轮");
            }
            m.type = in.type;
        }
        if (in.wear != null) {
            if (in.wear < 0 || in.wear > 100) {
                throw new BizException("磨损度必须在 0-100 之间");
            }
            m.wear = in.wear;
        }
        if (in.lastMaintain != null) {
            m.lastMaintain = in.lastMaintain;
        }
        if (in.status != null && !in.status.isBlank()) {
            if (!Dict.isValidMoldStatus(in.status)) {
                throw new BizException("模具状态非法");
            }
            m.status = in.status;
        }
        m.status = resolveStatus(m.wear, m.status);
        m.updatedAt = LocalDateTime.now();
        return repo.save(m);
    }

    /** 状态流转：报废不可再用。 */
    public Mold updateStatus(Long id, String to) {
        Mold m = repo.findById(id).orElseThrow(() -> new BizException("模具不存在"));
        if (!Dict.isValidMoldStatus(to)) {
            throw new BizException("模具状态非法");
        }
        if ("报废".equals(m.status)) {
            throw new BizException("模具已报废，不可再变更状态");
        }
        m.status = to;
        m.updatedAt = LocalDateTime.now();
        return repo.save(m);
    }
}
