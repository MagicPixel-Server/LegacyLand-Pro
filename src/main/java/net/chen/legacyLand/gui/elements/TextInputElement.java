package net.chen.legacyLand.gui.elements;

import io.papermc.paper.dialog.*;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.Getter;
import lombok.Setter;
import net.chen.legacyLand.gui.GuiElement;
import net.chen.legacyLand.gui.GuiSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本输入元素
 * 使用 Paper Dialog API 实现文本输入
 */
@Getter
@Setter
public class TextInputElement implements GuiElement {

    private final String id;
    private String displayName;
    private List<String> lore;
    private Material icon;
    private String value;
    private String defaultValue;
    private String prompt;
    private int maxLength;
    private boolean multiline;
    private int slot = -1;

    public TextInputElement(String id, String displayName, String prompt) {
        this.id = id;
        this.displayName = displayName;
        this.prompt = prompt;
        this.icon = Material.WRITABLE_BOOK;
        this.lore = new ArrayList<>();
        this.maxLength = 64;
        this.multiline = false;
    }

    public TextInputElement defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        return this;
    }

    public TextInputElement maxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public TextInputElement multiline(boolean multiline) {
        this.multiline = multiline;
        return this;
    }

    public TextInputElement icon(Material icon) {
        this.icon = icon;
        return this;
    }

    public TextInputElement lore(String... lines) {
        this.lore = List.of(lines);
        return this;
    }

    @Override
    public ItemStack render(GuiSession session) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName));

            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(Component.text(line));
            }

            loreComponents.add(Component.empty());
            if (value != null && !value.isEmpty()) {
                loreComponents.add(Component.text("§7当前值: §f" + value));
            } else {
                loreComponents.add(Component.text("§7当前值: §8(未设置)"));
            }
            loreComponents.add(Component.empty());
            loreComponents.add(Component.text("§e点击输入"));

            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {
        // 创建提交回调
        DialogActionCallback submitCallback = (response, audience) -> {
            String inputValue = response.getText(id);
            if (inputValue != null) {
                this.value = inputValue;

                // 重新打开 GUI
                session.refresh();
            }
        };

        // 创建取消回调
        DialogActionCallback cancelCallback = (response, audience) -> {
            // 重新打开 GUI
            session.refresh();
        };

        // 设置回调选项
        ClickCallback.Options cbOptions = ClickCallback.Options.builder()
                .uses(1)
                .lifetime(Duration.ofMinutes(10))
                .build();

        // 创建按钮
        ActionButton submitButton = ActionButton.builder(Component.text("§a提交"))
                .width(150)
                .action(DialogAction.customClick(submitCallback, cbOptions))
                .build();

        ActionButton cancelButton = ActionButton.builder(Component.text("§c取消"))
                .width(150)
                .action(DialogAction.customClick(cancelCallback, cbOptions))
                .build();

        // 构建对话框基础
        DialogBase.Builder baseBuilder = DialogBase.builder(Component.text(displayName))
                .canCloseWithEscape(false)
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(List.of(DialogBody.plainMessage(Component.text(prompt), 350)));

        // 添加输入字段
        io.papermc.paper.registry.data.dialog.input.TextDialogInput.Builder inputBuilder = DialogInput.text(id, Component.text("输入内容"))
                .width(250)
                .maxLength(maxLength);

        if (multiline) {
            inputBuilder.multiline(null);
        }

        if (value != null && !value.isEmpty()) {
            inputBuilder.initial(value);
        } else if (defaultValue != null && !defaultValue.isEmpty()) {
            inputBuilder.initial(defaultValue);
        }

        baseBuilder.inputs(List.of(inputBuilder.build()));

        DialogBase base = baseBuilder.build();

        // 创建并显示对话框
        Dialog dialog = Dialog.create(factory ->
                factory.empty().base(base).type(DialogType.confirmation(submitButton, cancelButton)));

        player.showDialog(dialog);
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
    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    /**
     * 设置元素的值
     *
     * @param value 新值
     */
    @Override
    public void setValue(Object value) {
        if (value != null) {
            this.value = value.toString();
        }
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
