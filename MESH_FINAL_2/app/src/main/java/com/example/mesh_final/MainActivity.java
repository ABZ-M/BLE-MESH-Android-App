package com.example.mesh_final;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.mesh.MeshManagerCallbacks;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.provisionerstates.ProvisioningState;
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.mesh.transport.ControlMessage;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;


public class MainActivity extends AppCompatActivity implements MeshManagerCallbacks {

    private Button devicesButton;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;
    private List<ScanResult> scanResults;
    private ArrayAdapter<ScanResult> scanResultAdapter;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private MyPreferences mPreferences;
    private BluetoothDevice selectedDevice;
    //Array list for storing bluetooth Devices
    List<MyBluetoothDevice> connectedDevices = new ArrayList<>();


    private BleMesh bleMesh;




    /////////////////////////////////////////////////////////////////////////////////////////////////////





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = new MyPreferences(this);

        bleMesh = new BleMesh(this, this) {
            @Override
            public void onTransactionFailed(int dst, boolean hasIncompleteTimerExpired) {

            }

            @Override
            public void onUnknownPduReceived(int src, byte[] accessPayload) {

            }

            @Override
            public void onBlockAcknowledgementProcessed(int dst, @NonNull ControlMessage message) {

            }

            @Override
            public void onBlockAcknowledgementReceived(int src, @NonNull ControlMessage message) {

            }

            @Override
            public void onMeshMessageProcessed(int dst, @NonNull MeshMessage meshMessage) {

            }

            @Override
            public void onMeshMessageReceived(int src, @NonNull MeshMessage meshMessage) {

            }

            @Override
            public void onMessageDecryptionFailed(String meshLayer, String errorMessage) {

            }

            @Override
            public void onProvisioningStateChanged(UnprovisionedMeshNode meshNode, ProvisioningState.States state, @Nullable byte[] data) {

            }

            @Override
            public void onProvisioningFailed(UnprovisionedMeshNode meshNode, ProvisioningState.States state, byte[] data) {

            }

            @Override
            public void onProvisioningCompleted(ProvisionedMeshNode meshNode, ProvisioningState.States state, byte[] data) {

            }

            @Override
            public void onNetworkLoaded(MeshNetwork meshNetwork) {

            }

            @Override
            public void onNetworkUpdated(MeshNetwork meshNetwork) {

            }

            @Override
            public void onNetworkLoadFailed(String error) {

            }

            @Override
            public void onNetworkImported(MeshNetwork meshNetwork) {

            }

            @Override
            public void onNetworkImportFailed(String error) {

            }

            @Override
            public void sendProvisioningPdu(UnprovisionedMeshNode meshNode, byte[] pdu) {

            }

            @Override
            public void onMeshPduCreated(byte[] pdu) {

            }

            @Override
            public int getMtu() {
                return 0;
            }

            @Override
            public void onMeshDataReceived(String data) {

            }
        };

        bleMesh.loadMeshNetwork();


        if (MyPreferences.isAppOpenedForFirstTime()) {
            // perform first-time initialization
            List<MyBluetoothDevice> connectedDevices = new ArrayList<>();
            MyPreferences.setConnectedDevices(connectedDevices);
            MyPreferences.setAppOpened(false);
        } else {
            // retrieve the connected devices from shared preferences
            List<MyBluetoothDevice> connectedDevices = MyPreferences.getConnectedDevices();
        }

