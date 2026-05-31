package net.chen.legacyLand.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * GUI 元素接口
 * 所有 GUI 元素的基础接口，定义了元素的核心行为
 */
public interface GuiElement {

    /**
     * 获取元素的唯一标识符
     * @return 元素 ID
     */
    String getId();

    /**
     * 获取元素的显示名称
     * @return 显示名称
     */
    String getDisplayName();

    /**
     * 渲染元素为物品堆
     * @param session 当前会话（包含玩家信息）
     * @return 渲染后的物品堆
     */
    ItemStack render(GuiSession session);

    /**
     * 处理玩家点击事件
     * @param player 点击的玩家
     * @param session 当前会话
     * @param clickType 点击类型
     */
    void onClick(Player player, GuiSession session, ClickType clickType);

    /**
     * 验证元素的当前值
     * @param player 目标玩家
     * @return 是否通过验证
     */
    boolean validate(Player player);

    /**
     * 获取元素的槽位位置
     * @return 槽位索引（-1 表示自动分配）
     */
    default int getSlot() {
        return -1;
    }

    /**
     * 设置元素的槽位位置
     * @param slot 槽位索引
     */
    void setSlot(int slot);

    /**
     * 检查元素是否可见
     * @param player 目标玩家
     * @return 是否可见
     */
    default boolean isVisible(Player player) {
        return true;
    }

    /**
     * 检查元素是否可交互
     * @param player 目标玩家
     * @return 是否可交互
     */
    default boolean isEnabled(Player player) {
        return true;
    }

    /**
     * 获取元素的验证器
     * @return 验证器（可为 null）
     */
    default Consumer<Object> getValidator() {
        return null;
    }

    /**
     * 设置元素的值
     * @param value 新值
     */
    void setValue(Object value);

    /**
     * 获取元素的当前值
     * @return 当前值
     */
    Object getValue();

    /**
     * 检查元素是否可点击
     * @return 是否可点击
     */
    default boolean isClickable() {
        return true;
    }
}
