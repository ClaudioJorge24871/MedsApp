package com.example.medsapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store and do simple CRUD on devices
 */
public class DeviceStorage {
    private static final String PREF_NAME = "devices_prefs";
    private static final String DEVICES_KEY = "device_ids";

    /**
     * Store the devices list
     * @param context
     * @param devices
     */
    public static void saveDevices(Context context, List<Device> devices) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray array = new JSONArray();
        for (Device device : devices) {
            try {
                array.put(device.toJSON());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        editor.putString(DEVICES_KEY, array.toString());
        editor.apply();
    }

    /**
     * Gets all stored devices
     * @param context
     * @return
     */
    public static List<Device> getDevices (Context context){
        List<Device> devices = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(DEVICES_KEY, null);

        if (jsonString != null){
            try{
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++){
                    JSONObject obj = jsonArray.getJSONObject(i);
                    devices.add(Device.fromJSON(obj));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return devices;
    }


    /**
     * Add a new Device
     * @param context
     * @param newDevice
     */
    public static void addDevice(Context context, Device newDevice){
        List<Device> devices = getDevices(context);
        for (Device device : devices) {
            if (device.getId().equals(newDevice.getId())) {
                return;
            }
        }

        devices.add(newDevice);
        saveDevices(context, devices);
    }

    /**
     * Remove a device
     * @param context
     * @param device
     */
    public static void removeDevice(Context context, Device device){
        List<Device> devices = getDevices(context);
        devices.remove(device);
        saveDevices(context, devices);
    }
}