        //button for moving to devices activity
        devicesButton = (Button) findViewById(R.id.devicesButton);
        devicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDevicesPage();
            }
        });

        // Initialize BluetoothAdapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize scan results list and adapter
        scanResults = new ArrayList<>();
        scanResultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scanResults);

        // Initialize scan callback
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                // Check if the scanned device has the desired name
                @SuppressLint("MissingPermission")
                String deviceName = result.getDevice().getName();
                if (deviceName != null && deviceName.equals("ESP-BLE-MESH")) {
                    addScanResult(result);
                    scanResultAdapter.notifyDataSetChanged();

                    // Store the selected device
                    selectedDevice = result.getDevice();
                }
            }


            @Override
            public void onScanFailed(int errorCode) {
                Toast.makeText(MainActivity.this, "Scan failed with error code " + errorCode, Toast.LENGTH_SHORT).show();
            }
        };

        // Initialize scanner
        scanner = bluetoothAdapter.getBluetoothLeScanner();

        // Find and initialize ListView
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(scanResultAdapter);


        // Start scan on button click
        Button scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            } else {
                // Permission has already been granted
                startScan();
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected ScanResult object
                ScanResult selectedResult = scanResults.get(position);

                // Do something with the selected ScanResult object
                Toast.makeText(getApplicationContext(), selectedResult.getDevice().getName(), Toast.LENGTH_SHORT).show();
                // You can also store the selected device in the selectedDevice variable if needed
                selectedDevice = selectedResult.getDevice();



                //Adding Selected Device to BLE Devices Array List
                MyBluetoothDevice newDevice = new MyBluetoothDevice("Device 1", selectedDevice);
                connectedDevices.add(newDevice);
                ///////////////////////////////////////////////////////////////

                /////////////////////////////////////////////////////////////////


                //Gatt things
                BluetoothGatt bluetoothGatt = selectedDevice.connectGatt(MainActivity.this, false, gattCallback);
            }
        });


    }

    //Gatt Call Back STUFF

    // Gatt Call Back STUFF
    // Gatt Call Back STUFF
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // The device is connected
                Log.i(TAG, "Connected to GATT server.");

                // Discover GATT services
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // The device is disconnected
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // GATT services are discovered
                Log.i("BLE", "Services discovered.");

                // Get the service UUIDs
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    String serviceUuid = service.getUuid().toString();
                    Log.d(TAG, "Service UUID: " + serviceUuid);
                }

                // Get the characteristic UUIDs for each service
                for (BluetoothGattService service : services) {
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        String characteristicUuid = characteristic.getUuid().toString();
                        Log.d(TAG, "Characteristic UUID: " + characteristicUuid);

                        // Check if the characteristic UUID matches the desired UUID
                        if (characteristicUuid.equals("00001801-0000-1000-8000-00805f9b34fb")) {
                            // Read the value of the characteristic
                            gatt.readCharacteristic(characteristic);
                        }
                    }
                }
            } else {
                // Failed to discover GATT services
                Log.w("BLE", "Service discovery failed with status: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Read the value of the characteristic
                byte[] value = characteristic.getValue();
                String uuidValue = new String(value);

                // Display the UUID value in the TextView
                TextView meshDataTextView = findViewById(R.id.mesh_data);
                meshDataTextView.setText(uuidValue);
            }
        }
    };




    //----------------------------------------------------------------------------------------------------


    //--------code for going to devices activity-----------//
    public void openDevicesPage(){
        Intent intent = new Intent(MainActivity.this, ShowConnectedDevices.class);
        //Sending BLE Devices array to
        intent.putExtra("devicesList", new ArrayList<>(connectedDevices));
        startActivity(intent);
    }
    //---------------------------------------------------//



    private void addScanResult(ScanResult result) {
        // Add scan result to list adapter
        String deviceAddress = result.getDevice().getAddress();
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult existingResult = scanResults.get(i);
            if (existingResult.getDevice().getAddress().equals(deviceAddress)) {
                scanResults.set(i, result);
                scanResultAdapter.notifyDataSetChanged();
                return;
            }
        }
        scanResults.add(result);
        scanResultAdapter.notifyDataSetChanged();
    }

    @SuppressLint("MissingPermission")
    private void startScan() {
        // Start Bluetooth LE scan
        scanResults.clear();
        scanResultAdapter.notifyDataSetChanged();
        scanner.startScan(scanCallback);
    }

    @SuppressLint("MissingPermission")
    private void stopScan() {
        // Stop Bluetooth LE scan
        scanner.stopScan(scanCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop Bluetooth LE scan before app is destroyed
        stopScan();
    }


    @Override
    public void onNetworkLoaded(MeshNetwork meshNetwork) {

    }

    @Override
    public void onNetworkUpdated(MeshNetwork meshNetwork) {

    }

    @Override
    public void onNetworkLoadFailed(String error) {

    }

    @Override
    public void onNetworkImported(MeshNetwork meshNetwork) {

    }

    @Override
    public void onNetworkImportFailed(String error) {

    }

    @Override
    public void sendProvisioningPdu(UnprovisionedMeshNode meshNode, byte[] pdu) {

    }

    @Override
    public void onMeshPduCreated(byte[] pdu) {

    }

    @Override
    public int getMtu() {
        return 0;
    }
}
