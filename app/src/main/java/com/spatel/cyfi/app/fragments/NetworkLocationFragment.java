package com.spatel.cyfi.app.fragments;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spatel.cyfi.app.NetworkChangeReceiver;
import com.spatel.cyfi.app.R;
import com.spatel.cyfi.app.db.DatabaseHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import gps.GPSTracker;

/**
 * Created by sheilpatelmac on 5/26/14.
 */
public class NetworkLocationFragment extends Fragment {
    GPSTracker gps;
    GoogleMap mMap;
    Context context;
    final int interval = 30000;
    Handler handle = new Handler();
    Runnable status;
    LatLng latLngUser = null;
    LatLng wifi;
    Location wifiLocation;
    Location mobLocation;
    boolean one = true;
    private static View rootView;
    DatabaseHandler db;
    String network_interface = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if(rootView != null){
            ViewGroup parent = (ViewGroup)rootView.getParent();
            if(parent != null)
                parent.removeView(rootView);
        }

        try{
            //Map doesn't exist, make it
            rootView = inflater.inflate(R.layout.network_location_fragment,container, false);
        }
        catch(InflateException e){
            //Map already exists
        }
        context = rootView.getContext();
        network_interface = NetworkChangeReceiver.getInterface();
        mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        db = new DatabaseHandler(rootView.getContext());
        gps = new GPSTracker(rootView.getContext());
        setHasOptionsMenu(true);
        if (!gps.canGetLocation()) {

            // location should probably always be found, but in case it is not,
            // take user to Location Settings to
            // enable proper location services

            AlertDialog.Builder locationNotFoundAlert = new AlertDialog.Builder(
                    rootView.getContext());

            // Setting Dialog Title
            locationNotFoundAlert.setTitle("Location Not Found");

            // Setting Dialog Message
            locationNotFoundAlert
                    .setMessage("Location Cannot Be Determined \n\nWould you like to go to Location Settings?");

            // Setting Positive "Yes" Btn
            locationNotFoundAlert.setPositiveButton("Settings",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Write your code here to execute after dialog
                            Intent viewIntent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(viewIntent);
                        }
                    });
            // Setting Negative "NO" Btn
            locationNotFoundAlert.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            // Showing Alert Dialog
            locationNotFoundAlert.show();
        }
        getFirstGPSLocation();
        return rootView;
    }

    @Override
    public void onStart() {
        // Create a new runnable object, it calls setupGUI() which updates
        // the GUI every <interval> milliseconds -> currently 5 sec
        status = new Runnable() {

            @Override
            public void run() {
                getGPSLocation();
                getServiceLocation();
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

    public LatLng getGPSLocation(){
        mMap.clear();
        if(gps != null){
            latLngUser = new LatLng(gps.getLatitude(), gps.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLngUser).title("Your Location"));
        }
        else{
            Toast.makeText(getView().getContext(), "Error Getting User Location", Toast.LENGTH_LONG).show();
        }
        return latLngUser;
    }

    public void getFirstGPSLocation() {
        mMap.clear();
        if(gps != null){
            latLngUser = new LatLng(gps.getLatitude(), gps.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLngUser).title("Your Location")).showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngUser, mMap.getCameraPosition().zoom));
        }
        else{
            Toast.makeText(getView().getContext(), "Error Getting User Location", Toast.LENGTH_LONG).show();
        }
    }

    public void getServiceLocation(){
        if (network_interface.equals("WIFI")) {
            try {
                wifi = new GetLocation_WIFI().execute(null, null, null).get();
                wifiLocation = new Location("WIFI");
                wifiLocation.setLatitude(wifi.latitude);
                wifiLocation.setLongitude(wifi.longitude);
                gps.setProviderLocation(wifiLocation, "WIFI");
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(wifiLocation.getLatitude(), wifiLocation.getLongitude()))
                        .title("Public IP Location")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            } catch (Exception e) {
                Toast.makeText(getView().getContext(), "Error Getting Public IP Location", Toast.LENGTH_LONG).show();
            }

        } else if (network_interface.equals("MOBILE")) {
            // mobile stuff here
            try{
                CellLocation.requestLocationUpdate();
                TelephonyManager tele = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                int phoneType = tele.getPhoneType();
                int networkType = tele.getNetworkType();
                if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    if(networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                        if(one == true) {
                            Toast.makeText(context, "LTE is not currenlty supported, please use WiFi", Toast.LENGTH_LONG).show();
                            one = false;
                        }
                    }
                    else {
                        CdmaCellLocation cdma = (CdmaCellLocation) tele.getCellLocation();
                        double stationLat = cdma.getBaseStationLatitude()/ (4.0 * 3600.0);
                        double stationLong = cdma.getBaseStationLongitude()/ (4.0 * 3600.0);
                        mobLocation = new Location("MOBILE");
                        mobLocation.setLatitude(stationLat);
                        mobLocation.setLongitude(stationLong);
                        gps.setProviderLocation(mobLocation, "MOBILE");
                        mMap.addMarker(new MarkerOptions().position(new LatLng(stationLat, stationLong)).title("Cell Tower Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    }

                }
                if(one == true){
                    if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                        Toast.makeText(context, "GSM is not currently supported, please use WiFi", Toast.LENGTH_SHORT).show();
                        one = false;
                    }
                }
            }
            catch(Exception e){
                Toast.makeText(getView().getContext(), "Error Getting Mobile Cell Location", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.location, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.go_to_me:
                if (latLngUser != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            latLngUser, mMap.getCameraPosition().zoom));
                } else {
                    Toast.makeText(getView().getContext(), "Can't Find Location",
                            Toast.LENGTH_SHORT).show();
                }

                return true;

            case R.id.map_type:
                AlertDialog.Builder builder = new AlertDialog.Builder(getView()
                        .getContext());
                CharSequence[] options = { "Normal", "Satellite", "Hybrid","Terrain" };
                builder.setTitle("Select Map Type").setItems(options,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    mMap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL);
                                } else if (which == 1) {
                                    mMap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE);
                                } else if (which == 2) {
                                    mMap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID);
                                } else {
                                    mMap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN);
                                }
                            }
                        });
                builder.show();
                return true;
            default:
                break;
        }

        return false;
    }

    public class GetLocation_WIFI extends AsyncTask<Void, Void, LatLng> {

        @Override
        protected LatLng doInBackground(Void... params) {
            String geoURL = "http://www.freegeoip.net/xml/";
            HttpEntity entity = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet(geoURL);
            try {
                HttpResponse response = httpclient.execute(request);
                entity = response.getEntity();
                String xml = EntityUtils.toString(entity);
                Document doc = this.getDomElement(xml);
                NodeList nl = doc.getElementsByTagName("Response");
                Element e = (Element) nl.item(0);
                double lat = Double.parseDouble(this.getValue(e, "Latitude"));
                double lon = Double.parseDouble(this.getValue(e, "Longitude"));
                //double lat = Double.parseDouble(split[7].replace("\"", ""));
                //double lon = Double.parseDouble(split[8].replace("\"", ""));
                LatLng latLngWifi = new LatLng(lat,lon);
                return latLngWifi;

            } catch (Exception e) {
                return null;
            }
        }

        public Document getDomElement(String xml){
            Document doc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is);

            } catch (Exception e) {

            }
            return doc;
        }

        public String getValue(Element item, String str) {
            NodeList n = item.getElementsByTagName(str);
            return this.getElementValue(n.item(0));
        }

        public final String getElementValue(Node elem ) {
            Node child;
            if( elem != null){
                if (elem.hasChildNodes()){
                    for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                        if( child.getNodeType() == Node.TEXT_NODE  ){
                            return child.getNodeValue();
                        }
                    }
                }
            }
            return "";
        }
    }

}
