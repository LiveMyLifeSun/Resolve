package com.alemand.resolve.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/19
 */
@Data
public class ReqImportClient {

    @SerializedName("userName")
    @Expose
    private String userName;

    @SerializedName("loginName")
    @Expose
    private String loginName;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("groupID")
    @Expose
    private String groupID;

}
