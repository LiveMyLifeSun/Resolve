package com.hangtuo.entity.file;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * Created by Shawn on 2017/8/22.
 */
@Data
public class ReqFileItem {
    @SerializedName("fileID")
    @Expose
    private String fileID;
    @SerializedName("status")
    @Expose
    public int status;
    @SerializedName("rectifyID")
    @Expose
    public String rectifyID;
    @SerializedName("deviceID")
    @Expose
    public String deviceID;
    @SerializedName("planID")
    @Expose
    public String planID;
    @SerializedName("downloadURL")
    @Expose
    private String downloadURL;
    @SerializedName("fileType")
    @Expose
    private int fileType;
    @SerializedName("timestamp")
    @Expose
    private long timestamp;
    @SerializedName("size")
    @Expose
    private double size;
    @SerializedName("forkID")
    @Expose
    private String forkID;
    @SerializedName("md5")
    @Expose
    private String md5;
    @SerializedName("clientID")
    @Expose
    private String clientID;
    @SerializedName("postClientID")
    @Expose
    private String postClientID;
    @SerializedName("groupID")
    @Expose
    private String groupID;

    public FileItem makeFileItem(){
        FileItem fileItem = new FileItem();
        fileItem.setDownloadURL(downloadURL);
        fileItem.setFileType(fileType);
        fileItem.setSize(size);
        fileItem.setMd5(md5);
        fileItem.setPostClientID(postClientID);
        return fileItem;
    }


    public static final class Builder {
        public int status;
        public String rectifyID;
        public String planID;
        private String fileID;
        private String downloadURL;
        private int fileType;
        private long timestamp;
        private double size;
        private String forkID;
        private String md5;
        private String clientID;
        private String postClientID;
        private String groupID;

        public Builder() {
        }

        public static Builder aReqFileItem() {
            return new Builder();
        }

        public Builder withFileID(String fileID) {
            this.fileID = fileID;
            return this;
        }

        public Builder withStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder withRectifyID(String rectifyID) {
            this.rectifyID = rectifyID;
            return this;
        }

        public Builder withPlanID(String planID) {
            this.planID = planID;
            return this;
        }

        public Builder withDownloadURL(String downloadURL) {
            this.downloadURL = downloadURL;
            return this;
        }

        public Builder withFileType(int fileType) {
            this.fileType = fileType;
            return this;
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withSize(double size) {
            this.size = size;
            return this;
        }

        public Builder withForkID(String forkID) {
            this.forkID = forkID;
            return this;
        }

        public Builder withMd5(String md5) {
            this.md5 = md5;
            return this;
        }

        public Builder withClientID(String clientID) {
            this.clientID = clientID;
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

        public ReqFileItem build() {
            ReqFileItem reqFileItem = new ReqFileItem();
            reqFileItem.setFileID(fileID);
            reqFileItem.setStatus(status);
            reqFileItem.setRectifyID(rectifyID);
            reqFileItem.setPlanID(planID);
            reqFileItem.setDownloadURL(downloadURL);
            reqFileItem.setFileType(fileType);
            reqFileItem.setTimestamp(timestamp);
            reqFileItem.setSize(size);
            reqFileItem.setForkID(forkID);
            reqFileItem.setMd5(md5);
            reqFileItem.setClientID(clientID);
            reqFileItem.setPostClientID(postClientID);
            reqFileItem.setGroupID(groupID);
            return reqFileItem;
        }
    }
}
