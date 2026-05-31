package net.chen.legacyLand.gui.elements;

import net.chen.legacyLand.gui.GuiElement;
import net.chen.legacyLand.gui.GuiSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 滑块元素 - 通过 +/- 按钮调节数值
 * 占用三个连续槽位：[减号][当前值][加号]
 */
public class SliderElement implements GuiElement {

    private final String id;
    private final String displayName;
    private double value;
    private final double minValue;
    private final double maxValue;
    private double step;
    private boolean integerOnly;
    private int slot = -1;
    private Consumer<Double> onChange;
    private Consumer<Object> validator;

    public SliderElement(String id, String displayName, double minValue, double maxValue) {
        this.id = id;
        this.displayName = displayName;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = minValue;
        this.step = 1.0;
        this.integerOnly = false;
    }

    public SliderElement defaultValue(double value) {
        this.value = Math.max(minValue, Math.min(maxValue, value));
        return this;
    }

    public SliderElement step(double step) {
        this.step = step;
        return this;
    }

    public SliderElement integerOnly(boolean integerOnly) {
        this.integerOnly = integerOnly;
        return this;
    }

    public SliderElement onChange(Consumer<Double> callback) {
        this.onChange = callback;
        return this;
    }

    public SliderElement validator(Consumer<Object> validator) {
        this.validator = validator;
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

    private String formatValue() {
        if (integerOnly) return String.valueOf((int) value);
        return String.format("%.2f", value).replaceAll("\\.?0+$", "");
    }

    /** 渲染减号按钮（slot）、当前值（slot+1）、加号按钮（slot+2）到 Inventory */
    public void renderToInventory(Inventory inventory) {
        if (slot < 0 || slot + 2 >= inventory.getSize()) return;

        // 减号按钮
        ItemStack minus = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta minusMeta = minus.getItemMeta();
        if (minusMeta != null) {
            minusMeta.displayName(Component.text("- " + formatValue(step)).color(NamedTextColor.RED));
            minus.setItemMeta(minusMeta);
        }
        inventory.setItem(slot, minus);

        // 当前值显示
        ItemStack display = new ItemStack(Material.COMPARATOR);
        ItemMeta displayMeta = display.getItemMeta();
        if (displayMeta != null) {
            displayMeta.displayName(Component.text(displayName + ": " + formatValue()).color(NamedTextColor.YELLOW));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7范围: " + formatNum(minValue) + " - " + formatNum(maxValue)).color(NamedTextColor.GRAY));
            lore.add(Component.text("§7步长: " + formatNum(step)).color(NamedTextColor.GRAY));
            displayMeta.lore(lore);
            display.setItemMeta(displayMeta);
        }
        inventory.setItem(slot + 1, display);

        // 加号按钮
        ItemStack plus = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta plusMeta = plus.getItemMeta();
        if (plusMeta != null) {
            plusMeta.displayName(Component.text("+ " + formatValue(step)).color(NamedTextColor.GREEN));
            plus.setItemMeta(plusMeta);
        }
        inventory.setItem(slot + 2, plus);
    }

    private String formatValue(double v) {
        if (integerOnly) return String.valueOf((int) v);
        return String.format("%.2f", v).replaceAll("\\.?0+$", "");
    }

    private String formatNum(double v) {
        if (v == Math.floor(v)) return String.valueOf((int) v);
        return String.valueOf(v);
    }

    @Override
    public ItemStack render(GuiSession session) {
        // 主槽位渲染当前值显示（renderToInventory 负责完整三槽渲染）
        ItemStack display = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName + ": " + formatValue()).color(NamedTextColor.YELLOW));
            display.setItemMeta(meta);
        }
        return display;
    }

    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {
        // 由 GuiForm.handleClick 根据实际点击槽位调用此方法
        // slot = 减号槽，slot+1 = 显示槽，slot+2 = 加号槽
        // 此处无法直接知道点击的是哪个子槽，由 SliderAwareGuiForm 处理
    }

    /**
     * 处理减号点击
     */
    public void onDecrement(Player player, GuiSession session) {
        value = Math.max(minValue, value - step);
        if (integerOnly) value = Math.floor(value);
        session.getResult().put(id, value);
        if (onChange != null) onChange.accept(value);
        session.refresh();
    }

    /**
     * 处理加号点击
     */
    public void onIncrement(Player player, GuiSession session) {
        value = Math.min(maxValue, value + step);
        if (integerOnly) value = Math.floor(value);
        session.getResult().put(id, value);
        if (onChange != null) onChange.accept(value);
        session.refresh();
    }

    @Override
    public boolean validate(Player player) { return value >= minValue && value <= maxValue; }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number n) {
            this.value = Math.max(minValue, Math.min(maxValue, n.doubleValue()));
        }
    }

    @Override
    public Object getValue() { return value; }

    @Override
    public Consumer<Object> getValidator() { return validator; }
}
