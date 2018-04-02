package com.hangtuo.common.Enum;

import java.util.HashMap;
import java.util.Map;

/**
 * <p> 设备请求类型</p>
 *
 * @author Alemand
 * @since 2017/11/16
 */
public enum XZ_DEVICE_RES {

    /**
     *全部设备
     */
    ALL(0),
    /**
     *已停用
     */
    BREAKDOWN(1),
    /**
     *退场
     */
    EXIT(2),
    /**
     *使用中
     */
    USEING(3);

    private static Map map = new HashMap();

    static {
        for (XZ_DEVICE_RES pageType : XZ_DEVICE_RES.values()) {
            map.put(pageType.value, pageType);
        }
    }

    private int value;


    private XZ_DEVICE_RES(int value) {
        this.value = value;
    }

    public static XZ_DEVICE_RES valueOf(int value) {
        return (XZ_DEVICE_RES) map.get(value);
    }

    public int getValue() {
        return value;
    }
}
