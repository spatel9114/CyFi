package com.spatel.cyfi.app.fragments;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.spatel.cyfi.app.NetworkChangeReceiver;
import com.spatel.cyfi.app.PinInfo;
import com.spatel.cyfi.app.R;
import com.spatel.cyfi.app.db.DatabaseHandler;
import gps.GPSTracker;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.google.android.gms.maps.model.LatLng;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class NetworkInfoFragment extends Fragment {

    Context context;
    final int interval = 10000;
    Handler handle = new Handler();
    Runnable status;
    TextView signal_strength;
    TextView signal_strength_header;
    MobilePhoneListener phoneState;
    TelephonyManager telManager;
    String network_interface = "MOBILE";
    View view = null;
    TextView mac_address;
    TextView gateway;
    TextView internal_ip;
    TextView external_ip;
    TextView subnet_mask;
    TextView broadcast_ip;
    TextView dns_primary;
    TextView dns_secondary;
    TextView ssid;
    TextView ssid_mac;
    TextView wifi_text;
    TextView mobile_text;
    TextView pingTest_text;
    TextView speedTest_text;
    TextView label_gateway;
    TextView label_subnet;
    TextView label_broadcast;
    private static View rootView;
    static WifiManager wifi_manager;
    GPSTracker gps;
    LatLng location;
    Menu optionsMenu;
    int snapshot_number = 10;
    DatabaseHandler db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {
            rootView = inflater.inflate(R.layout.network_info_fragment, container,
                    false);
        } catch (InflateException e) {
            // Map already exists
        }
        context = rootView.getContext();
        view = rootView;
        setHasOptionsMenu(true);
        final Button ping_google_Btn = (Button) rootView.findViewById(R.id.ping_test);
        final Button speed_test_Btn = (Button) rootView.findViewById(R.id.speed_test);
        setupNetwork();
        phoneState = new MobilePhoneListener();
        gps = new GPSTracker(context);
        location = getGPSLocation();
        setupInitialGUI();
        getActiveInterfaces();

        ping_google_Btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ping_google_Btn.setEnabled(false);
                new PingTest().execute(null, null, null);
            }

        });

        speed_test_Btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                speed_test_Btn.setEnabled(false);
                new DownloadSpeedTest().execute(null, null, null);
            }

        });
        db = new DatabaseHandler(context);

        return rootView;
    }

    public void setupNetwork(){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(wifi.isConnected()){
            network_interface = "WIFI";
            NetworkChangeReceiver.setInterface("WIFI");
        }
        else if(mobile.isConnected()){
            network_interface = "MOBILE";
            NetworkChangeReceiver.setInterface("MOBILE");
        }
    }

    public LatLng getGPSLocation() {
        return new LatLng(gps.getLatitude(), gps.getLongitude());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.info, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(network_interface.equals("WIFI")) {
            menu.findItem(R.id.history).setVisible(true);
        }
        else {
            menu.findItem(R.id.history).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history:
                if (network_interface.equals("WIFI")) {
                    try{
                        if(location == null)
                            throw new Exception("GPS not ON or masked");
                        double latitude = location.latitude;
                        double longitude = location.longitude;
                        String i_ip = internal_ip.getText().toString();
                        String e_ip = external_ip.getText().toString();
                        String gw = gateway.getText().toString();
                        String sn = subnet_mask.getText().toString();
                        String bc = broadcast_ip.getText().toString();
                        String dns_p = dns_primary.getText().toString();
                        String dns_s = dns_secondary.getText().toString();
                        String router = ssid.getText().toString().trim();
                        String router_MAC = ssid_mac.getText().toString();
                        String p_test = pingTest_text.getText().toString();
                        String s_test = speedTest_text.getText().toString();
                        String sig_strength = signal_strength.getText().toString();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a MM-dd-yyyy", Locale.getDefault());
                        String date = dateFormat.format(Calendar.getInstance()
                                .getTime());
                        String dns;
                        if (dns_s == null || dns_s.equals(""))
                            dns = dns_p;
                        else {
                            dns = dns_p + "; " + dns_s;
                        }
                        int count = db.getPinCount();
                        if (count < snapshot_number) {
                            int key = db.getAvailablePKey();
                            db.addPin(new PinInfo(key, date, i_ip, e_ip, bc, dns, gw,
                                    sn, router, router_MAC, p_test, s_test,
                                    sig_strength, latitude, longitude));
                            Toast.makeText(getView().getContext(),
                                    "Snapshot " + (count + 1) + " of "+snapshot_number+" Written",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            List<PinInfo> pins = db.getAllPins();
                            PinInfo remove = pins.get(pins.size() - 1);
                            db.deletePin(remove);
                            int key = db.getAvailablePKey();
                            db.addPin(new PinInfo(key, date, i_ip, e_ip, bc, dns, gw,
                                    sn, router, router_MAC, p_test, s_test,
                                    sig_strength, latitude, longitude));
                            Toast.makeText(getView().getContext(),
                                    "Snapshot 10 of 10 written", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                    catch(Exception e){
                        if(e.getMessage() != null){
                            Toast.makeText(getView().getContext(),"Error: "+e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                        else{
                            Toast.makeText(getView().getContext(),"Error With Snapshot, Please Try Again", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getView().getContext(),
                            "History Snapshot Only Avilable on Wifi",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.google_play:
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=Cryptic+Applications"));
                startActivity(browserIntent);
                return true;
            case R.id.about:
                Intent browserIntent_2 = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.crypticapplications.com"));
                startActivity(browserIntent_2);
                return true;
            default:
                break;
        }

        return false;
    }

    public void setupInitialGUI() {
        wifi_text = (TextView) view.findViewById(R.id.wifi_text);
        mobile_text = (TextView) view.findViewById(R.id.mobile_text);
        mac_address = (TextView) view.findViewById(R.id.mac_address);
        gateway = (TextView) view.findViewById(R.id.gateway);
        internal_ip = (TextView) view.findViewById(R.id.int_ip);
        external_ip = (TextView) view.findViewById(R.id.ext_ip_value);
        subnet_mask = (TextView) view.findViewById(R.id.subnet_mask);
        broadcast_ip = (TextView) view.findViewById(R.id.broadcast_ip);
        dns_primary = (TextView) view.findViewById(R.id.dns_primary);
        dns_secondary = (TextView) view.findViewById(R.id.dns_secondary);
        ssid = (TextView) view.findViewById(R.id.ssid);
        ssid_mac = (TextView) view.findViewById(R.id.ssid_mac);
        signal_strength = (TextView) view.findViewById(R.id.signal_strength);
        signal_strength_header = (TextView) view
                .findViewById(R.id.signal_strength_header);
        pingTest_text = (TextView) view.findViewById(R.id.ping_test_text);
        speedTest_text = (TextView) view.findViewById(R.id.speed_test_text);

        label_gateway = (TextView) view.findViewById(R.id.gateway_header);
        label_subnet = (TextView) view.findViewById(R.id.subnet_header);
        label_broadcast = (TextView) view.findViewById(R.id.broadcast_header);

        mac_address.setText(R.string.loading);
        gateway.setText(R.string.loading);
        internal_ip.setText(R.string.loading);
        subnet_mask.setText(R.string.loading);
        broadcast_ip.setText(R.string.loading);
        dns_primary.setText(R.string.loading);
        dns_secondary.setText("");
        ssid.setText(R.string.loading);
        ssid_mac.setText(R.string.loading);

        runPingAndSpeedTests();
    }

    public void runPingAndSpeedTests() {
        // check if WIFI or MOBILE are both available first..
        new PingTest().execute(null, null, null);
        new DownloadSpeedTest().execute(null, null, null);
    }

    public void getActiveInterfaces() {
        if (NetworkChangeReceiver.getInterface().equals("WIFI")) {
            getWIFIInfo();
            wifi_text.setTextColor(Color.parseColor("#00a33d"));
            wifi_text.setShadowLayer(25, 0, 0, Color.parseColor("#00a33d"));
            mobile_text.setTextColor(Color.parseColor("#f91b23"));
            mobile_text.setShadowLayer(25, 0, 0, Color.parseColor("#f91b23"));
            network_interface = "WIFI";

        } else if (NetworkChangeReceiver.getInterface().equals("MOBILE")) {
            getMOBILEInfo();
            mobile_text.setTextColor(Color.parseColor("#00a33d"));
            mobile_text.setShadowLayer(25, 0, 0, Color.parseColor("#00a33d"));
            wifi_text.setTextColor(Color.parseColor("#f91b23"));
            wifi_text.setShadowLayer(25, 0, 0, Color.parseColor("#f91b23"));
            network_interface = "MOBILE";
        }
    }

    public void getWIFIInfo() {
        try {
            wifi_manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifi = wifi_manager.getConnectionInfo();
            // getDhcpInfo is depreciated, should be using
            // ConnectivityManager.getLinkProperties()
            // Method is still hidden from use for now
            // Will have to check back after 11/2013
            DhcpInfo dhcp = wifi_manager.getDhcpInfo();

            label_gateway.setText("Gateway IP:");
            label_subnet.setText("Subnet Mask:");
            label_broadcast.setText("Broadcast IP:");

            dns_primary.setText(convertIntToAddr(dhcp.dns1));
            dns_secondary.setText(convertIntToAddr(dhcp.dns2));
            gateway.setText(convertIntToAddr(dhcp.gateway));
            try {
                InterfaceAddress ipAddr = getInterfaceAddress(NetworkInterface
                        .getByName("wlan0"));
                internal_ip.setText(ipAddr.getAddress().getHostAddress());
                subnet_mask.setText(convertIntToAddr(dhcp.netmask));
                broadcast_ip.setText(ipAddr.getBroadcast().getHostAddress());
            } catch (NullPointerException ex) {
                internal_ip.setText("0.0.0.0");
                subnet_mask.setText("0.0.0.0");
                broadcast_ip.setText("0.0.0.0");
            }
            mac_address.setText(getMAC(NetworkInterface.getByName("wlan0")));
            new GetExternalIP().execute(null, null, null);
            ssid.setText(wifi.getSSID().replace('"', ' '));
            ssid_mac.setText(wifi.getBSSID().toUpperCase());
            int rssi = wifi.getRssi();
            int wireless_Strength = WifiManager.calculateSignalLevel(rssi, 100);
            wirelessStrengthGraph(wireless_Strength);
            signal_strength.setText(rssi + " dBm -- " + wireless_Strength
                    + " %");
        } catch (Exception ex) {

        }
    }

    public void getMOBILEInfo() {
        try {
            NetworkInterface mobile_network = null;
            if (NetworkInterface.getNetworkInterfaces() != null) {
                List<NetworkInterface> interfaces = Collections
                        .list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface inter : interfaces) {
                    if (!inter.isLoopback() && inter.isUp() && !inter.getDisplayName().equals("wlan0") && !inter.getDisplayName().contains("p2p")) {
                        List<InterfaceAddress> addrs = inter.getInterfaceAddresses();
                        if (addrs.size() >= 1) {
                            InterfaceAddress ipAddr = getInterfaceAddress(inter);
                            if (ipAddr != null) {
                                InetAddress mAddr = ipAddr.getAddress();
                                if (mAddr != null) {
                                    String ip = mAddr.getHostAddress();
                                    if (ip != null) {
                                        mobile_network = inter;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            label_gateway.setText("Phone Type:");
            label_subnet.setText("Network Type:");
            setPhoneNetworkType();

            telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            telManager.listen(phoneState ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            try{
                InterfaceAddress ipAddr = getInterfaceAddress(mobile_network);
                internal_ip.setText(ipAddr.getAddress().getHostAddress());
            }
            catch(NullPointerException ex){
                internal_ip.setText("0.0.0.0");
            }
            mac_address.setText(R.string.not_applicable);
            new GetExternalIP().execute(null, null, null);
            dns_primary.setText(R.string.not_applicable);
            dns_secondary.setText("");
            ssid.setText(R.string.not_applicable);
            ssid_mac.setText(R.string.not_applicable);
            int signal = phoneState.returnSignal();
            String signalString = phoneState.returnSignalString();
            signal_strength.setText(signalString);
            wirelessStrengthGraph(signal);
        }
        catch(Exception ex){

        }
    }

    public void setPhoneNetworkType(){
        TelephonyManager tele = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int phoneType = tele.getPhoneType();
        int networkType = tele.getNetworkType();

        if(phoneType == TelephonyManager.PHONE_TYPE_CDMA)
            gateway.setText("CDMA");
        if(phoneType == TelephonyManager.PHONE_TYPE_GSM)
            gateway.setText("GSM");
        if(phoneType == TelephonyManager.PHONE_TYPE_SIP)
            gateway.setText("SIP");
        if(phoneType == TelephonyManager.PHONE_TYPE_NONE)
            gateway.setText("NONE");

        if(networkType == TelephonyManager.NETWORK_TYPE_1xRTT)
            subnet_mask.setText("1xRTT");
        if(networkType == TelephonyManager.NETWORK_TYPE_CDMA)
            subnet_mask.setText("CDMA");
        if(networkType == TelephonyManager.NETWORK_TYPE_EDGE)
            subnet_mask.setText("EDGE");
        if(networkType == TelephonyManager.NETWORK_TYPE_EHRPD)
            subnet_mask.setText("EHRPD");
        if(networkType == TelephonyManager.NETWORK_TYPE_EVDO_0)
            subnet_mask.setText("EVDO rev. 0");
        if(networkType == TelephonyManager.NETWORK_TYPE_EVDO_A)
            subnet_mask.setText("EVDO rev. A");
        if(networkType == TelephonyManager.NETWORK_TYPE_EVDO_B)
            subnet_mask.setText("EVDO rev. B");
        if(networkType == TelephonyManager.NETWORK_TYPE_GPRS)
            subnet_mask.setText("GPRS");
        if(networkType == TelephonyManager.NETWORK_TYPE_HSDPA)
            subnet_mask.setText("HSDPA");
        if(networkType == TelephonyManager.NETWORK_TYPE_HSPA)
            subnet_mask.setText("HSPA");
        if(networkType == TelephonyManager.NETWORK_TYPE_HSPAP)
            subnet_mask.setText("HSPA+");
        if(networkType == TelephonyManager.NETWORK_TYPE_HSUPA)
            subnet_mask.setText("HSUPA");
        if(networkType == TelephonyManager.NETWORK_TYPE_IDEN)
            subnet_mask.setText("iDen");
        if(networkType == TelephonyManager.NETWORK_TYPE_LTE)
            subnet_mask.setText("LTE");
        if(networkType == TelephonyManager.NETWORK_TYPE_UMTS)
            subnet_mask.setText("UMTS");
        if(networkType == TelephonyManager.NETWORK_TYPE_UNKNOWN)
            subnet_mask.setText("Unknown");

        broadcast_ip.setText("");
        label_broadcast.setText("");
    }

    public String getInterface() {
        String network = "";
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInterface = cm.getActiveNetworkInfo();
        if (netInterface.isConnected()) {
            network = netInterface.getTypeName();
            if (network.equals("mobile")) {
                network = network.toUpperCase(Locale.US);
            }
        }
        return network;
    }

    public InterfaceAddress getInterfaceAddress(NetworkInterface network) {
        List<InterfaceAddress> addrs = network.getInterfaceAddresses();
        for (int i = 0; i < addrs.size(); i++)
            if (addrs.size() > 0) {
                for (InterfaceAddress addr : addrs) {
                    String ip = addr.getAddress().getHostAddress();
                    if (!addr.getAddress().isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip))
                        return addr;
                }
            }

        return null;
    }

    public String convertIntToAddr(int i) {
        byte[] b = BigInteger.valueOf(i).toByteArray();
        for (int j = 0; j < b.length / 2; j++) {
            byte temp = b[b.length - j - 1];
            b[b.length - j - 1] = b[j];
            b[j] = temp;
        }
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(b);
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            return "0.0.0.0";
        }
    }

    public String getMAC(NetworkInterface network) {
        byte[] mac = null;
        try {
            mac = network.getHardwareAddress();
            StringBuilder buf = new StringBuilder();
            for (int idx = 0; idx < mac.length; idx++)
                buf.append(String.format("%02X:", mac[idx]));
            if (buf.length() > 0)
                buf.deleteCharAt(buf.length() - 1);
            return buf.toString();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    public void wirelessStrengthGraph(int wifi) {
        ImageView image = (ImageView) view
                .findViewById(R.id.wifi_signal_strength_bar);
        if (wifi <= 10) {
            image.setImageResource(R.drawable.ten_per_signal);
        } else if (wifi > 10 && wifi <= 30) {
            image.setImageResource(R.drawable.thirty_per_signal);
        } else if (wifi > 30 && wifi <= 50) {
            image.setImageResource(R.drawable.fifty_per_signal);
        } else if (wifi > 50 && wifi <= 70) {
            image.setImageResource(R.drawable.seventy_per_signal);
        } else if (wifi > 70 && wifi <= 85) {
            image.setImageResource(R.drawable.eightyfive_per_signal);
        } else if (wifi > 85) {
            image.setImageResource(R.drawable.hundred_per_signal);
        }
    }

    @Override
    public void onStart() {
        // Create a new runnable object, it calls setupGUI() which updates
        // the GUI every <interval> milliseconds -> currently 5 sec
        status = new Runnable() {

            @Override
            public void run() {
                getActiveInterfaces();
                handle.postDelayed(this, interval);
            }

        };
        // Actually starts to run the Runnable object
        handle.post(status);
        super.onStart();
    }

    @Override
    public void onDestroy() {
        handle.removeCallbacks(status);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        handle.removeCallbacks(status);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public class MobilePhoneListener extends PhoneStateListener {
        int signal = 100;
        String signalLevel = "";

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            int dbm_strength = signalStrength.getCdmaDbm();
            signal = WifiManager.calculateSignalLevel(dbm_strength, 100);
            signalLevel = dbm_strength + " dBm -- " + signal + " %";
        }

        public int returnSignal() {
            return signal;
        }

        public String returnSignalString() {
            return signalLevel;
        }

    }

    public class PingTest extends AsyncTask<String, Void, Integer> {

        TextView ping_google = (TextView) rootView
                .findViewById(R.id.ping_test_text);
        Button btn_pingTest = (Button) rootView.findViewById(R.id.ping_test);

        @Override
        protected Integer doInBackground(String... params) {
            int time = 0;
            try {
                while (time <= 10) {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlConn = (HttpURLConnection) url
                            .openConnection();
                    urlConn.setUseCaches(false);
                    urlConn.setConnectTimeout(2000); // mTimeout is in seconds
                    urlConn.setReadTimeout(4000);
                    long startTime = System.currentTimeMillis();
                    urlConn.connect();
                    long endTime = System.currentTimeMillis();
                    time = (int) (endTime - startTime);
                    urlConn.disconnect();
                }

            } catch (MalformedURLException e) {
                return -1;
            } catch (IOException e) {
                return -1;
            }
            return time;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result < 0) {
                ping_google.setText("Ping Failed");
            } else {
                ping_google.setText(result + " ms");
            }
            btn_pingTest.setEnabled(true);
        }

    }

    public class DownloadSpeedTest extends AsyncTask<String, String, String> {
        String label = "Kbps";

        TextView speed_test = (TextView) rootView
                .findViewById(R.id.speed_test_text);
        Button btn_Speedtest = (Button) rootView.findViewById(R.id.speed_test);

        @Override
        protected String doInBackground(String... params) {
            int expectedSize = 20480; // in bytes
            double txSpeed = 0.0;
            InputStream stream = null;
            String urlPath = "http://www.google.com/intl/en/policies/terms";
            try {
                URL url = new URL(urlPath);
                @SuppressWarnings("unused")
                int currentByte = 0;
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setUseCaches(false);
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(5000);
                long startTime = System.currentTimeMillis();
                stream = connection.getInputStream();
                while ((currentByte = stream.read()) != -1) {
                }
                long endTime = System.currentTimeMillis();
                long longTime = (endTime - startTime);
                Long l_time = Long.valueOf(longTime);
                Integer intTime = Integer.valueOf(l_time.intValue());
                double txTime = (double) intTime / 1000;
                txSpeed = ((expectedSize / txTime) / 1024) * 8;
                if (txSpeed >= 1024) {
                    label = "Mbps";
                    txSpeed /= 1024;
                }
                stream.close();
                connection.disconnect();
            } catch (MalformedURLException e1) {
                return "fail";
            } catch (IOException e) {
                return "fail";
            }

            DecimalFormat df = new DecimalFormat("#.#");
            return "" + df.format(txSpeed);
        }

        @Override
        protected void onPostExecute(String results) {
            if (results.equals("fail")) {
                speed_test.setText("Download Failed");
            } else {
                speed_test.setText(results + " " + label);
            }
            btn_Speedtest.setEnabled(true);
        }

    }

    public class GetExternalIP extends AsyncTask<Void, Void, String> {

        TextView t = (TextView) rootView.findViewById(R.id.ext_ip_value);

        @Override
        protected String doInBackground(Void... arg0) {
            String ip = "";
            String host1 = "http://checkip.amazonaws.com/";
            // String host1 = "http://api.externalip.net/ip";
            // String host1 = "http://icanhazip.com";
            HttpEntity entity = null;
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 2000);
            HttpConnectionParams.setSoTimeout(params, 3000);
            HttpClient httpclient = new DefaultHttpClient(params);
            try {
                // Try 1st request
                HttpGet request1 = new HttpGet(host1);
                HttpResponse response1 = httpclient.execute(request1);
                if (response1.getStatusLine().toString().contains("200 OK")) {
                    entity = response1.getEntity();
                }
                // request1.abort();
                if (entity != null) {
                    long len = entity.getContentLength();
                    if (len != -1 && len < 1024) {
                        ip = EntityUtils.toString(entity);
                    } else {
                        ip = "Response too long or error.";
                    }
                } else {
                    ip = "Null entity";
                }
            } catch (ClientProtocolException e) {
                return "fail";
            } catch (IOException e) {
                return "Fail";
            }
            return ip;
        }

        @Override
        protected void onPostExecute(String results) {
            if (results.equals("fail")) {
                t.setText("Request Failed");
            } else {
                t.setText(results.trim().replace("\n", "").replace("\r", ""));
            }
        }

    }

}
