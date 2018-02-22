package com.urop.chemistrydroplets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

//bluetooth portions of this activity adapted from BlueControl

public class SelectionActivity extends Activity {

//					ProgressDialog progressDialog = new ProgressDialog(SelectionActivity.this);
//					progressDialog.setTitle("BluetoothConnect");
//					progressDialog.setMessage("Connecting to Droplet...");
//					progressDialog.setCancelable(false);
//					progressDialog.setIndeterminate(true);
//					progressDialog.show();
	
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_ENABLE_BT = 3;
	
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private static String address = "00:00:00:00:00:00";

	private String connected = "false";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selection);
		
		Button start = (Button)findViewById(R.id.start);
		start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SelectionActivity.this, MainActivity.class);
				intent.putExtra("isConnected", connected);
				startActivity(intent);
			}
		});
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		Button bluetooth = (Button)findViewById(R.id.bluetooth);
		bluetooth.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				checkBTState();
				Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
				Intent serverIntent = new Intent(SelectionActivity.this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

			}
			
		});
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.selection, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public String connectDevice(Intent data, boolean secure) {
		address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		BluetoothDevice device = btAdapter.getRemoteDevice(address);
		Log.d("ADDRESS", address);
		try{
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			return "Error: socket create failed: " + e.getMessage() + ".";
		}
		
		btAdapter.cancelDiscovery();
		
		try{
			btSocket.connect();
		}catch(IOException e) {
			try{
				btSocket.close();
			}catch(IOException e2){
				return "Error: unable to close socket during connection failure" + e2.getMessage() + ".";
			}
		}
		
		try{
			outStream = btSocket.getOutputStream();
		}catch(IOException e) {
			return "Error: output stream creation failed: " + e.getMessage() + ".";
		}
		BluetoothDevice Device = btAdapter.getRemoteDevice(address);
		Log.d("SUCCESS", "Connected to randombot");

		connected = "true";
		((Bluetooth)this.getApplicationContext()).outputStream = outStream;
		return "Connected";
	}
	
	private void checkBTState() {
		if(btAdapter==null) {
			Toast msg = Toast.makeText(getBaseContext(), "Error: Bluetooth not supported. Aborting", Toast.LENGTH_SHORT);
			msg.show();
			finish();
		}else{
			if(btAdapter.isEnabled()) {
				Log.d("BT","Bluetooth is enabled.");
			}else{
				Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode){
			case REQUEST_CONNECT_DEVICE_SECURE:
				if (resultCode == Activity.RESULT_OK) {
					String result = connectDevice(data, true);
					Toast msg = Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT);
					msg.show();
				}
				break;
		}
	}
}
