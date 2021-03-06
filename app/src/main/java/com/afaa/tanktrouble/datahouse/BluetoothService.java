package com.afaa.tanktrouble.datahouse;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.afaa.tanktrouble.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;


public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("27e86a38-a29c-421e-9d17-fe9c0c3bf2e6");

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException ignored) {
            }
            btSocket = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class BtBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private final IBinder binder = new BtBinder();

    public static class BtUnavailableException extends Exception {
        public BtUnavailableException() {
            super("bluetooth is not supported");
        }
    }

    public void initBtAdapter() throws BtUnavailableException {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            throw new BtUnavailableException();
        }
    }

    public boolean isConnected() {
        return connectedThread != null;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return btAdapter;
    }

    public interface OnConnected {
        void success();
    }

    private OnConnected onConnected;

    public void setOnConnected(OnConnected onConnected) {
        this.onConnected = onConnected;
    }

    public void startAcceptThread() {
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void startConnectThread(String address) {
        connectThread = new ConnectThread(address);
        connectThread.start();
    }

    public BluetoothSocket getBluetoothSocket() {
        return btSocket;
    }


    public void registerActivity(Class<?> activityClass) {
        lastClass = activityClass;
        changeRegCount(1);
    }


    public void unregisterActivity() {
        changeRegCount(-1);
    }

    private int regCount = 0;
    private Class<?> lastClass;

    private synchronized void changeRegCount(int d) {
        int newRegCount = regCount + d;

        if (newRegCount != 0 && regCount == 0) {
            hideNotification();
        }

        regCount = newRegCount;
    }

    public boolean isServer() {
        return isServer;
    }

    public interface OnMessageReceivedListener {
        void process(byte[] buffer);
    }

    public class MessageChannel {
        private OnMessageReceivedListener onMessageReceivedListener;

        public void setOnMessageReceivedListener(OnMessageReceivedListener onMessageReceivedListener) {
            this.onMessageReceivedListener = onMessageReceivedListener;
        }

        public void send(byte[] bytes) {

            if(connectedThread == null)
                return;
            synchronized (connectedThread) {
                byte[] buffer = new byte[bytes.length + 1];
                System.arraycopy(bytes, 0, buffer, 1, bytes.length);
                connectedThread.write(buffer);
            }
        }
    }

    MessageChannel channel = new MessageChannel();

    public MessageChannel getChannel() {
        if(channel == null) {
            channel = new MessageChannel();
        }
        return channel;
    }

    public void unregisterChannel() {
        channel = null;
    }

    private void process(byte[] message) {
        if(channel == null || channel.onMessageReceivedListener == null) {
            return;
        }
        channel.onMessageReceivedListener.process(message);
    }

    private void hideNotification() {
        stopForeground(true);
    }

    private boolean isServer;

    private synchronized void connected(BluetoothSocket socket, boolean isServer) {
        this.btSocket = socket;

        this.isServer = isServer;

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        onConnected.success();

        connectedThread = new ConnectedThread();
        connectedThread.start();
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            try {
                serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(TAG, MY_UUID);
            } catch (IOException ignored) {
            }
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }
                if (socket != null) {
                    connected(socket, true);
                    try {
                        serverSocket.close();
                    } catch (IOException ignored) {
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;
        public static final String TAG = "ConnectThread";

        public ConnectThread(String address) {
            try {
                socket = btAdapter.getRemoteDevice(address).createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException ignored) {
            }
        }

        public void run() {
            btAdapter.cancelDiscovery();

            try {
                socket.connect();
            } catch (IOException connectException) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                return;
            }

            BluetoothSocket tmp = socket;
            socket = null;
            connected(tmp, false);
        }

        public void cancel() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread() {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = btSocket.getInputStream();
                tmpOut = btSocket.getOutputStream();
            } catch (IOException ignored) {
                Log.d(TAG, "error");
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];

            while (true) {
                try {
                    int countReadBytes = inputStream.read(buffer);
                    process(Arrays.copyOfRange(buffer, 0, countReadBytes));
                    buffer = new byte[1024];
                } catch (IOException e) {
                    break;
                }
            }
        }

        private void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException ignored) {
            }
        }

        public void cancel() {
            try {
                btSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
