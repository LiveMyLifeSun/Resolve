package com.hangtuo.entity.file;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * Created by Shawn on 2017/8/21.
 */
@Data
public class FileItem {
    @SerializedName("fileID")
    @Expose
    private String fileID;
    @SerializedName("downloadURL")
    @Expose
    private String downloadURL;
    @SerializedName("fileType")
    @Expose
    private int fileType;
    @SerializedName("deadTime")
    @Expose
    private long deadTime;
    @SerializedName("previewURL")
    @Expose
    private String previewURL;
    @SerializedName("size")
    @Expose
    private double size;
    @SerializedName("fileName")
    @Expose
    private String fileName;
    @SerializedName("md5")
    @Expose
    private String md5;
    @SerializedName("createDate")
    @Expose
    private long createDate;
    @SerializedName("postClientID")
    @Expose
    private String postClientID;
    @SerializedName("shareURL")
    @Expose
    private String shareURL;
    @SerializedName("title")
    @Expose
    private String title;

}
