package entity;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/4/2
 */
public enum XZ_SUBMIT_TYPE {
    /**
     * post请求方式
     */
    FORM(0),
    /**
     * get请求方式
     */
    JSON(1);

    private static Map map = new HashMap();

    static {
        for (XZ_SUBMIT_TYPE pageType : XZ_SUBMIT_TYPE.values()) {
            map.put(pageType.value, pageType);
        }
    }

    private int value;

    private XZ_SUBMIT_TYPE(int value) {
        this.value = value;
    }

    public static XZ_SUBMIT_TYPE valueOf(int value) {
        return (XZ_SUBMIT_TYPE) map.get(value);
    }

    public int getValue() {
        return value;
    }

}
