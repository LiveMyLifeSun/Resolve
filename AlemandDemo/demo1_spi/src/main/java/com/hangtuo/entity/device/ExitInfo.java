package com.hangtuo.entity.device;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import lombok.Data;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/17
 */
@Data
public class ExitInfo {
    @SerializedName("clientID")
    @Expose
    public String clientID;
    @SerializedName("deviceID")
    @Expose
    public String deviceID;
    @SerializedName("groupID")
    @Expose
    public String groupID;
    @SerializedName("content")
    @Expose
    public String content;
    @SerializedName("exitTime")
    @Expose
    public long exitTime;
    @SerializedName("deviceIDs")
    @Expose
    public ArrayList<String> deviceIDs;

}
