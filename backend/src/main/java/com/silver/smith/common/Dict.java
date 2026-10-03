package com.silver.smith.common;

import java.util.List;
import java.util.Map;

/**
 * 银饰锻造坊业务词典与规则参数表：银料合金 / 来源 / 状态流转、锻造工单产品 / 工艺 / 状态流转、
 * 客户定制状态流转、工具模具类型 / 状态。规则集中在此，便于出题时被要求调整或扩展。
 */
public final class Dict {

    private Dict() {
    }

    // ---------------- 银料 silver_stock ----------------
    /** 999 / 925 / 苗银 */
    public static final List<String> SILVER_ALLOY = List.of("999", "925", "苗银");

    /** 采购 / 回收 */
    public static final List<String> SILVER_SOURCE = List.of("采购", "回收");

    /** 在用 / 熔炼中 / 已成材 / 报废 */
    public static final List<String> SILVER_STATUS = List.of("在用", "熔炼中", "已成材", "报废");

    /** 不可用（不可被锻造工单引用）的银料状态：报废、熔炼中 */
    public static final List<String> SILVER_UNUSABLE = List.of("报废", "熔炼中");

    /** 银料状态机：在用→熔炼中→已成材→报废；报废为终态不可恢复；熔炼中不可回退到在用 */
    public static final Map<String, List<String>> SILVER_FLOW = Map.of(
            "在用", List.of("熔炼中", "报废"),
            "熔炼中", List.of("已成材", "报废"),
            "已成材", List.of("报废"),
            "报废", List.of());

    // ---------------- 锻造工单 forge_order ----------------
    /** 手镯 / 戒指 / 项链 / 耳环 / 吊坠 */
    public static final List<String> FORGE_PRODUCT = List.of("手镯", "戒指", "项链", "耳环", "吊坠");

    /** 开料 / 锻打 / 抛光 / 质检 / 完成 / 返工 */
    public static final List<String> FORGE_STATUS = List.of("开料", "锻打", "抛光", "质检", "完成", "返工");

    /** 錾刻 / 花丝 / 烧蓝 */
    public static final List<String> FORGE_CRAFT = List.of("錾刻", "花丝", "烧蓝");

    /** 锻造工单状态机：开料→锻打→抛光→质检→完成；任意阶段可返工，返工只能回到锻打；完成为终态 */
    public static final Map<String, List<String>> FORGE_FLOW = Map.of(
            "开料", List.of("锻打"),
            "锻打", List.of("抛光", "返工"),
            "抛光", List.of("质检", "返工"),
            "质检", List.of("完成", "返工"),
            "返工", List.of("锻打"),
            "完成", List.of());

    // ---------------- 客户定制 custom_order ----------------
    /** 待确认 / 制作中 / 待取 / 已交付 / 退订 */
    public static final List<String> CUSTOM_STATUS = List.of("待确认", "制作中", "待取", "已交付", "退订");

    /** 定制状态机：待确认→制作中→待取→已交付；任一环节可退订；已交付、退订为终态 */
    public static final Map<String, List<String>> CUSTOM_FLOW = Map.of(
            "待确认", List.of("制作中", "退订"),
            "制作中", List.of("待取", "退订"),
            "待取", List.of("已交付", "退订"),
            "已交付", List.of(),
            "退订", List.of());

    // ---------------- 工具模具 mold ----------------
    /** 錾子 / 砧 / 花丝模 / 抛光轮 */
    public static final List<String> MOLD_TYPE = List.of("錾子", "砧", "花丝模", "抛光轮");

    /** 良好 / 待修 / 报废 */
    public static final List<String> MOLD_STATUS = List.of("良好", "待修", "报废");

    // ---------------- 取值方法 ----------------

    public static boolean isValidSilverAlloy(String a) {
        return a != null && SILVER_ALLOY.contains(a);
    }

    public static boolean isValidSilverSource(String s) {
        return s != null && SILVER_SOURCE.contains(s);
    }

    public static boolean isValidSilverStatus(String s) {
        return s != null && SILVER_STATUS.contains(s);
    }

    public static boolean isValidForgeProduct(String p) {
        return p != null && FORGE_PRODUCT.contains(p);
    }

    public static boolean isValidForgeStatus(String s) {
        return s != null && FORGE_STATUS.contains(s);
    }

    public static boolean isValidForgeCraft(String c) {
        return c != null && FORGE_CRAFT.contains(c);
    }

    public static boolean isValidCustomStatus(String s) {
        return s != null && CUSTOM_STATUS.contains(s);
    }

    public static boolean isValidMoldType(String t) {
        return t != null && MOLD_TYPE.contains(t);
    }

    public static boolean isValidMoldStatus(String s) {
        return s != null && MOLD_STATUS.contains(s);
    }

    /**
     * 银料状态流转校验：报废不可恢复；否则按 SILVER_FLOW 前进。
     */
    public static boolean canTransferSilver(String from, String to) {
        if (!isValidSilverStatus(from) || !isValidSilverStatus(to)) {
            return false;
        }
        List<String> next = SILVER_FLOW.get(from);
        return next != null && next.contains(to);
    }

    /**
     * 锻造工单状态流转校验（不含「完成」的特殊前置校验，该前置在 Service 中单独处理）。
     */
    public static boolean canTransferForge(String from, String to) {
        if (!isValidForgeStatus(from) || !isValidForgeStatus(to)) {
            return false;
        }
        List<String> next = FORGE_FLOW.get(from);
        return next != null && next.contains(to);
    }

    /**
     * 定制订单状态流转校验：已交付、退订为终态不可变更。
     */
    public static boolean canTransferCustom(String from, String to) {
        if (!isValidCustomStatus(from) || !isValidCustomStatus(to)) {
            return false;
        }
        List<String> next = CUSTOM_FLOW.get(from);
        return next != null && next.contains(to);
    }
}
