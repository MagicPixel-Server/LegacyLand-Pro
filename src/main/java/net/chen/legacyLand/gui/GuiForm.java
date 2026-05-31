package net.chen.legacyLand.gui;

import lombok.Getter;
import lombok.Setter;
import net.chen.legacyLand.gui.elements.ListElement;
import net.chen.legacyLand.gui.elements.MultiSelectElement;
import net.chen.legacyLand.gui.elements.SliderElement;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * GUI 表单抽象基类
 * 所有表单的基础类，提供通用的表单管理功能
 */
@Getter
@Setter
public abstract class GuiForm {

    /**
     * 表单 ID
     */
    private final String formId;

    /**
     * 表单标题
     */
    private String title;

    /**
     * 表单大小（行数，1-6）
     */
    private int size = 6;

    /**
     * 表单元素列表
     */
    private final List<GuiElement> elements = new ArrayList<>();

    /**
     * 元素映射（ID -> 元素）
     */
    private final Map<String, GuiElement> elementMap = new HashMap<>();

    /**
     * 提交回调
     */
    private Consumer<FormResult> onSubmit;

    /**
     * 取消回调
     */
    private Consumer<FormResult> onCancel;

    /**
     * 构造函数
     * @param formId 表单 ID
     * @param title 表单标题
     */
    protected GuiForm(String formId, String title) {
        this.formId = formId;
        this.title = title;
    }

    /**
     * 构造函数（默认 6 行）
     * @param formId 表单 ID
     * @param title 表单标题
     * @param size 表单大小（行数）
     */
    protected GuiForm(String formId, String title, int size) {
        this.formId = formId;
        this.title = title;
        this.size = Math.max(1, Math.min(6, size));
    }

    /**
     * 添加元素
     * @param element 元素
     */
    public void addElement(GuiElement element) {
        elements.add(element);
        elementMap.put(element.getId(), element);
    }

    /**
     * 通过 ID 获取元素
     * @param elementId 元素 ID
     * @return 元素（可能为 null）
     */
    public GuiElement getElement(String elementId) {
        return elementMap.get(elementId);
    }

    /**
     * 移除元素
     * @param elementId 元素 ID
     */
    public void removeElement(String elementId) {
        GuiElement element = elementMap.remove(elementId);
        if (element != null) {
            elements.remove(element);
        }
    }

    /**
     * 渲染表单
     * @param player 玩家
     * @param session 会话
     */
    public void render(Player player, GuiSession session) {
        // 创建 Inventory
        Inventory inventory = Bukkit.createInventory(null, size * 9, Component.text(title));

        // 渲染所有元素
        for (GuiElement element : elements) {
            if (!element.isVisible(player)) continue;

            // 全屏列表元素自行渲染整个 Inventory
            if (element instanceof ListElement<?> list) {
                list.renderToInventory(inventory);
                continue;
            }
            if (element instanceof MultiSelectElement<?> multi) {
                multi.renderToInventory(inventory);
                continue;
            }
            // SliderElement 自行渲染三个槽
            if (element instanceof SliderElement slider) {
                slider.renderToInventory(inventory);
                continue;
            }

            ItemStack item = element.render(session);
            int slot = element.getSlot();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, item);
            } else {
                inventory.addItem(item);
            }
        }

        // 更新会话
        session.setCurrentInventory(inventory);
        GuiManager.getInstance().registerInventory(inventory, session);

        // 打开 GUI
        player.openInventory(inventory);
    }

    /**
     * 处理元素点击
     * @param player 玩家
     * @param session 会话
     * @param slot 槽位
     * @param clickType 点击类型
     */
    public void handleClick(Player player, GuiSession session, int slot, ClickType clickType) {
        for (GuiElement element : elements) {
            // 全屏列表元素处理所有槽位点击
            if (element instanceof ListElement<?> list) {
                list.handleInventoryClick(player, session, slot);
                return;
            }
            if (element instanceof MultiSelectElement<?> multi) {
                multi.handleInventoryClick(player, session, slot);
                return;
            }
            // SliderElement 占三个槽：slot（减）、slot+1（显示）、slot+2（加）
            if (element instanceof SliderElement slider && slider.getSlot() >= 0) {
                int base = slider.getSlot();
                if (slot == base) {
                    slider.onDecrement(player, session);
                    return;
                } else if (slot == base + 2) {
                    slider.onIncrement(player, session);
                    return;
                } else if (slot == base + 1) {
                    return;
                }
                continue;
            }
            if (element.getSlot() == slot && element.isEnabled(player)) {
                element.onClick(player, session, clickType);
                return;
            }
        }
    }

    /**
     * 提交表单
     * @param session 会话
     */
    public void submit(GuiSession session) {
        // 收集所有元素的值
        FormResult result = session.getResult();
        for (GuiElement element : elements) {
            Object value = element.getValue();
            if (value != null) {
                result.put(element.getId(), value);
            }
        }

        // 标记为已提交
        result.markSubmitted();

        // 调用提交回调
        if (onSubmit != null) {
            onSubmit.accept(result);
        }

        // 关闭会话
        Player player = Bukkit.getPlayer(session.getPlayerUuid());
        if (player != null) {
            GuiManager.getInstance().closeSession(player);
        }
    }

    /**
     * 取消表单
     * @param session 会话
     */
    public void cancel(GuiSession session) {
        FormResult result = session.getResult();
        result.markCancelled();

        // 调用取消回调
        if (onCancel != null) {
            onCancel.accept(result);
        }

        // 关闭会话
        Player player = Bukkit.getPlayer(session.getPlayerUuid());
        if (player != null) {
            GuiManager.getInstance().closeSession(player);
        }
    }

    /**
     * 验证表单
     * @param result 表单结果
     * @return 验证是否通过
     */
    public boolean validate(FormResult result) {
        for (GuiElement element : elements) {
            Consumer<Object> validator = element.getValidator();
            if (validator != null) {
                try {
                    validator.accept(element.getValue());
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 初始化表单（子类实现）
     * 用于添加元素和设置回调
     */
    protected abstract void initialize();
}
