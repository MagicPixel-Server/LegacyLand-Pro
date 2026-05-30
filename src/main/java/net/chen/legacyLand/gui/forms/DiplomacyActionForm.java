package net.chen.legacyLand.gui.forms;

import net.chen.legacyLand.gui.GuiForm;
import net.chen.legacyLand.gui.GuiManager;
import net.chen.legacyLand.gui.elements.ButtonElement;
import net.chen.legacyLand.gui.elements.LabelElement;
import net.chen.legacyLand.gui.elements.SeparatorElement;
import net.chen.legacyLand.nation.NationManager;
import net.chen.legacyLand.nation.NationPermission;
import net.chen.legacyLand.nation.diplomacy.DiplomacyManager;
import net.chen.legacyLand.nation.diplomacy.GuaranteeManager;
import net.chen.legacyLand.nation.diplomacy.RelationType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 外交操作界面 - 对单个国家执行外交操作
 */
public class DiplomacyActionForm extends GuiForm {

    private final Player viewer;
    private final String targetNation;

    public DiplomacyActionForm(Player viewer, String targetNation) {
        super("diplomacy_action", "外交操作: " + targetNation, 4);
        this.viewer = viewer;
        this.targetNation = targetNation;
    }

    @Override
    protected void initialize() {
        NationManager nationManager = NationManager.getInstance();
        DiplomacyManager diplomacy = DiplomacyManager.getInstance();
        GuaranteeManager guarantee = GuaranteeManager.getInstance();

        com.palmergames.bukkit.towny.object.Nation myNation = nationManager.getPlayerNation(viewer);
        String myNationName = myNation != null ? myNation.getName() : null;

        RelationType current = myNationName != null
                ? diplomacy.getRelation(myNationName, targetNation)
                : RelationType.NEUTRAL;

        boolean hasDeclareWar = nationManager.hasPermission(viewer, NationPermission.DECLARE_WAR);
        boolean hasFormAlliance = nationManager.hasPermission(viewer, NationPermission.FORM_ALLIANCE);
        boolean hasProposeDiplomacy = nationManager.hasPermission(viewer, NationPermission.PROPOSE_DIPLOMACY);
        boolean isLeader = myNationName != null &&
                nationManager.getPlayerRole(myNationName, viewer.getUniqueId()).isLeader();

        // slot 0: 目标国名称标签
        // LabelElement uses Adventure API — no § codes in displayName or lore
        LabelElement titleLabel = new LabelElement("title", targetNation, Material.NAME_TAG);
        titleLabel.lore("当前关系: " + current.getDisplayName());
        titleLabel.setSlot(0);
        addElement(titleLabel);

        // slot 8: 返回按钮
        // ButtonElement uses legacy API — § codes are fine
        ButtonElement backBtn = new ButtonElement("back", Material.ARROW, "§7← 返回");
        backBtn.setSlot(8);
        backBtn.onClick(p -> GuiManager.getInstance().openForm(p, new DiplomacyOverviewForm(p)));
        addElement(backBtn);

        // 分隔行 slot 9-17
        for (int i = 9; i <= 17; i++) {
            SeparatorElement sep = new SeparatorElement("sep_" + i);
            sep.setSlot(i);
            addElement(sep);
        }

        // slot 18: 宣战
        addActionButton("war", Material.IRON_SWORD, "§c宣战",
                List.of("§7将与 " + targetNation + " 进入战争状态"),
                18,
                hasDeclareWar && myNationName != null && current != RelationType.WAR,
                !hasDeclareWar ? "§c无宣战权限" : current == RelationType.WAR ? "§c已处于战争状态" : null,
                p -> {
                    if (myNationName == null) { p.sendMessage("§c你不在任何国家中！"); return; }
                    if (!diplomacy.declareWar(myNationName, targetNation)) {
                        p.sendMessage("§c宣战失败！");
                        return;
                    }
                    p.sendMessage("§c已向 " + targetNation + " 宣战！");
                    GuiManager.getInstance().openForm(p, new DiplomacyActionForm(p, targetNation));
                });

        // slot 19: 求和
        addActionButton("peace", Material.WHITE_BANNER, "§a求和",
                List.of("§7与 " + targetNation + " 恢复中立关系"),
                19,
                hasDeclareWar && myNationName != null && current == RelationType.WAR,
                !hasDeclareWar ? "§c无求和权限" : current != RelationType.WAR ? "§c当前未处于战争状态" : null,
                p -> {
                    if (myNationName == null) { p.sendMessage("§c你不在任何国家中！"); return; }
                    if (!diplomacy.makePeace(myNationName, targetNation)) {
                        p.sendMessage("§c求和失败！");
                        return;
                    }
                    p.sendMessage("§a已与 " + targetNation + " 和平！");
                    GuiManager.getInstance().openForm(p, new DiplomacyActionForm(p, targetNation));
                });

        // slot 20: 结盟（防御）
        addActionButton("ally_def", Material.BLUE_BANNER, "§9结盟（防御）",
                List.of("§7与 " + targetNation + " 建立共同防御同盟"),
                20,
                hasFormAlliance && myNationName != null && !current.isAlliance() && current != RelationType.WAR,
                !hasFormAlliance ? "§c无结盟权限" : current.isAlliance() ? "§c已存在同盟关系" : current == RelationType.WAR ? "§c战争中无法结盟" : null,
                p -> {
                    if (myNationName == null) { p.sendMessage("§c你不在任何国家中！"); return; }
                    if (!diplomacy.formAlliance(myNationName, targetNation, true)) {
                        p.sendMessage("§c结盟失败！");
                        return;
                    }
                    p.sendMessage("§9已与 " + targetNation + " 建立共同防御同盟！");
                    GuiManager.getInstance().openForm(p, new DiplomacyActionForm(p, targetNation));
                });

        // slot 21: 结盟（进攻）
        addActionButton("ally_off", Material.PURPLE_BANNER, "§5结盟（进攻）",
                List.of("§7与 " + targetNation + " 建立共同进攻同盟"),
                21,
                hasFormAlliance && myNationName != null && !current.isAlliance() && current != RelationType.WAR,
                !hasFormAlliance ? "§c无结盟权限" : current.isAlliance() ? "§c已存在同盟关系" : current == RelationType.WAR ? "§c战争中无法结盟" : null,
                p -> {
                    if (myNationName == null) { p.sendMessage("§c你不在任何国家中！"); return; }
                    if (!diplomacy.formAlliance(myNationName, targetNation, false)) {
                        p.sendMessage("§c结盟失败！");
                        return;
                    }
                    p.sendMessage("§5已与 " + targetNation + " 建立共同进攻同盟！");
                    GuiManager.getInstance().openForm(p, new DiplomacyActionForm(p, targetNation));
                });

        // slot 22: 贸易协议
        addActionButton("trade", Material.GOLD_INGOT, "§e贸易协议",
                List.of("§7与 " + targetNation + " 签订贸易协议"),
                22,
                hasProposeDiplomacy && myNationName != null && current != RelationType.TRADE_AGREEMENT && current != RelationType.WAR,
                !hasProposeDiplomacy ? "§c无外交提案权限" : current == RelationType.TRADE_AGREEMENT ? "§c已有贸易协议" : current == RelationType.WAR ? "§c战争中无法签订" : null,
                p -> {
                    if (myNationName == null) { p.sendMessage("§c你不在任何国家中！"); return; }
                    if (!diplomacy.signTradeAgreement(myNationName, targetNation)) {
                        p.sendMessage("§c签订贸易协议失败！");
                        return;
                    }
                    p.sendMessage("§e已与 " + targetNation + " 签订贸易协议！");
                    GuiManager.getInstance().openForm(p, new DiplomacyActionForm(p, targetNation));
                });

        // slot 23: 科技协议
        addActionButton("tech", Material.BOOK, "§b科技协议",
                List.of("§7与 " + targetNation + " 签订科技协议"),
                23,
                hasProposeDiplomacy && myNationName != null && current != RelationType.TECH_AGREEMENT && current != RelationType.WAR,
                !hasProposeDiplomacy ? "§c无外交提案权限" : current == RelationType.TECH_AGREEMENT ? "§c已有科技协议" : current == RelationType.WAR ? "§c战争中无法签订" : null,
                p -> {
                    if (myNationName == null) { p.sendMessage("§c你不在任何国家中！"); return; }
                    if (!diplomacy.signTechAgreement(myNationName, targetNation)) {
                        p.sendMessage("§c签订科技协议失败！");
                        return;
                    }
                    p.sendMessage("§b已与 " + targetNation + " 签订科技协议！");
                    GuiManager.getInstance().openForm(p, new DiplomacyActionForm(p, targetNation));
                });

        // slot 24: 设为中立
        addActionButton("neutral", Material.GRAY_BANNER, "§7设为中立",
                List.of("§7将与 " + targetNation + " 的关系重置为中立"),
                24,
                hasProposeDiplomacy && myNationName != null && current != RelationType.NEUTRAL && current != RelationType.WAR,
                !hasProposeDiplomacy ? "§c无外交提案权限" : current == RelationType.NEUTRAL ? "§c已是中立关系" : current == RelationType.WAR ? "§c战争中请先求和" : null,
                p -> {
                    if (myNationName == null) { p.sendMessage("§c你不在任何国家中！"); return; }
                    if (!diplomacy.removeRelation(myNationName, targetNation)) {
                        p.sendMessage("§c操作失败！");
                        return;
                    }
                    p.sendMessage("§7已将与 " + targetNation + " 的关系设为中立。");
                    GuiManager.getInstance().openForm(p, new DiplomacyActionForm(p, targetNation));
                });

        // slot 25: 保护担保
        boolean alreadyGuaranteed = myNationName != null && guarantee.hasGuarantee(myNationName, targetNation);
        addActionButton("guarantee", Material.SHIELD, "§b保护担保",
                List.of("§7为 " + targetNation + " 提供保护担保", "§8每小时消耗 500 金币 + 10 贸易经验"),
                25,
                isLeader && myNationName != null && !alreadyGuaranteed,
                !isLeader ? "§c仅领导者可操作" : alreadyGuaranteed ? "§c已提供保护担保" : null,
                p -> {
                    if (myNationName == null) { p.sendMessage("§c你不在任何国家中！"); return; }
                    if (!guarantee.establishGuarantee(myNationName, targetNation)) {
                        p.sendMessage("§c建立保护担保失败！");
                        return;
                    }
                    p.sendMessage("§b已为 " + targetNation + " 建立保护担保！");
                    GuiManager.getInstance().openForm(p, new DiplomacyActionForm(p, targetNation));
                });
    }

    private void addActionButton(String id, Material material, String name,
                                  List<String> lore, int slot,
                                  boolean enabled, String disabledReason,
                                  Consumer<Player> action) {
        if (enabled) {
            ButtonElement btn = new ButtonElement(id, material, name, new ArrayList<>(lore));
            btn.setSlot(slot);
            btn.onClick(action);
            addElement(btn);
        } else {
            List<String> disabledLore = new ArrayList<>(lore);
            if (disabledReason != null) disabledLore.add(disabledReason);
            ButtonElement btn = new ButtonElement(id, Material.GRAY_STAINED_GLASS_PANE,
                    "§8" + stripColor(name), disabledLore);
            btn.setSlot(slot);
            addElement(btn);
        }
    }

    private String stripColor(String s) {
        return s.replaceAll("§[0-9a-fk-or]", "");
    }
}
