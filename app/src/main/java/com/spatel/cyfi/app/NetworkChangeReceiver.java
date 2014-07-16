package com.spatel.cyfi.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by sheilpatelmac on 6/8/14.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    static String network_interface = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(wifi.isConnected())
            network_interface = "WIFI";
        else if(mobile.isConnected())
            network_interface = "MOBILE";
    }

    public static String getInterface(){
        return network_interface;
    }

    public static void setInterface(String s){
        network_interface = s;
    }
}
