package com.wztlei.tanktrouble.match;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.datahouse.Bluetooth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "WL/UserUtils";

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private Set<BluetoothDevice> nearDevices = new HashSet<>();
    ListView pairedListView, nearListView;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            System.out.println("run method");
            nearDevices.clear();
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                nearDevices.add(device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        pairedListView = (ListView)findViewById(R.id.bluetoothPairedListView);
        nearListView = (ListView)findViewById(R.id.bluetoothNearListView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
           Log.d(TAG, "Device doesn't support Bluetooth");
        }
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                 642);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public void findDevices(View v) {
        System.out.println("findDevices");
        if (bluetoothAdapter.isDiscovering()) {
            // Bluetooth is already in modo discovery mode, we cancel to restart it again
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        ArrayList list = new ArrayList();
        for(BluetoothDevice bluetoothDevice : nearDevices) {
            list.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
        }
        Toast.makeText(getApplicationContext(), "Showing Near Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        nearListView.setAdapter(adapter);
    }

    public void on(View v){
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v){
        bluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
    }


    public void visible(View v){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, 0);
    }

    public void listPairedDevice(View v){
        pairedDevices = bluetoothAdapter.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice bluetoothDevice : pairedDevices) {
            list.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
        }
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        pairedListView.setAdapter(adapter);
    }
}