package net.chen.legacyLand.gui.forms;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import net.chen.legacyLand.gui.GuiForm;
import net.chen.legacyLand.gui.GuiManager;
import net.chen.legacyLand.gui.elements.ListElement;
import net.chen.legacyLand.nation.NationManager;
import net.chen.legacyLand.nation.diplomacy.DiplomacyManager;
import net.chen.legacyLand.nation.diplomacy.DiplomacyRelation;
import net.chen.legacyLand.nation.diplomacy.GuaranteeManager;
import net.chen.legacyLand.nation.diplomacy.RelationType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 外交总览界面 - 展示本国与所有其他国家的关系
 */
public class DiplomacyOverviewForm extends GuiForm {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    private final Player viewer;

    public DiplomacyOverviewForm(Player viewer) {
        super("diplomacy_overview", "§6外交关系总览", 6);
        this.viewer = viewer;
    }

    @Override
    protected void initialize() {
        Nation myNation = NationManager.getInstance().getPlayerNation(viewer);
        String myNationName = myNation != null ? myNation.getName() : null;

        Collection<Nation> allNations = TownyAPI.getInstance().getNations();
        List<String> nationNames = new ArrayList<>();
        for (Nation n : allNations) {
            if (!n.getName().equals(myNationName)) {
                nationNames.add(n.getName());
            }
        }

        DiplomacyManager diplomacy = DiplomacyManager.getInstance();
        GuaranteeManager guarantee = GuaranteeManager.getInstance();

        ListElement<String> list = new ListElement<>("nation_list", "国家列表", nationNames);

        list.nameMapper(nationName -> {
            if (myNationName == null) return "§7" + nationName;
            RelationType rel = diplomacy.getRelation(myNationName, nationName);
            return colorPrefix(rel) + nationName;
        });

        list.materialMapper(nationName -> {
            if (myNationName == null) return Material.GRAY_CONCRETE;
            RelationType rel = diplomacy.getRelation(myNationName, nationName);
            return materialFor(rel);
        });

        list.loreMapper(nationName -> {
            List<String> lore = new ArrayList<>();
            if (myNationName == null) {
                lore.add("§7你不在任何国家中");
                return lore;
            }
            RelationType rel = diplomacy.getRelation(myNationName, nationName);
            lore.add("§7关系: " + colorPrefix(rel) + rel.getDisplayName());

            // 建立时间
            List<DiplomacyRelation> relations = diplomacy.getNationRelations(myNationName);
            for (DiplomacyRelation r : relations) {
                if (r.involves(nationName)) {
                    lore.add("§8建立于 " + DATE_FMT.format(Instant.ofEpochMilli(r.getEstablishedTime())));
                    break;
                }
            }

            // 保护担保
            if (guarantee.hasGuarantee(nationName, myNationName)) {
                lore.add("§b受其保护担保");
            }

            lore.add("");
            lore.add("§e点击查看操作");
            return lore;
        });

        list.onSelect(nationName -> {
            GuiManager.getInstance().openForm(viewer, new DiplomacyActionForm(viewer, nationName));
        });

        addElement(list);
    }

    private String colorPrefix(RelationType rel) {
        return switch (rel) {
            case WAR -> "§c";
            case HOSTILE -> "§6";
            case NEUTRAL -> "§7";
            case FRIENDLY -> "§a";
            case ALLIANCE_DEFENSIVE -> "§9";
            case ALLIANCE_OFFENSIVE -> "§5";
            case TRADE_AGREEMENT -> "§e";
            case TECH_AGREEMENT -> "§b";
        };
    }

    private Material materialFor(RelationType rel) {
        return switch (rel) {
            case WAR -> Material.RED_CONCRETE;
            case HOSTILE -> Material.ORANGE_CONCRETE;
            case NEUTRAL -> Material.GRAY_CONCRETE;
            case FRIENDLY -> Material.LIME_CONCRETE;
            case ALLIANCE_DEFENSIVE -> Material.BLUE_CONCRETE;
            case ALLIANCE_OFFENSIVE -> Material.PURPLE_CONCRETE;
            case TRADE_AGREEMENT -> Material.YELLOW_CONCRETE;
            case TECH_AGREEMENT -> Material.CYAN_CONCRETE;
        };
    }
}
