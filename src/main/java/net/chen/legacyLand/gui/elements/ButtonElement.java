package net.chen.legacyLand.gui.elements;

import net.chen.legacyLand.gui.GuiElement;
import net.chen.legacyLand.gui.GuiSession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ButtonElement implements GuiElement {
    private final String id;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private Consumer<Player> onClick;
    private Consumer<Player> onRightClick;
    private Consumer<Player> onShiftClick;
    private int slot = -1;

    public ButtonElement(String id, Material material, String displayName) {
        this(id, material, displayName, new ArrayList<>());
    }

    public ButtonElement(String id, Material material, String displayName, List<String> lore) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
    }

    public ButtonElement onClick(Consumer<Player> callback) {
        this.onClick = callback;
        return this;
    }

    public ButtonElement onRightClick(Consumer<Player> callback) {
        this.onRightClick = callback;
        return this;
    }

    public ButtonElement onShiftClick(Consumer<Player> callback) {
        this.onShiftClick = callback;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * 获取元素的显示名称
     *
     * @return 显示名称
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public ItemStack render(GuiSession session) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {
        if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            if (onShiftClick != null) {
                onShiftClick.accept(player);
            }
        } else if (clickType == ClickType.RIGHT) {
            if (onRightClick != null) {
                onRightClick.accept(player);
            }
        } else {
            if (onClick != null) {
                onClick.accept(player);
            }
        }
    }

    /**
     * 验证元素的当前值
     *
     * @param player 目标玩家
     * @return 是否通过验证
     */
    @Override
    public boolean validate(Player player) {
        return false;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public boolean isVisible(Player player) {
        return true;
    }

    @Override
    public boolean isEnabled(Player player) {
        return true;
    }

    @Override
    public Consumer<Object> getValidator() {
        return null;
    }

    @Override
    public void setValue(Object value) {
        // Buttons don't have values
    }

    @Override
    public Object getValue() {
        return null;
    }
}
