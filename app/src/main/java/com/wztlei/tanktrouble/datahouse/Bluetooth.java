package com.wztlei.tanktrouble.datahouse;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.Set;

public class Bluetooth {
    private static Bluetooth instance = null;

    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    BluetoothSocket peerSocket;

    private Bluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
    }

    public static Bluetooth getInstance() {
        if(instance == null)
            instance = new Bluetooth();

        return instance;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void setPeerSocket(BluetoothSocket peerSocket) {
        this.peerSocket = peerSocket;
    }

    // TODO: FARAZ: Must fix the function selection of device
    public BluetoothDevice choosePairedDevice() {
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                return device;
            }
        }
        return null;
    }

    public void discoverDevices() {

    }
}
