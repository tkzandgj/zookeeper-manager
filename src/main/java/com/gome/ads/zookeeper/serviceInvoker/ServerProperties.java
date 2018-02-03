package com.gome.ads.zookeeper.serviceInvoker;

public class ServerProperties {
    private String name;
    private String invokeAlert;
    private String noHostAlert;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInvokeAlert() {
        return invokeAlert;
    }

    public void setInvokeAlert(String invokeAlert) {
        this.invokeAlert = invokeAlert;
    }

    public String getNoHostAlert() {
        return noHostAlert;
    }

    public void setNoHostAlert(String noHostAlert) {
        this.noHostAlert = noHostAlert;
    }
}
