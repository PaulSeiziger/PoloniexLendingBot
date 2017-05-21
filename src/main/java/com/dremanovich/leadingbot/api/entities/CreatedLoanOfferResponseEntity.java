package com.dremanovich.leadingbot.api.entities;

/**
 * Created by PavelDremanovich on 21.05.17.
 */
public class CreatedLoanOfferResponseEntity {
    private int success;
    private String message;
    private int orderID;

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

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

}
