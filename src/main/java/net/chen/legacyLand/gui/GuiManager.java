package net.chen.legacyLand.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI 管理器（单例）
 * 管理所有 GUI 会话和表单
 */
public class GuiManager {

    @Getter
    private static GuiManager instance;

    private final JavaPlugin plugin;

    /**
     * 活跃会话映射（玩家 UUID -> 会话）
     */
    private final Map<UUID, GuiSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Inventory 到会话的映射（用于快速查找）
     */
    private final Map<Inventory, GuiSession> inventorySessionMap = new ConcurrentHashMap<>();

    /**
     * 注册的表单映射（表单 ID -> 表单类）
     */
    private final Map<String, Class<? extends GuiForm>> registeredForms = new HashMap<>();

    /**
     * 私有构造函数
     * @param plugin 插件实例
     */
    private GuiManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 初始化管理器，并启动超时清理任务（每 30 秒检查一次，超时 5 分钟）
     * @param plugin 插件实例
     */
    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new GuiManager(plugin);
            // 每 30 秒清理一次超时会话
            Bukkit.getScheduler().runTaskTimer(plugin, instance::cleanupTimedOutSessions, 600L, 600L);
        }
    }

    /**
     * 获取插件实例
     * @return 插件实例
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * 打开表单
     * @param player 玩家
     * @param form 表单
     * @return 创建的会话
     */
    public GuiSession openForm(Player player, GuiForm form) {
        // 关闭玩家已有的会话
        closeSession(player);

        // 初始化表单元素
        form.initialize();

        // 创建新会话
        GuiSession session = new GuiSession(player, form);
        activeSessions.put(player.getUniqueId(), session);

        // 渲染表单
        form.render(player, session);

        return session;
    }

    /**
     * 关闭玩家的会话
     * @param player 玩家
     */
    public void closeSession(Player player) {
        GuiSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            if (session.getCurrentInventory() != null) {
                inventorySessionMap.remove(session.getCurrentInventory());
            }
            session.close();
        }
    }

    /**
     * 获取玩家的活跃会话
     * @param player 玩家
     * @return 会话（可能为 null）
     */
    public GuiSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    /**
     * 通过 Inventory 获取会话
     * @param inventory Inventory
     * @return 会话（可能为 null）
     */
    public GuiSession getSessionByInventory(Inventory inventory) {
        return inventorySessionMap.get(inventory);
    }

    /**
     * 注册 Inventory 到会话的映射
     * @param inventory Inventory
     * @param session 会话
     */
    public void registerInventory(Inventory inventory, GuiSession session) {
        inventorySessionMap.put(inventory, session);
    }

    /**
     * 注销 Inventory 映射
     * @param inventory Inventory
     */
    public void unregisterInventory(Inventory inventory) {
        inventorySessionMap.remove(inventory);
    }

    /**
     * 注册表单类
     * @param formId 表单 ID
     * @param formClass 表单类
     */
    public void registerForm(String formId, Class<? extends GuiForm> formClass) {
        registeredForms.put(formId, formClass);
    }

    /**
     * 创建表单实例
     * @param formId 表单 ID
     * @return 表单实例（可能为 null）
     */
    public GuiForm createForm(String formId) {
        Class<? extends GuiForm> formClass = registeredForms.get(formId);
        if (formClass != null) {
            try {
                return formClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                plugin.getLogger().severe("无法创建表单实例: " + formId);
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 清理超时会话（超过 5 分钟无活动）
     */
    private void cleanupTimedOutSessions() {
        long now = System.currentTimeMillis();
        long timeout = 5 * 60 * 1000L;
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, GuiSession> entry : activeSessions.entrySet()) {
            if (now - entry.getValue().getCreatedAt() > timeout) {
                toRemove.add(entry.getKey());
            }
        }
        for (UUID uuid : toRemove) {
            GuiSession session = activeSessions.remove(uuid);
            if (session != null) {
                if (session.getCurrentInventory() != null) {
                    inventorySessionMap.remove(session.getCurrentInventory());
                }
                session.close();
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage("§e[GUI] 会话已超时，已自动关闭。");
                }
            }
        }
    }

    /**
     * 清理所有会话
     */
    public void cleanup() {
        for (GuiSession session : activeSessions.values()) {
            session.close();
        }
        activeSessions.clear();
        inventorySessionMap.clear();
    }
}
