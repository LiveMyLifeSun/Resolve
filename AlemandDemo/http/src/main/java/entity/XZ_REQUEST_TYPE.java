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
public enum XZ_REQUEST_TYPE {
    /**
     * post请求方式
     */
    POST(0),
    /**
     * get请求方式
     */
    GET(1);

    private static Map map = new HashMap();

    static {
        for (XZ_REQUEST_TYPE pageType : XZ_REQUEST_TYPE.values()) {
            map.put(pageType.value, pageType);
        }
    }

    private int value;

    private XZ_REQUEST_TYPE(int value) {
        this.value = value;
    }

    public static XZ_REQUEST_TYPE valueOf(int value) {
        return (XZ_REQUEST_TYPE) map.get(value);
    }

    public int getValue() {
        return value;
    }

}
