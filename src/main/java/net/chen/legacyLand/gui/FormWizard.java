package net.chen.legacyLand.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 多步骤向导表单
 * 每个步骤是一个独立的 GuiForm，向导负责步骤间导航和数据聚合
 */
public abstract class FormWizard {

    private final String wizardId;
    private final String title;
    private final List<GuiForm> steps = new ArrayList<>();
    private int currentStepIndex = 0;
    private final Map<String, Object> wizardData = new HashMap<>();

    private Consumer<FormResult> onComplete;
    private Consumer<FormResult> onCancel;

    protected FormWizard(String wizardId, String title) {
        this.wizardId = wizardId;
        this.title = title;
    }

    /**
     * 子类实现：添加步骤
     */
    protected abstract void buildSteps();

    /**
     * 添加步骤
     */
    protected void addStep(GuiForm step) {
        steps.add(step);
    }

    public FormWizard onComplete(Consumer<FormResult> callback) {
        this.onComplete = callback;
        return this;
    }

    public FormWizard onCancel(Consumer<FormResult> callback) {
        this.onCancel = callback;
        return this;
    }

    /**
     * 打开向导（从第一步开始）
     */
    public void open(Player player) {
        if (steps.isEmpty()) buildSteps();
        currentStepIndex = 0;
        openCurrentStep(player);
    }

    private void openCurrentStep(Player player) {
        if (currentStepIndex < 0 || currentStepIndex >= steps.size()) return;

        GuiForm step = steps.get(currentStepIndex);
        // 注入导航按钮
        injectNavigationButtons(step, player);
        GuiManager.getInstance().openForm(player, step);
    }

    private void injectNavigationButtons(GuiForm step, Player player) {
        // 移除旧的导航按钮（避免重复添加）
        step.removeElement("__wizard_prev__");
        step.removeElement("__wizard_next__");
        step.removeElement("__wizard_cancel__");

        int inventorySize = step.getSize() * 9;
        boolean isFirst = currentStepIndex == 0;
        boolean isLast = currentStepIndex == steps.size() - 1;

        // 取消按钮（左下角 slot 45）
        net.chen.legacyLand.gui.elements.ButtonElement cancelBtn =
                new net.chen.legacyLand.gui.elements.ButtonElement("__wizard_cancel__", Material.BARRIER, "§c取消");
        cancelBtn.setSlot(inventorySize - 9);
        cancelBtn.onClick(p -> cancelWizard(p));
        step.addElement(cancelBtn);

        // 上一步按钮（slot 48）
        if (!isFirst) {
            net.chen.legacyLand.gui.elements.ButtonElement prevBtn =
                    new net.chen.legacyLand.gui.elements.ButtonElement("__wizard_prev__", Material.ARROW,
                            "§e上一步 (" + currentStepIndex + "/" + steps.size() + ")");
            prevBtn.setSlot(inventorySize - 6);
            prevBtn.onClick(p -> previousStep(p));
            step.addElement(prevBtn);
        }

        // 下一步/完成按钮（slot 50）
        String nextLabel = isLast
                ? "§a完成 (" + steps.size() + "/" + steps.size() + ")"
                : "§a下一步 (" + (currentStepIndex + 2) + "/" + steps.size() + ")";
        Material nextMat = isLast ? Material.EMERALD : Material.ARROW;
        net.chen.legacyLand.gui.elements.ButtonElement nextBtn =
                new net.chen.legacyLand.gui.elements.ButtonElement("__wizard_next__", nextMat, nextLabel);
        nextBtn.setSlot(inventorySize - 4);
        nextBtn.onClick(p -> {
            if (isLast) {
                completeWizard(p);
            } else {
                nextStep(p);
            }
        });
        step.addElement(nextBtn);
    }

    private void nextStep(Player player) {
        // 收集当前步骤数据
        GuiSession session = GuiManager.getInstance().getSession(player);
        if (session != null) {
            wizardData.putAll(session.getResult().getData());
        }
        currentStepIndex++;
        openCurrentStep(player);
    }

    private void previousStep(Player player) {
        GuiSession session = GuiManager.getInstance().getSession(player);
        if (session != null) {
            wizardData.putAll(session.getResult().getData());
        }
        currentStepIndex--;
        openCurrentStep(player);
    }

    private void completeWizard(Player player) {
        GuiSession session = GuiManager.getInstance().getSession(player);
        if (session != null) {
            wizardData.putAll(session.getResult().getData());
        }
        GuiManager.getInstance().closeSession(player);

        FormResult result = new FormResult();
        wizardData.forEach(result::put);
        result.markSubmitted();

        if (onComplete != null) onComplete.accept(result);
    }

    private void cancelWizard(Player player) {
        GuiManager.getInstance().closeSession(player);
        FormResult result = new FormResult();
        wizardData.forEach(result::put);
        result.markCancelled();
        if (onCancel != null) onCancel.accept(result);
    }

    /**
     * 获取跨步骤数据
     */
    public Object getWizardData(String key) {
        return wizardData.get(key);
    }

    public String getWizardId() { return wizardId; }
    public int getCurrentStepIndex() { return currentStepIndex; }
    public int getTotalSteps() { return steps.size(); }
}
