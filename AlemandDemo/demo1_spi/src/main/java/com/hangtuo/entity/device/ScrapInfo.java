package com.hangtuo.entity.device;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import lombok.Data;

/**
 * Created by Shawn on 2017/5/24.
 */
@Data
public class ScrapInfo {
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
    @SerializedName("scrapTime")
    @Expose
    public long scrapTime;
    @SerializedName("deviceIDs")
    @Expose
    public ArrayList<String> deviceIDs;


}
