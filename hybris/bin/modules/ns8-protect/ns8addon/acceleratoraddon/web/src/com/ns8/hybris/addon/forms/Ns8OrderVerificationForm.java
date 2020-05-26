package com.ns8.hybris.addon.forms;

public class Ns8OrderVerificationForm {

    private String orderId;
    private String token;
    private String verificationId;
    private String phone;
    private String code;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(final String verificationId) {
        this.verificationId = verificationId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

}
