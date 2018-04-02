package com.hangtuo.entity.device;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import lombok.Data;

/**
 * Created by Administrator on 2017/5/23.
 */
@Data
public class ReqDeviceItem {
    @SerializedName("clientID")
    @Expose
    public String clientID;
    @SerializedName("groupID")
    @Expose
    public String groupID;
    @SerializedName("deviceID")
    @Expose
    public String deviceID;
    @SerializedName("deviceIDs")
    @Expose
    public ArrayList<String> deviceIDs;

    public static final class Builder {
        public String clientID;
        public String groupID;
        public String deviceID;
        public ArrayList<String> deviceIDs;

        public Builder() {
        }

        public static Builder aReqDeviceItem() {
            return new Builder();
        }

        public Builder withClientID(String clientID) {
            this.clientID = clientID;
            return this;
        }

        public Builder withGroupID(String groupID) {
            this.groupID = groupID;
            return this;
        }

        public Builder withDeviceID(String deviceID) {
            this.deviceID = deviceID;
            return this;
        }

        public Builder withDeviceIDs(ArrayList<String> deviceIDs) {
            this.deviceIDs = deviceIDs;
            return this;
        }

        public ReqDeviceItem build() {
            ReqDeviceItem reqDeviceItem = new ReqDeviceItem();
            reqDeviceItem.setClientID(clientID);
            reqDeviceItem.setGroupID(groupID);
            reqDeviceItem.setDeviceID(deviceID);
            reqDeviceItem.setDeviceIDs(deviceIDs);
            return reqDeviceItem;
        }
    }
}
