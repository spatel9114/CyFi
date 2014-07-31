package com.spatel.cyfi.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spatel.cyfi.app.PinInfo;
import com.spatel.cyfi.app.R;
import com.spatel.cyfi.app.db.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

import gps.GPSTracker;

public class NetworkHistoryFragment extends Fragment{
    GPSTracker gps;
    GoogleMap mMap;
    private static View rootView;
    public static float[] color = {BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_YELLOW, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_AZURE};
    public static CharSequence[] markerColor = {"Blue Pin", "Red Pin", "Purple Pin", "Green Pin", "Orange Pin", "Cyan Pin", "Magenta Pin", "Yellow Pin", "Rose Pin", "Azure Pin"};
    static List<List<String>> markers = new ArrayList<List<String>>();
    public List<LatLng> markerPos = new ArrayList<LatLng>();
    public List<PinInfo> pins;
    DatabaseHandler db;
    LatLng location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if(rootView != null){
            ViewGroup parent = (ViewGroup)rootView.getParent();
            if(parent != null)
                parent.removeView(rootView);
        }
        try{
            //Map doesn't exist, make it
            rootView = inflater.inflate(R.layout.fragment_network_history,container, false);
        }
        catch(InflateException e){
            //Map already exists
        }
        setHasOptionsMenu(true);
        mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.history_map)).getMap();
        mMap.setInfoWindowAdapter(new customInfoWindow(inflater));
        db = new DatabaseHandler(rootView.getContext());
        gps = new GPSTracker(rootView.getContext());
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
        location = new LatLng(gps.getLatitude(), gps.getLongitude());
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.history, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        placePins();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear_single_snapshot:
                AlertDialog.Builder snapshot_chooser = new AlertDialog.Builder(getView().getContext());
                int pinCount = db.getPinCount();
                CharSequence[] popup = new CharSequence[pinCount];
                for(int i = 0; i < pinCount; i++){
                    popup[i] = markerColor[i];
                }
                pins = db.getAllPins();
                snapshot_chooser.setTitle("Select Snapshot to Delete").setItems(popup, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int choose) {
                        try{
                            PinInfo toRemove = pins.get(choose);
                            db.deletePin(toRemove);
                            placePins();
                            shakeGoogleMap();
                            Toast.makeText(getActivity(), "Single Snapshot Cleared!", Toast.LENGTH_LONG).show();
                        }
                        catch(NullPointerException ex){
                            Toast.makeText(getActivity(), "Error Removing Pin, Please Try Again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                snapshot_chooser.show();
                return true;

            case R.id.clear_history_map_tab:
                mMap.clear();
                db.deleteAllPins();
                Toast.makeText(getActivity(), "All Snapshots Cleared!", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void placePins(){
        try{
            pins = db.getAllPins();
            mMap.clear();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, mMap.getCameraPosition().zoom));
            for(int pinCount = 0; pinCount < pins.size(); pinCount++){
                PinInfo pin = pins.get(pinCount);
                LatLng pos = new LatLng(pin.getLatitude(), pin.getLongitude());
                if(pos != null){
                    mMap.addMarker(new MarkerOptions().position(pos).title(""+pinCount).icon(BitmapDescriptorFactory.defaultMarker(color[pinCount])));
                }
            }
        }
        catch(Exception ex){
            Toast.makeText(getActivity(), "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void shakeGoogleMap(){
        LatLng oldPos = mMap.getCameraPosition().target;
        LatLng newPos = new LatLng((oldPos.latitude+0.001), (oldPos.longitude+0.001));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPos, mMap.getCameraPosition().zoom));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(oldPos, mMap.getCameraPosition().zoom));
    }

    class customInfoWindow implements InfoWindowAdapter{
        View popup;

        public customInfoWindow(LayoutInflater inflater){
            popup = inflater.inflate(R.layout.infowindow, null);
        }

        //Add code into infocontents, need xml
        @Override
        public View getInfoContents(Marker marker) {
            TextView tv_date = (TextView)popup.findViewById(R.id.iw_date);
            TextView tv_i_ip = (TextView)popup.findViewById(R.id.iw_internalip);
            TextView tv_e_ip = (TextView)popup.findViewById(R.id.iw_externalip);
            TextView tv_dns = (TextView)popup.findViewById(R.id.iw_dns);
            TextView tv_gw = (TextView)popup.findViewById(R.id.iw_gw);
            TextView tv_sn = (TextView)popup.findViewById(R.id.iw_sn);
            TextView tv_bc = (TextView)popup.findViewById(R.id.iw_broadcast);
            TextView tv_ssid = (TextView)popup.findViewById(R.id.iw_ssid);
            TextView tv_ssidmac = (TextView)popup.findViewById(R.id.iw_ssidmac);
            TextView tv_ping = (TextView)popup.findViewById(R.id.iw_ping);
            TextView tv_speed = (TextView)popup.findViewById(R.id.iw_speed);
            TextView tv_signalstrength = (TextView)popup.findViewById(R.id.iw_signalstrength);

            try{
                PinInfo data = pins.get(Integer.parseInt(marker.getTitle()));

                tv_date.setText("Date: "+data.getDate());
                tv_i_ip.setText("Internal IP: "+data.getInternal_ip());
                tv_e_ip.setText("External IP: "+data.getExternal_ip());
                tv_dns.setText("DNS: "+data.getDns());
                tv_gw.setText("Gateway: "+data.getGateway());
                tv_sn.setText("Subnet: "+data.getSubnet());
                tv_bc.setText("Broadcast: "+data.getBroadcast_ip());
                tv_ssid.setText("SSID: "+data.getSsid());
                tv_ssidmac.setText("SSID MAC: "+data.getSsid_mac());
                tv_signalstrength.setText("Signal Strength: "+data.getSignal_strength());
                tv_ping.setText("Ping Test: " + data.getPing());
                tv_speed.setText("Speed Test: "+data.getSpeedtest());
            }
            catch(NullPointerException ex){

            }
            return popup;
        }

        @Override
        public View getInfoWindow(Marker arg0) {
            // TODO Auto-generated method stub
            return null;
        }

    }

}