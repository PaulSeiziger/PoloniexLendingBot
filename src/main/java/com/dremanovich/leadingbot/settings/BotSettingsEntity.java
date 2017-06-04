package com.dremanovich.leadingbot.settings;

/**
 * Created by PavelDremanovich on 05.06.17.
 */
public class BotSettingsEntity {
    private String url;
    private int requestDelay;
    private boolean printRequest;
    private int connectTimeout;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRequestDelay() {
        return requestDelay;
    }

    public void setRequestDelay(int requestDelay) {
        this.requestDelay = requestDelay;
    }

    public boolean isPrintRequest() {
        return printRequest;
    }

    public void setPrintRequest(boolean printRequest) {
        this.printRequest = printRequest;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
