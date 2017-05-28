package com.dremanovich.leadingbot.api.entities;

/**
 * Created by PavelDremanovich on 28.05.17.
 */
public class CanceledLoanOfferResponseEntity {
    private int success;
    private String message;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
