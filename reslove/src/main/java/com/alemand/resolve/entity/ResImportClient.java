package com.alemand.resolve.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Shawn
 * @since 2018/3/19
 */
public class ResImportClient implements Serializable {
    @SerializedName("clientID")
    @Expose
    private String clientID;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("loginName")
    @Expose
    private String loginName;
    @SerializedName("status")
    @Expose
    private int status;

    public ResImportClient() {
    }

    public String getClientID() {
        return clientID;
    }

    public String getUserName() {
        return userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public int getStatus() {
        return status;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
