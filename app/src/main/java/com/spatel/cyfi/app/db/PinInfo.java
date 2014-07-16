package com.spatel.cyfi.app.db;

/**
 * Created by sheilpatelmac on 6/8/14.
 */
public class PinInfo {

    int key;
    String date;
    String internal_ip;
    String external_ip;
    String broadcast_ip;
    String dns;
    String gateway;
    String subnet;
    String ssid;
    String ssid_mac;
    String ping;
    String speedtest;
    String signal_strength;
    double latitude;
    double longitude;

    public PinInfo() {
        //empty constructor
    }

    public PinInfo(int key, String date, String internal_ip, String external_ip,
                   String broadcast_ip, String dns, String gateway, String subnet,
                   String ssid, String ssid_mac, String ping, String speedtest,
                   String signal_strength, double latitude, double longitude) {

        this.key = key;
        this.date = date;
        this.internal_ip = internal_ip;
        this.external_ip = external_ip;
        this.broadcast_ip = broadcast_ip;
        this.dns = dns;
        this.gateway = gateway;
        this.subnet = subnet;
        this.ssid = ssid;
        this.ssid_mac = ssid_mac;
        this.ping = ping;
        this.speedtest = speedtest;
        this.signal_strength = signal_strength;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInternal_ip() {
        return internal_ip;
    }

    public void setInternal_ip(String internal_ip) {
        this.internal_ip = internal_ip;
    }

    public String getExternal_ip() {
        return external_ip;
    }

    public void setBroadcast_ip(String broadcast_ip) {
        this.broadcast_ip = broadcast_ip;
    }

    public String getBroadcast_ip() {
        return broadcast_ip;
    }

    public void setExternal_ip(String external_ip) {
        this.external_ip = external_ip;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSsid_mac() {
        return ssid_mac;
    }

    public void setSsid_mac(String ssid_mac) {
        this.ssid_mac = ssid_mac;
    }

    public String getPing() {
        return ping;
    }

    public void setPing(String ping) {
        this.ping = ping;
    }

    public String getSpeedtest() {
        return speedtest;
    }

    public void setSpeedtest(String speedtest) {
        this.speedtest = speedtest;
    }

    public String getSignal_strength() {
        return signal_strength;
    }

    public void setSignal_strength(String signal_strength) {
        this.signal_strength = signal_strength;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
