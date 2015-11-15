package com.sam.bluesamclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//import android.bluetooth.;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 227;
	private TextView searchingBluetoothStatus;
	private TextView searchBluetoothAddress; 
	private TextView myBluetoothaddress;

	private Button exposeMeButton = null;
	private Button scanAroundButton = null;
    
	//this will be used in several functions
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothReceiver bluetoothReceiver = null;

	private String NAME="BlueSamServer";
	private UUID MY_UUID;
	private Handler myHandler;
	private Handler myHandler2;
	private int MESSAGE_READ;
	
	private BluetoothDevice RemoteDevice=null;
	private BluetoothSocket FianlSocket=null;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_main);
		
		searchingBluetoothStatus = (TextView)findViewById(R.id.ShowingSearchingStatus);
		searchBluetoothAddress = (TextView)findViewById(R.id.SearchedBluetoothDevice);
		myBluetoothaddress = (TextView)findViewById(R.id.myDeviceAddress);
		
		exposeMeButton = (Button)findViewById(R.id.exposeMeButtonId);
		exposeMeButton.setOnClickListener(new ExposeMeButtonListener());
		
		scanAroundButton = (Button)findViewById(R.id.scanAroundButtonId);
		scanAroundButton.setOnClickListener(new ScanAroundButtonListener());
	
		
		//responsible for local BlueTooth device activity
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter==null){
			searchingBluetoothStatus.setText("Bluetooth is not supported in this phone.");
		}

		//enable BlueTooth
		if(!mBluetoothAdapter.isEnabled()){
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		myBluetoothaddress.setText(mBluetoothAdapter.getAddress());
		
		//register the broadcastReceiver
		IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		bluetoothReceiver = new BluetoothReceiver();
		registerReceiver(bluetoothReceiver,intentFilter);
		
		MY_UUID=UUID.fromString("d22f30b8-2716-41d2-84f2-4cd56bb75ecc");//this is used to make sure the connection.
	}
	
	private class ExposeMeButtonListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			System.out.println("current--DiscoverButtonListener->"+Thread.currentThread().getName());
			// TODO Auto-generated method stub
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}	
	}

	private class ScanAroundButtonListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			System.out.println("current--ScanButtonListener->"+Thread.currentThread().getName());
			// TODO Auto-generated method stub
			mBluetoothAdapter.startDiscovery();//send broadcast after the scan
		}
	}
	//the ScanAroundButtonListener result will be feedback in Receiver
	private class BluetoothReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			//System.out.println("current--BluetoothReciver->"+Thread.currentThread().getName());
			String action = intent.getAction();
			//textview2.setText("inside receiver");
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				searchingBluetoothStatus.setText("Searching around Bluetooth...");
				searchBluetoothAddress.setText(device.getAddress());
				Log.d("devices", device.getAddress());
				//String address = "68:76:4F:20:93:4A";
				String address = "E0:75:7D:19:CE:A6";
				address = "F8:F1:B6:F8:F9:7A";
				if(device.getAddress().equals(address)){
					Toast.makeText(MainActivity.this, "detected:"+device.getAddress(), Toast.LENGTH_LONG).show();
					Handler connectHandler = new ConnectHandler();
					Message msgg = connectHandler.obtainMessage();
			        msgg.obj=device;
			        connectHandler.sendMessage(msgg);
				}
			}	
		}	
	}
	
	private class ConnectHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {	
			RemoteDevice=(BluetoothDevice)msg.obj;
			Thread connectThread= new ConnectThread(RemoteDevice);
			connectThread.start();
		}
	}
	
	
		
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	    //public Handler InsideHandler;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	    	Looper.prepare();
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        //ConnectedSocket(mmSocket);
	        Handler connectedHandler = new ConnectedHandler();
			Message msgg = connectedHandler.obtainMessage();
	        msgg.obj=mmSocket;
	        connectedHandler.sendMessage(msgg);
	        
	        Looper.loop();
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	public class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	               /* Handler uiHandler = new UIHandler();
	                uiHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                        .sendToTarget();*/
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	    
	}
	
	
	class ConnectedHandler extends Handler {
		@Override
		public void handleMessage(Message msgg) {
			FianlSocket=(BluetoothSocket)msgg.obj;
			Thread connectedThread= new ConnectedThread(FianlSocket);
			String s = "hello";
			byte[] bytes = new byte[1024];
			bytes=s.getBytes();
			((ConnectedThread) connectedThread).write(bytes);
			connectedThread.start();
			
		}
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

