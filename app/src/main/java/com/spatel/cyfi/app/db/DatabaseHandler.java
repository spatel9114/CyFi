package com.spatel.cyfi.app.db;

/**
 * Created by sheilpatelmac on 5/26/14.
 */
import java.util.ArrayList;
import java.util.List;

import com.spatel.cyfi.app.PinInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cyfi_db";

    // Contacts table name
    private static final String TABLE_PINS = "pin_info";

    // Contacts Table Columns names
    private static final String KEY_ID = "key";
    private static final String KEY_DATE = "date";
    private static final String KEY_INT_IP = "internal_ip";
    private static final String KEY_EXT_IP = "external_ip";
    private static final String KEY_BROAD_IP = "broadcast_ip";
    private static final String KEY_DNS = "dns";
    private static final String KEY_GATEWAY = "gateway";
    private static final String KEY_SUBNET = "subnet";
    private static final String KEY_SSID = "ssid";
    private static final String KEY_SSID_MAC = "ssid_mac";
    private static final String KEY_PING = "ping";
    private static final String KEY_SPEEDTEST = "speedtest";
    private static final String KEY_SIG_STRENGTH = "signal_strength";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LONG = "longitude";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PINS + "("
                + KEY_ID + " INTEGER," + KEY_DATE + " TEXT,"
                + KEY_INT_IP + " TEXT," + KEY_EXT_IP + " TEXT," + KEY_BROAD_IP + " TEXT," + KEY_DNS + " TEXT," + KEY_GATEWAY + " TEXT,"
                + KEY_SUBNET + " TEXT," + KEY_SSID + " TEXT," + KEY_SSID_MAC + " TEXT," + KEY_PING + " TEXT," + KEY_SPEEDTEST + " TEXT,"
                + KEY_SIG_STRENGTH + " TEXT," + KEY_LAT + " REAL," + KEY_LONG + " REAL"+")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PINS);
        onCreate(db);

    }

    public void deleteAllPins() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PINS);
    }

    public int getAvailablePKey() {
        SQLiteDatabase db = this.getWritableDatabase();
        for(int i = 0; i < 10; i++) {
            String query = "SELECT "+KEY_ID+" FROM " + TABLE_PINS +" WHERE "+KEY_ID+" = "+i;
            Cursor cursor = db.rawQuery(query, null);
            if(cursor.getCount() == 0) {
                return i;
            }
        }
        return -1;
    }

    // Adding new contact
    public void addPin(PinInfo pin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, pin.getKey());
        values.put(KEY_DATE, pin.getDate());
        values.put(KEY_INT_IP, pin.getInternal_ip());
        values.put(KEY_EXT_IP, pin.getExternal_ip());
        values.put(KEY_BROAD_IP, pin.getBroadcast_ip());
        values.put(KEY_DNS, pin.getDns());
        values.put(KEY_GATEWAY, pin.getGateway());
        values.put(KEY_SUBNET, pin.getSubnet());
        values.put(KEY_SSID, pin.getSsid());
        values.put(KEY_SSID_MAC, pin.getSsid_mac());
        values.put(KEY_PING, pin.getPing());
        values.put(KEY_SPEEDTEST, pin.getSpeedtest());
        values.put(KEY_SIG_STRENGTH, pin.getSignal_strength());
        values.put(KEY_LAT, pin.getLatitude());
        values.put(KEY_LONG, pin.getLongitude());

        // Inserting Row
        db.insert(TABLE_PINS, null, values);
    }

    // Getting single contact
    public PinInfo getPin(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PINS +" WHERE "+KEY_ID+" = "+id;
        Cursor cursor = db.rawQuery(query, null);
        PinInfo pin = null;
        if(cursor.moveToFirst()){
            pin = new PinInfo(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9),
                    cursor.getString(10), cursor.getString(11), cursor.getString(12), cursor.getDouble(13), cursor.getDouble(14));
        }
        return pin;
    }

    // Getting All Contacts
    public List<PinInfo> getAllPins() {
        List<PinInfo> contactList = new ArrayList<PinInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PINS + " ORDER BY " + KEY_DATE + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PinInfo pin = new PinInfo();
                pin.setKey(cursor.getInt(0));
                pin.setDate(cursor.getString(1));
                pin.setInternal_ip(cursor.getString(2));
                pin.setExternal_ip(cursor.getString(3));
                pin.setBroadcast_ip(cursor.getString(4));
                pin.setDns(cursor.getString(5));
                pin.setGateway(cursor.getString(6));
                pin.setSubnet(cursor.getString(7));
                pin.setSsid(cursor.getString(8));
                pin.setSsid_mac(cursor.getString(9));
                pin.setPing(cursor.getString(10));
                pin.setSpeedtest(cursor.getString(11));
                pin.setSignal_strength(cursor.getString(12));
                pin.setLatitude(cursor.getDouble(13));
                pin.setLongitude(cursor.getDouble(14));

                // Adding contact to list
                contactList.add(pin);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // Getting pin Count
    public int getPinCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PINS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    // Updating single pin
    public int updatePin(PinInfo pin) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, pin.getDate());
        values.put(KEY_INT_IP, pin.getInternal_ip());
        values.put(KEY_EXT_IP, pin.getExternal_ip());
        values.put(KEY_BROAD_IP, pin.getBroadcast_ip());
        values.put(KEY_DNS, pin.getDns());
        values.put(KEY_GATEWAY, pin.getGateway());
        values.put(KEY_SUBNET, pin.getSubnet());
        values.put(KEY_SSID, pin.getSsid());
        values.put(KEY_SSID_MAC, pin.getSsid_mac());
        values.put(KEY_PING, pin.getPing());
        values.put(KEY_SPEEDTEST, pin.getSpeedtest());
        values.put(KEY_SIG_STRENGTH, pin.getSignal_strength());
        values.put(KEY_LAT, pin.getLatitude());
        values.put(KEY_LONG, pin.getLongitude());

        // updating row
        return db.update(TABLE_PINS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(pin.getKey()) });
    }

    // Deleting single contact
    public void deletePin(PinInfo pin) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PINS +" WHERE "+KEY_ID+" = "+pin.getKey());
    }


}
