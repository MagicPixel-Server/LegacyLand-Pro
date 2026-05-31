package net.chen.legacyLand.gui.elements;

import net.chen.legacyLand.gui.GuiElement;
import net.chen.legacyLand.gui.GuiSession;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

/**
 * 分隔符元素 - 视觉分隔，不可交互
 */
public class SeparatorElement implements GuiElement {

    private final String id;
    private Material material;
    private int slot = -1;

    public SeparatorElement(String id) {
        this(id, Material.GRAY_STAINED_GLASS_PANE);
    }

    public SeparatorElement(String id, Material material) {
        this.id = id;
        this.material = material;
    }

    public SeparatorElement material(Material material) {
        this.material = material;
        return this;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return " "; }

    @Override
    public int getSlot() { return slot; }

    @Override
    public void setSlot(int slot) { this.slot = slot; }

    @Override
    public ItemStack render(GuiSession session) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {}

    @Override
    public boolean validate(Player player) { return true; }

    @Override
    public boolean isEnabled(Player player) { return false; }

    @Override
    public void setValue(Object value) {}

    @Override
    public Object getValue() { return null; }

    @Override
    public Consumer<Object> getValidator() { return null; }
}
