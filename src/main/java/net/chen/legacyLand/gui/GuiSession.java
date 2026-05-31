package net.chen.legacyLand.gui;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI 会话管理类
 * 管理单个玩家的 GUI 交互会话
 */
@Getter
@Setter
public class GuiSession {

    /**
     * 会话 ID
     */
    private final UUID sessionId;

    /**
     * 玩家 UUID
     */
    private final UUID playerUuid;

    /**
     * 关联的表单
     */
    private final GuiForm form;

    /**
     * 当前打开的 Inventory
     */
    private Inventory currentInventory;

    /**
     * 表单结果
     */
    private final FormResult result;

    /**
     * 会话数据（用于存储临时状态）
     */
    private final Map<String, Object> sessionData = new HashMap<>();

    /**
     * 会话创建时间
     */
    private final long createdAt;

    /**
     * 会话是否活跃
     */
    private boolean active = true;

    /**
     * 构造函数
     * @param player 玩家
     * @param form 表单
     */
    public GuiSession(Player player, GuiForm form) {
        this.sessionId = UUID.randomUUID();
        this.playerUuid = player.getUniqueId();
        this.form = form;
        this.result = new FormResult();
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * 存储会话数据
     * @param key 键
     * @param value 值
     */
    public void putData(String key, Object value) {
        sessionData.put(key, value);
    }

    /**
     * 获取会话数据
     * @param key 键
     * @return 值
     */
    public Object getData(String key) {
        return sessionData.get(key);
    }

    /**
     * 获取字符串类型的会话数据
     * @param key 键
     * @return 字符串值
     */
    public String getStringData(String key) {
        Object value = sessionData.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取整数类型的会话数据
     * @param key 键
     * @return 整数值
     */
    public Integer getIntData(String key) {
        Object value = sessionData.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * 检查会话是否包含指定数据
     * @param key 键
     * @return 是否包含
     */
    public boolean hasData(String key) {
        return sessionData.containsKey(key);
    }

    /**
     * 关闭会话
     */
    public void close() {
        this.active = false;
        if (currentInventory != null) {
            Player player = org.bukkit.Bukkit.getPlayer(playerUuid);
            if (player != null && player.getOpenInventory().getTopInventory().equals(currentInventory)) {
                player.closeInventory();
            }
        }
    }

    /**
     * 刷新当前 GUI
     */
    public void refresh() {
        if (currentInventory != null && active) {
            Player player = org.bukkit.Bukkit.getPlayer(playerUuid);
            if (player != null) {
                form.render(player, this);
            }
        }
    }
}
