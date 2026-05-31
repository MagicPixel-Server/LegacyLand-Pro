package net.chen.legacyLand.gui.elements;

import lombok.Getter;
import lombok.Setter;
import net.chen.legacyLand.gui.GuiElement;
import net.chen.legacyLand.gui.GuiSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 开关按钮元素
 * 提供两种状态的切换功能（开启/关闭）
 */
@Getter
@Setter
public class ToggleElement implements GuiElement {

    private final String id;
    private final String displayName;
    private boolean value;
    private int slot = -1;

    // 自定义材质
    private Material enabledMaterial = Material.LIME_WOOL;
    private Material disabledMaterial = Material.RED_WOOL;

    // 自定义显示文本
    private String enabledText = "已开启";
    private String disabledText = "已关闭";

    // 描述文本
    private List<String> description = new ArrayList<>();

    // 点击回调
    private Consumer<Boolean> onToggle;

    // 验证器
    private Consumer<Object> validator;

    /**
     * 构造函数
     * @param id 元素 ID
     * @param displayName 显示名称
     * @param defaultValue 默认值
     */
    public ToggleElement(String id, String displayName, boolean defaultValue) {
        this.id = id;
        this.displayName = displayName;
        this.value = defaultValue;
    }

    /**
     * 设置开启状态的材质
     * @param material 材质
     * @return 当前实例（链式调用）
     */
    public ToggleElement enabledMaterial(Material material) {
        this.enabledMaterial = material;
        return this;
    }

    /**
     * 设置关闭状态的材质
     * @param material 材质
     * @return 当前实例（链式调用）
     */
    public ToggleElement disabledMaterial(Material material) {
        this.disabledMaterial = material;
        return this;
    }

    /**
     * 设置开启状态的文本
     * @param text 文本
     * @return 当前实例（链式调用）
     */
    public ToggleElement enabledText(String text) {
        this.enabledText = text;
        return this;
    }

    /**
     * 设置关闭状态的文本
     * @param text 文本
     * @return 当前实例（链式调用）
     */
    public ToggleElement disabledText(String text) {
        this.disabledText = text;
        return this;
    }

    /**
     * 添加描述行
     * @param line 描述文本
     * @return 当前实例（链式调用）
     */
    public ToggleElement addDescription(String line) {
        this.description.add(line);
        return this;
    }

    /**
     * 设置切换回调
     * @param callback 回调函数
     * @return 当前实例（链式调用）
     */
    public ToggleElement onToggle(Consumer<Boolean> callback) {
        this.onToggle = callback;
        return this;
    }

    /**
     * 设置验证器
     * @param validator 验证器
     * @return 当前实例（链式调用）
     */
    public ToggleElement validator(Consumer<Object> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public ItemStack render(GuiSession session) {
        // 根据当前状态选择材质
        Material material = value ? enabledMaterial : disabledMaterial;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // 设置显示名称
        String statusText = value ? enabledText : disabledText;
        NamedTextColor color = value ? NamedTextColor.GREEN : NamedTextColor.RED;
        meta.displayName(Component.text(displayName + " - " + statusText).color(color));

        // 设置描述
        List<Component> lore = new ArrayList<>();
        for (String line : description) {
            lore.add(Component.text(line).color(NamedTextColor.GRAY));
        }
        lore.add(Component.empty());
        lore.add(Component.text("点击切换状态").color(NamedTextColor.YELLOW));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {
        // 切换状态
        value = !value;

        // 执行回调
        if (onToggle != null) {
            onToggle.accept(value);
        }

        // 刷新会话显示
        session.refresh();
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
    public void setValue(Object value) {
        if (value instanceof Boolean) {
            this.value = (Boolean) value;
        } else if (value instanceof String) {
            this.value = Boolean.parseBoolean((String) value);
        } else {
            throw new IllegalArgumentException("ToggleElement only accepts Boolean or String values");
        }
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Consumer<Object> getValidator() {
        return validator;
    }
}
