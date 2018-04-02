package com.alemand.commen;

import java.util.HashMap;
import java.util.Map;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/1
 */
public enum COLOR {
    RED("红色"),
    GREEN("绿色");
    private String value;

    private COLOR (String value){
        this.value = value;
    }

    public void Set(String value){
        this.value = value;
    }
    public String get(){
        return value;
    }

}
