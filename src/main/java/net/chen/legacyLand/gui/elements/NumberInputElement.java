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
 * 数字输入元素
 * 使用 Paper Dialog API 实现数字输入
 */
@Getter
@Setter
public class NumberInputElement implements GuiElement {

    private final String id;
    private String displayName;
    private List<String> lore;
    private Material icon;
    private Double value;
    private Double defaultValue;
    private String prompt;
    private Double minValue;
    private Double maxValue;
    private boolean integerOnly;
    private int slot = -1;

    public NumberInputElement(String id, String displayName, String prompt) {
        this.id = id;
        this.displayName = displayName;
        this.prompt = prompt;
        this.icon = Material.PAPER;
        this.lore = new ArrayList<>();
        this.integerOnly = false;
    }

    public NumberInputElement defaultValue(double defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        return this;
    }

    public NumberInputElement minValue(double minValue) {
        this.minValue = minValue;
        return this;
    }

    public NumberInputElement maxValue(double maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public NumberInputElement integerOnly(boolean integerOnly) {
        this.integerOnly = integerOnly;
        return this;
    }

    public NumberInputElement icon(Material icon) {
        this.icon = icon;
        return this;
    }

    public NumberInputElement lore(String... lines) {
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
            if (value != null) {
                String displayValue = integerOnly ? String.valueOf(value.intValue()) : String.valueOf(value);
                loreComponents.add(Component.text("§7当前值: §f" + displayValue));
            } else {
                loreComponents.add(Component.text("§7当前值: §8(未设置)"));
            }

            if (minValue != null || maxValue != null) {
                String range = "§7范围: §f";
                if (minValue != null && maxValue != null) {
                    range += minValue + " - " + maxValue;
                } else if (minValue != null) {
                    range += ">= " + minValue;
                } else {
                    range += "<= " + maxValue;
                }
                loreComponents.add(Component.text(range));
            }

            loreComponents.add(Component.empty());
            loreComponents.add(Component.text("§e点击输入"));

            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
        return item;
    }



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
        if (value instanceof Number n) {
            this.value = n.doubleValue();
        } else if (value instanceof String s) {
            try { this.value = Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
    }



    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {
        // 创建提交回调
        DialogActionCallback submitCallback = (response, audience) -> {
            String inputValue = response.getText(id);
            if (inputValue != null && !inputValue.isEmpty()) {
                try {
                    double parsedValue = Double.parseDouble(inputValue);

                    // 验证范围
                    if (minValue != null && parsedValue < minValue) {
                        player.sendMessage(Component.text("§c输入值不能小于 " + minValue));
                        session.refresh();
                        return;
                    }
                    if (maxValue != null && parsedValue > maxValue) {
                        player.sendMessage(Component.text("§c输入值不能大于 " + maxValue));
                        session.refresh();
                        return;
                    }

                    // 如果只允许整数，进行转换
                    if (integerOnly) {
                        parsedValue = Math.floor(parsedValue);
                    }

                    this.value = parsedValue;
                    session.getResult().put(id, parsedValue);

                    // 重新打开 GUI
                    session.refresh();
                } catch (NumberFormatException e) {
                    player.sendMessage(Component.text("§c请输入有效的数字"));
                    session.refresh();
                }
            } else {
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
        String promptText = prompt;
        if (minValue != null || maxValue != null) {
            promptText += "\n范围: ";
            if (minValue != null && maxValue != null) {
                promptText += minValue + " - " + maxValue;
            } else if (minValue != null) {
                promptText += ">= " + minValue;
            } else {
                promptText += "<= " + maxValue;
            }
        }
        if (integerOnly) {
            promptText += "\n(仅限整数)";
        }

        DialogBase.Builder baseBuilder = DialogBase.builder(Component.text(displayName))
                .canCloseWithEscape(false)
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(List.of(DialogBody.plainMessage(Component.text(promptText), 350)));

        // 添加输入字段
        io.papermc.paper.registry.data.dialog.input.TextDialogInput.Builder inputBuilder = DialogInput.text(id, Component.text("输入数字"))
                .width(250)
                .maxLength(20);

        if (value != null) {
            String displayValue = integerOnly ? String.valueOf(value.intValue()) : String.valueOf(value);
            inputBuilder.initial(displayValue);
        } else if (defaultValue != null) {
            String displayValue = integerOnly ? String.valueOf(defaultValue.intValue()) : String.valueOf(defaultValue);
            inputBuilder.initial(displayValue);
        }

        baseBuilder.inputs(List.of(inputBuilder.build()));

        DialogBase base = baseBuilder.build();

        // 创建并显示对话框
        Dialog dialog = Dialog.create(factory ->
                factory.empty().base(base).type(DialogType.confirmation(submitButton, cancelButton)));

        player.showDialog(dialog);
    }

    public boolean validate() {
        return false;
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
