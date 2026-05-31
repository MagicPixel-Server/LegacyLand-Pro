package net.chen.legacyLand.gui.elements;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
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
import java.util.function.Consumer;

/**
 * 下拉选择元素 - 通过 Dialog API 的 singleOption 实现
 */
public class DropdownElement implements GuiElement {

    private final String id;
    private String displayName;
    private String prompt;
    private final List<String> optionIds;
    private final List<String> optionLabels;
    private String selectedId;
    private Material icon;
    private int slot = -1;
    private Consumer<String> onSelect;
    private Consumer<Object> validator;

    public DropdownElement(String id, String displayName, String prompt) {
        this.id = id;
        this.displayName = displayName;
        this.prompt = prompt;
        this.icon = Material.CHEST;
        this.optionIds = new ArrayList<>();
        this.optionLabels = new ArrayList<>();
    }

    public DropdownElement addOption(String optionId, String label) {
        this.optionIds.add(optionId);
        this.optionLabels.add(label);
        return this;
    }

    public DropdownElement defaultOption(String optionId) {
        this.selectedId = optionId;
        return this;
    }

    public DropdownElement icon(Material icon) {
        this.icon = icon;
        return this;
    }

    public DropdownElement onSelect(Consumer<String> callback) {
        this.onSelect = callback;
        return this;
    }

    public DropdownElement validator(Consumer<Object> validator) {
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

    @Override
    public ItemStack render(GuiSession session) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            if (selectedId != null) {
                int idx = optionIds.indexOf(selectedId);
                String label = idx >= 0 ? optionLabels.get(idx) : selectedId;
                lore.add(Component.text("§7当前: §f" + label));
            } else {
                lore.add(Component.text("§7当前: §8(未选择)"));
            }
            lore.add(Component.empty());
            lore.add(Component.text("§e点击选择"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onClick(Player player, GuiSession session, ClickType clickType) {
        if (optionIds.isEmpty()) return;

        DialogActionCallback submitCallback = (response, audience) -> {
            String chosen = response.getText(id);
            if (chosen != null && !chosen.isEmpty()) {
                selectedId = chosen;
                session.getResult().put(id, chosen);
                if (onSelect != null) onSelect.accept(chosen);
            }
            session.refresh();
        };

        DialogActionCallback cancelCallback = (response, audience) -> session.refresh();

        ClickCallback.Options cbOptions = ClickCallback.Options.builder()
                .uses(1)
                .lifetime(Duration.ofMinutes(10))
                .build();

        ActionButton submitButton = ActionButton.builder(Component.text("§a确认"))
                .width(150)
                .action(DialogAction.customClick(submitCallback, cbOptions))
                .build();

        ActionButton cancelButton = ActionButton.builder(Component.text("§c取消"))
                .width(150)
                .action(DialogAction.customClick(cancelCallback, cbOptions))
                .build();

        // 构建选项列表
        List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>();
        for (int i = 0; i < optionIds.size(); i++) {
            String oid = optionIds.get(i);
            String label = optionLabels.get(i);
            boolean isInitial = oid.equals(selectedId);
            entries.add(SingleOptionDialogInput.OptionEntry.create(oid, Component.text(label), isInitial));
        }

        io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput.Builder inputBuilder =
                DialogInput.singleOption(id, Component.text(displayName), entries).width(300);

        DialogBase base = DialogBase.builder(Component.text(displayName))
                .canCloseWithEscape(false)
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(List.of(DialogBody.plainMessage(Component.text(prompt), 350)))
                .inputs(List.of(inputBuilder.build()))
                .build();

        Dialog dialog = Dialog.create(factory ->
                factory.empty().base(base).type(DialogType.confirmation(submitButton, cancelButton)));

        player.showDialog(dialog);
    }

    @Override
    public boolean validate(Player player) { return selectedId != null; }

    @Override
    public void setValue(Object value) {
        if (value != null) this.selectedId = value.toString();
    }

    @Override
    public Object getValue() { return selectedId; }

    @Override
    public Consumer<Object> getValidator() { return validator; }
}
