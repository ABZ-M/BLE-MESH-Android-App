package com.example.mesh_final;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

public class MyBluetoothDevice implements Parcelable {
    private String name;
    private BluetoothDevice device;

    public MyBluetoothDevice(String name, BluetoothDevice device) {
        this.name = name;
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    // Parcelable implementation
    protected MyBluetoothDevice(Parcel in) {
        name = in.readString();
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public static final Creator<MyBluetoothDevice> CREATOR = new Creator<MyBluetoothDevice>() {
        @Override
        public MyBluetoothDevice createFromParcel(Parcel in) {
            return new MyBluetoothDevice(in);
        }

        @Override
        public MyBluetoothDevice[] newArray(int size) {
            return new MyBluetoothDevice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(device, flags);
    }
}
