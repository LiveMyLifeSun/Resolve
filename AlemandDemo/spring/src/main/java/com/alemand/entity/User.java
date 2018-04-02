package com.alemand.entity;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/19
 */
public class User {

    private String userName;

    private String passWard;

    public User() {
    }

    public String getUserName() {
        return userName;
    }

    public String getPassWard() {
        return passWard;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassWard(String passWard) {
        this.passWard = passWard;
    }
}
