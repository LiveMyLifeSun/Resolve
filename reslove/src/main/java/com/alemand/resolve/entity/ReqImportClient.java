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

    public String getUserName() {
        return userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getPassword() {
        return password;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }
}
