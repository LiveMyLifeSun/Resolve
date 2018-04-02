package com.hangtuo.entity.device;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/21
 */
@Data
public class NodeFileDevice {
    @SerializedName("fileName")
    @Expose
    private String fileName;
    @SerializedName("deviceID")
    @Expose
    private String deviceID;
    @SerializedName("callbackURL")
    @Expose
    private String callbackURL;
    @SerializedName("postClientID")
    @Expose
    private String postClientID;
    @SerializedName("groupID")
    @Expose
    private String groupID;
    @SerializedName("qrCode")
    @Expose
    private String qrCode;

    public static final class Builder {
        private String fileName;
        private String deviceID;
        private String callbackURL;
        private String postClientID;
        private String groupID;
        private String qrCode;

        public Builder() {
        }

        public static Builder aNodeFileDevice() {
            return new Builder();
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withDeviceID(String deviceID) {
            this.deviceID = deviceID;
            return this;
        }

        public Builder withCallbackURL(String callbackURL) {
            this.callbackURL = callbackURL;
            return this;
        }

        public Builder withPostClientID(String postClientID) {
            this.postClientID = postClientID;
            return this;
        }

        public Builder withGroupID(String groupID) {
            this.groupID = groupID;
            return this;
        }

        public Builder withQrCode(String qrCode) {
            this.qrCode = qrCode;
            return this;
        }

        public NodeFileDevice build() {
            NodeFileDevice nodeFileDevice = new NodeFileDevice();
            nodeFileDevice.setFileName(fileName);
            nodeFileDevice.setDeviceID(deviceID);
            nodeFileDevice.setCallbackURL(callbackURL);
            nodeFileDevice.setPostClientID(postClientID);
            nodeFileDevice.setGroupID(groupID);
            nodeFileDevice.setQrCode(qrCode);
            return nodeFileDevice;
        }
    }
}
