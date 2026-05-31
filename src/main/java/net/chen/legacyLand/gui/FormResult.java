package net.chen.legacyLand.gui;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 表单提交结果容器
 * 存储表单中所有元素的值
 */
@Getter
@Setter
public class FormResult {

    /**
     * 表单数据映射（元素 ID -> 值）
     */
    private final Map<String, Object> data = new HashMap<>();

    /**
     * 表单是否已提交
     */
    private boolean submitted = false;

    /**
     * 表单是否已取消
     */
    private boolean cancelled = false;

    /**
     * 添加表单数据
     * @param elementId 元素 ID
     * @param value 值
     */
    public void put(String elementId, Object value) {
        data.put(elementId, value);
    }

    /**
     * 获取表单数据
     * @param elementId 元素 ID
     * @return 值（可能为 null）
     */
    public Object get(String elementId) {
        return data.get(elementId);
    }

    /**
     * 获取字符串类型的数据
     * @param elementId 元素 ID
     * @return 字符串值
     */
    public String getString(String elementId) {
        Object value = data.get(elementId);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取整数类型的数据
     * @param elementId 元素 ID
     * @return 整数值
     */
    public Integer getInt(String elementId) {
        Object value = data.get(elementId);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * 获取双精度浮点数类型的数据
     * @param elementId 元素 ID
     * @return 双精度浮点数值
     */
    public Double getDouble(String elementId) {
        Object value = data.get(elementId);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    /**
     * 获取布尔类型的数据
     * @param elementId 元素 ID
     * @return 布尔值
     */
    public Boolean getBoolean(String elementId) {
        Object value = data.get(elementId);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

    /**
     * 检查是否包含指定元素的数据
     * @param elementId 元素 ID
     * @return 是否包含
     */
    public boolean contains(String elementId) {
        return data.containsKey(elementId);
    }

    /**
     * 标记表单为已提交
     */
    public void markSubmitted() {
        this.submitted = true;
        this.cancelled = false;
    }

    /**
     * 标记表单为已取消
     */
    public void markCancelled() {
        this.cancelled = true;
        this.submitted = false;
    }
}
