package com.hangtuo.entity.device;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import lombok.Data;

/**
 * Created by Shawn on 2017/5/23. 停用记录
 */
@Data
public class DisableInfo {
    @SerializedName("disableID")
    @Expose
    public String disableID;
    @SerializedName("clientID")
    @Expose
    public String clientID;
    @SerializedName("groupID")
    @Expose
    public String groupID;
    @SerializedName("deviceID")
    @Expose
    public String deviceID;
    @SerializedName("disableTime")
    @Expose
    public long disableTime;
    @SerializedName("content")
    @Expose
    public String content;
    @SerializedName("status")
    @Expose
    public int status;
    @SerializedName("disableIDNum")
    @Expose
    public int disableIDNum;
    @SerializedName("deviceIDs")
    @Expose
    public ArrayList<String> deviceIDs;


}
