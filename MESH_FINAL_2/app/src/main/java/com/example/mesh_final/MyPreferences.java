package com.example.mesh_final;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MyPreferences implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String CONNECTED_DEVICES_KEY = "connectedDevices";
    private static final String APP_OPENED_KEY = "appOpened";
    private static SharedPreferences mSharedPreferences;

    public MyPreferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public static boolean isAppOpenedForFirstTime() {
        return mSharedPreferences.getBoolean(APP_OPENED_KEY, true);
    }

    public static void setAppOpened(boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(APP_OPENED_KEY, value);
        editor.apply();
    }

    public static List<MyBluetoothDevice> getConnectedDevices() {
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(CONNECTED_DEVICES_KEY, null);
        Type type = new TypeToken<List<MyBluetoothDevice>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public static void setConnectedDevices(List<MyBluetoothDevice> connectedDevices) {
        Gson gson = new Gson();
        String json = gson.toJson(connectedDevices);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(CONNECTED_DEVICES_KEY, json);
        editor.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle changes to shared preferences
    }
}

