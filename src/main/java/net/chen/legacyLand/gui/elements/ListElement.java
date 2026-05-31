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
import java.util.function.Function;

/**
 * 列表元素 - 带分页的单选列表
 * 占用整个 Inventory（除底部导航行），每页最多 45 个选项
 */
public class ListElement<T> implements GuiElement {

    private static final int PAGE_SIZE = 45;
    private static final int PREV_SLOT = 48;
    private static final int INDICATOR_SLOT = 49;
    private static final int NEXT_SLOT = 50;

    private final String id;
    private final String displayName;
    private final List<T> options;
    private T selectedValue;
    private int currentPage = 0;
    private int slot = -1;

    private Function<T, String> nameMapper;
    private Function<T, List<String>> loreMapper;
    private Function<T, Material> materialMapper;
    private Consumer<T> onSelect;
    private Consumer<Object> validator;

    public ListElement(String id, String displayName, List<T> options) {
        this.id = id;
        this.displayName = displayName;
        this.options = new ArrayList<>(options);
        this.nameMapper = Object::toString;
        this.loreMapper = o -> new ArrayList<>();
        this.materialMapper = o -> Material.PAPER;
    }

    public ListElement<T> nameMapper(Function<T, String> mapper) {
        this.nameMapper = mapper;
        return this;
    }

    public ListElement<T> loreMapper(Function<T, List<String>> mapper) {
        this.loreMapper = mapper;
        return this;
    }

    public ListElement<T> materialMapper(Function<T, Material> mapper) {
        this.materialMapper = mapper;
        return this;
    }

    public ListElement<T> onSelect(Consumer<T> callback) {
        this.onSelect = callback;
        return this;
    }

    public ListElement<T> validator(Consumer<Object> validator) {
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

    /** 将列表渲染到整个 Inventory（由 GuiForm 调用） */
    public void renderToInventory(Inventory inventory) {
        inventory.clear();
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, options.size());

        for (int i = start; i < end; i++) {
            T option = options.get(i);
            ItemStack item = new ItemStack(materialMapper.apply(option));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                boolean selected = option.equals(selectedValue);
                NamedTextColor color = selected ? NamedTextColor.GREEN : NamedTextColor.WHITE;
                meta.displayName(Component.text((selected ? "✔ " : "") + nameMapper.apply(option)).color(color));
                List<Component> lore = new ArrayList<>();
                for (String line : loreMapper.apply(option)) {
                    lore.add(Component.text(line).color(NamedTextColor.GRAY));
                }
                if (selected) lore.add(Component.text("§a已选择").color(NamedTextColor.GREEN));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(i - start, item);
        }

        // 导航按钮
        if (currentPage > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta m = prev.getItemMeta();
            if (m != null) { m.displayName(Component.text("§e上一页").color(NamedTextColor.YELLOW)); prev.setItemMeta(m); }
            inventory.setItem(PREV_SLOT, prev);
        }

        ItemStack indicator = new ItemStack(Material.PAPER);
        ItemMeta im = indicator.getItemMeta();
        if (im != null) {
            im.displayName(Component.text("第 " + (currentPage + 1) + " / " + totalPages() + " 页").color(NamedTextColor.GRAY));
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

    /** 处理 Inventory 点击（由 GuiForm 路由） */
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
            selectedValue = options.get(index);
            session.getResult().put(id, selectedValue);
            if (onSelect != null) onSelect.accept(selectedValue);
            session.refresh();
        }
    }

    @Override
    public ItemStack render(GuiSession session) {
        // 占位符，实际渲染由 renderToInventory 完成
        return new ItemStack(Material.PAPER);
    }

    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {}

    @Override
    public boolean validate(Player player) { return selectedValue != null; }

    @Override
    public void setValue(Object value) { this.selectedValue = (T) value; }

    @Override
    public Object getValue() { return selectedValue; }

    @Override
    public Consumer<Object> getValidator() { return validator; }
}
