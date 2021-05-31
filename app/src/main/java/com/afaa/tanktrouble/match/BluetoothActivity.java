package com.afaa.tanktrouble.match;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.afaa.tanktrouble.R;

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
            System.out.println(bluetoothDevice.getUuids()[0]);
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
        pairedListView.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            System.out.println("when clicked" + info);
//            String address = info.substring(info.length() - 17);
            // Make an intent to start next activity.
//            Intent i = new Intent(this, MyCommunicationsActivity.class);
            //Change the activity.
//            i.putExtra(EXTRA_ADDRESS, address); //this will be received at CommunicationsActivity
//            startActivity(i);
        }
    };
}