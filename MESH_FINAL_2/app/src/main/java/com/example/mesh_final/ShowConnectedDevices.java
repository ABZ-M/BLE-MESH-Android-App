package com.example.mesh_final;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ShowConnectedDevices extends AppCompatActivity {

    List<MyBluetoothDevice> devicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_connected_devices);

        devicesList = getIntent().getParcelableArrayListExtra("devicesList");
    }
}
