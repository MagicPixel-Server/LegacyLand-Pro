package net.chen.legacyLand.gui.elements;

import net.chen.legacyLand.gui.GuiElement;
import net.chen.legacyLand.gui.GuiSession;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签元素 - 非交互式展示文本
 */
public class LabelElement implements GuiElement {

    private final String id;
    private final String displayName;
    private final List<String> lore;
    private Material material;
    private int slot = -1;

    public LabelElement(String id, String displayName) {
        this(id, displayName, Material.PAPER);
    }

    public LabelElement(String id, String displayName, Material material) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.lore = new ArrayList<>();
    }

    public LabelElement lore(String... lines) {
        this.lore.clear();
        this.lore.addAll(List.of(lines));
        return this;
    }

    public LabelElement material(Material material) {
        this.material = material;
        return this;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public int getSlot() { return slot; }

    @Override
    public void setSlot(int slot) { this.slot = slot; }

    @Override
    public ItemStack render(GuiSession session) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName));
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(Component.text(line));
            }
            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {
        // 非交互元素，不处理点击
    }

    @Override
    public boolean validate(Player player) { return true; }

    @Override
    public boolean isEnabled(Player player) { return false; }

    @Override
    public void setValue(Object value) {}

    @Override
    public Object getValue() { return null; }
}
