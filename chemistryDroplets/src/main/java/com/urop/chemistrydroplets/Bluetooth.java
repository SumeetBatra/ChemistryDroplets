package com.urop.chemistrydroplets;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by sumeetbatra on 8/25/17.
 */

public class Bluetooth extends Application {

    public OutputStream outputStream;

    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            outputStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
        }
    }


}
