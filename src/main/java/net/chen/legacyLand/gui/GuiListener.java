package net.chen.legacyLand.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

/**
 * GUI 事件监听器
 * 统一处理所有 GUI 相关的事件
 */
public class GuiListener implements Listener {

    /**
     * 处理 Inventory 点击事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) {
            return;
        }

        // 检查是否是 GUI 会话的 Inventory
        GuiSession session = GuiManager.getInstance().getSessionByInventory(clickedInventory);
        if (session == null) {
            return;
        }

        // 取消事件（防止物品被拿走）
        event.setCancelled(true);

        // 获取点击的槽位
        int slot = event.getSlot();
        if (slot < 0) {
            return;
        }

        // 委托给表单处理
        GuiForm form = session.getForm();
        if (form != null) {
            form.handleClick(player, session, slot, event.getClick());
        }
    }

    /**
     * 处理 Inventory 关闭事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        // 检查是否是 GUI 会话的 Inventory
        GuiSession session = GuiManager.getInstance().getSessionByInventory(inventory);
        if (session == null) {
            return;
        }

        // 检查会话是否仍然活跃
        GuiSession activeSession = GuiManager.getInstance().getSession(player);
        if (activeSession != null && activeSession.equals(session)) {
            // 如果表单未提交也未取消，则视为取消
            FormResult result = session.getResult();
            if (!result.isSubmitted() && !result.isCancelled()) {
                session.getForm().cancel(session);
            }
        }

        // 清理 Inventory 映射
        GuiManager.getInstance().unregisterInventory(inventory);
    }

    /**
     * 处理玩家退出事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 清理玩家的会话
        GuiManager.getInstance().closeSession(player);
    }
}
