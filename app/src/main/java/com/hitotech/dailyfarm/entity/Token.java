package com.hitotech.dailyfarm.entity;

/**
 * Created by Lv on 2016/4/4.
 */
public class Token {

    /**
     * scope : SCOPE
     * unionid : o6_bmasdasdsad6_2sgVt7hMZOPfL
     * openid : OPENID
     * expires_in : 7200
     * refresh_token : REFRESH_TOKEN
     * access_token : ACCESS_TOKEN
     */
    private String scope;
    private String unionid;
    private String openid;
    private int expires_in;
    private String refresh_token;
    private String access_token;

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getScope() {
        return scope;
    }

    public String getUnionid() {
        return unionid;
    }

    public String getOpenid() {
        return openid;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public String getAccess_token() {
        return access_token;
    }
}
