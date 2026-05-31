package net.chen.legacyLand.gui.elements;

import net.chen.legacyLand.gui.GuiElement;
import net.chen.legacyLand.gui.GuiSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 多选列表元素 - 带分页，已选项显示附魔光效
 */
public class MultiSelectElement<T> implements GuiElement {

    private static final int PAGE_SIZE = 45;
    private static final int PREV_SLOT = 48;
    private static final int INDICATOR_SLOT = 49;
    private static final int NEXT_SLOT = 50;

    private final String id;
    private final String displayName;
    private final List<T> options;
    private final Set<T> selectedValues = new HashSet<>();
    private int currentPage = 0;
    private int slot = -1;

    private Function<T, String> nameMapper;
    private Function<T, List<String>> loreMapper;
    private Function<T, Material> materialMapper;
    private Consumer<Set<T>> onChange;
    private Consumer<Object> validator;

    public MultiSelectElement(String id, String displayName, List<T> options) {
        this.id = id;
        this.displayName = displayName;
        this.options = new ArrayList<>(options);
        this.nameMapper = Object::toString;
        this.loreMapper = o -> new ArrayList<>();
        this.materialMapper = o -> Material.PAPER;
    }

    public MultiSelectElement<T> nameMapper(Function<T, String> mapper) {
        this.nameMapper = mapper;
        return this;
    }

    public MultiSelectElement<T> loreMapper(Function<T, List<String>> mapper) {
        this.loreMapper = mapper;
        return this;
    }

    public MultiSelectElement<T> materialMapper(Function<T, Material> mapper) {
        this.materialMapper = mapper;
        return this;
    }

    public MultiSelectElement<T> onChange(Consumer<Set<T>> callback) {
        this.onChange = callback;
        return this;
    }

    public MultiSelectElement<T> validator(Consumer<Object> validator) {
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

    private int totalPages() {
        return Math.max(1, (int) Math.ceil((double) options.size() / PAGE_SIZE));
    }

    public void renderToInventory(Inventory inventory) {
        inventory.clear();
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, options.size());

        for (int i = start; i < end; i++) {
            T option = options.get(i);
            boolean selected = selectedValues.contains(option);
            ItemStack item = new ItemStack(materialMapper.apply(option));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text((selected ? "✔ " : "") + nameMapper.apply(option))
                        .color(selected ? NamedTextColor.GREEN : NamedTextColor.WHITE));
                List<Component> lore = new ArrayList<>();
                for (String line : loreMapper.apply(option)) {
                    lore.add(Component.text(line).color(NamedTextColor.GRAY));
                }
                lore.add(Component.text(selected ? "§a点击取消选择" : "§e点击选择").color(selected ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
                meta.lore(lore);
                if (selected) {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                item.setItemMeta(meta);
            }
            inventory.setItem(i - start, item);
        }

        if (currentPage > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta m = prev.getItemMeta();
            if (m != null) { m.displayName(Component.text("§e上一页").color(NamedTextColor.YELLOW)); prev.setItemMeta(m); }
            inventory.setItem(PREV_SLOT, prev);
        }

        ItemStack indicator = new ItemStack(Material.PAPER);
        ItemMeta im = indicator.getItemMeta();
        if (im != null) {
            im.displayName(Component.text("第 " + (currentPage + 1) + " / " + totalPages() + " 页  已选 " + selectedValues.size()).color(NamedTextColor.GRAY));
            indicator.setItemMeta(im);
        }
        inventory.setItem(INDICATOR_SLOT, indicator);

        if (currentPage < totalPages() - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta m = next.getItemMeta();
            if (m != null) { m.displayName(Component.text("§e下一页").color(NamedTextColor.YELLOW)); next.setItemMeta(m); }
            inventory.setItem(NEXT_SLOT, next);
        }
    }

    public void handleInventoryClick(Player player, GuiSession session, int clickedSlot) {
        if (clickedSlot == PREV_SLOT && currentPage > 0) {
            currentPage--;
            session.refresh();
            return;
        }
        if (clickedSlot == NEXT_SLOT && currentPage < totalPages() - 1) {
            currentPage++;
            session.refresh();
            return;
        }
        if (clickedSlot == INDICATOR_SLOT) return;

        int index = currentPage * PAGE_SIZE + clickedSlot;
        if (index >= 0 && index < options.size()) {
            T option = options.get(index);
            if (selectedValues.contains(option)) {
                selectedValues.remove(option);
            } else {
                selectedValues.add(option);
            }
            session.getResult().put(id, new HashSet<>(selectedValues));
            if (onChange != null) onChange.accept(new HashSet<>(selectedValues));
            session.refresh();
        }
    }

    @Override
    public ItemStack render(GuiSession session) {
        return new ItemStack(Material.CHEST);
    }

    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {}

    @Override
    public boolean validate(Player player) { return true; }

    @Override
    public void setValue(Object value) {
        if (value instanceof Set<?> set) {
            selectedValues.clear();
            for (Object o : set) selectedValues.add((T) o);
        }
    }

    @Override
    public Object getValue() { return new HashSet<>(selectedValues); }

    @Override
    public Consumer<Object> getValidator() { return validator; }
}
