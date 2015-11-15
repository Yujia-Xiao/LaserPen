package com.samantha.bluesamserver;


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
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
//import android.bluetooth.;
import android.widget.Toast;

public class MainActivity extends Activity {
	String TAG = "Server";
	private static final int REQUEST_ENABLE_BT = 227;
	private TextView searchingBluetoothStatus;
	private TextView searchBluetoothAddress; 
	private TextView myBluetoothaddress;
	private TextView receivedMsg;
	
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
	
	private BluetoothDevice FinalDevice=null;
	private BluetoothSocket FianlSocket=null;
    private HandlerThread handlerThread = null;
    private UIHandler transHandler = null;
    private String TestUI = null;
    private Handler mainUIhandler = null;
	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_main);
		
		searchingBluetoothStatus = (TextView)findViewById(R.id.ShowingSearchingStatus);
		searchBluetoothAddress = (TextView)findViewById(R.id.SearchedBluetoothDevice);
		myBluetoothaddress = (TextView)findViewById(R.id.myDeviceAddress);
		receivedMsg = (TextView)findViewById(R.id.recievedMeg);
		
		exposeMeButton = (Button)findViewById(R.id.exposeMeButtonId);
		exposeMeButton.setOnClickListener(new ExposeMeButtonListener());
		
		scanAroundButton = (Button)findViewById(R.id.scanAroundButtonId);
		scanAroundButton.setOnClickListener(new ScanAroundButtonListener());
	    
		handlerThread = new HandlerThread("handler_thread");
		handlerThread.start();
		transHandler = new UIHandler(handlerThread.getLooper());
		
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
		
		//get the UUID for connection 
		MY_UUID=UUID.fromString("d22f30b8-2716-41d2-84f2-4cd56bb75ecc");//this is used to make sure the connection.
        
		mainUIhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String change = (String)msg.obj;
                receivedMsg.setText(change);

            }
        };
		
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
			Log.d(TAG, "before ScanBluetooth device");
			mBluetoothAdapter.startDiscovery();//send broadcast after the scan
			Log.d(TAG, "after ScanBluetooth device");
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
				String address = "F8:F1:B6:F8:F9:7A";
				address = "5C:2E:59:BA:5F:D6";
				//address = "E0:75:7D:19:CE:A6";
				if(device.getAddress().equals(address)){
					Log.d(TAG, "Get the Bluetooth Device");
					Toast.makeText(MainActivity.this, "detected:"+device.getAddress(), Toast.LENGTH_LONG).show();
				    Thread acceptThread = new AcceptThread();
				    acceptThread.start();
				}
			}	
		}	
	}
	
	
	private class AcceptThread extends Thread {
	    private final BluetoothServerSocket mmServerSocket;
	 
	    public AcceptThread() {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	        	Log.d(TAG, "AcceptThread initial - before");
	            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
	        } catch (IOException e) { }
	        mmServerSocket = tmp;
	        Log.d(TAG, "AcceptThread initial - end");
	    }
	 
	    public void run() {
	    	Looper.prepare();
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        while (true) {
	            try {
	            	Log.d(TAG, "AcceptThread run - before Socket accept");
	                socket = mmServerSocket.accept();
	                Log.d(TAG, "AcceptThread run - after Socket accept");
	            } catch (IOException e) {
	            	Log.d(TAG, "AcceptThread run - Socket accept error");
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	                // Do work to manage the connection (in a separate thread)
	                //manageConnectedSocket(socket);
	            	Log.d(TAG, "AcceptThread run - before Socket accept");
	    	        Handler connectedHandler = new ConnectedHandler();
	    			Message msgg = connectedHandler.obtainMessage();
	    	        msgg.obj=socket;
	    	        Log.d(TAG,"Before send message");
	    	        
	    	        connectedHandler.sendMessage(msgg);
	                try {
	                	Log.d(TAG,"Before serverSocket close");
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                break;
	            }
	        }
	        Looper.loop();
	    }
	 
	    /** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() {
	        try {
	            mmServerSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	
   class AcceptHandler extends Handler{
	
	   public void handleMessage(Message msg){
		   Thread accept = new AcceptThread();
		   Log.d(TAG,"thread accept");
		   accept.start();
	   }	
  }	
		
	class ConnectedHandler extends Handler {
		@Override
		public void handleMessage(Message msgg) {	
			// TODO Auto-generated method stub
			Log.d(TAG,"FinalSocket");
			FianlSocket=(BluetoothSocket)msgg.obj;
			Thread connectedThread= new ConnectedThread(FianlSocket);
			Log.d(TAG,"connectThreadStart");
			connectedThread.start();
		}
	}
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	    Message decodedmsg = transHandler.obtainMessage();
	 
	public ConnectedThread(BluetoothSocket socket) {
	    	System.out.println("current--connectedThread-construct>"+Thread.currentThread().getName());
	    	mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	       
	        
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	        	
	        	System.out.println("current--connectedThread-construct-try>"+Thread.currentThread().getName());
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	            System.out.println("current--connectedThread-construct-afterTry>"+Thread.currentThread().getName());
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	        System.out.println("current--connectedThread-construct-end>"+Thread.currentThread().getName());
	    }
	 
	    public void run() {
	    	super.run();
	    	Looper.prepare();
	    	
	    	System.out.println("current--COnnectedThread->"+Thread.currentThread().getName());
	    	byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	            	System.out.println("current--connectedThread-construct-run-try>"+Thread.currentThread().getName());
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	               //er uiHandler = new UIHandler(Looper.myLooper()); 
	               //uiHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	               //         .sendToTarget();
	               Log.d(TAG, "1111111111111");
	               //Message uimsg = uiHandler.obtainMessage();
	               Log.d(TAG, "22222222222");
	               String decoded = new String(buffer, "UTF-8");
	               decoded=decoded.substring(0, bytes);	
	               Toast.makeText(MainActivity.this, "decoded: "+decoded, Toast.LENGTH_LONG).show();
	               
	               //decodedmsg.obj=decoded;
	               //transHandler.sendMessage(decodedmsg);
	               Message message = Message.obtain();
                   message.obj = decoded;
                   mainUIhandler.sendMessage(message);
	              
	               
	               Log.d(TAG, "33333"+decoded);
	    	       //uimsg.obj=decoded;
	    	       Log.d(TAG, "44444");
	    	       //uiHandler.sendMessage(uimsg);
	    	       Log.d(TAG, "55555");
	    	       //buffer=null;
	    	       
	               System.out.println("current--connectedThread-construct-run-after-try>"+Thread.currentThread().getName());
	            } catch (IOException e) {
	                break;
	            }
	        }
	        Looper.loop();
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
	
	public class UIHandler extends Handler {
		
		public UIHandler(){}
		public UIHandler(Looper looper){
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msgg) {	
			// TODO Auto-generated method stub
			super.handleMessage(msgg);
			
			Log.d(TAG, "UIHandler-before");
			final String RecievedMeg = (String)msgg.obj;
			Log.d(TAG, "UIHandler-before2222");
			
			/*MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	receivedMsg.setText(RecievedMeg);
                }
            });*/
			
			//receivedMsg.setText("Received Msg is: "+RecievedMeg);
			TestUI=RecievedMeg;
			
			Log.d(TAG, "UIHandler-before333"+RecievedMeg);
		}
	}

	
	
	
	
	
	
	
	
	private class ButtonListner implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//find the BLurtooth device
			System.out.println("current--ButtonListener->"+Thread.currentThread().getName());
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			BluetoothDevice OutDevice = null;
			if (adapter != null){
				//System.out.println("This machine has Bluetooth");
				//textview.setText("this machine has Bluetooth Device");
				if(!adapter.isEnabled()){
					//textview.setText("cy");
					Intent intent= new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivity(intent);	
				}
				
				Set<BluetoothDevice> devices =adapter.getBondedDevices();
				
				if(devices.size()>0){
					for (Iterator iterator = devices.iterator(); iterator.hasNext();){
						BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
						//textview.setText(bluetoothDevice.getAddress());
						OutDevice = bluetoothDevice;
					}
				}
				
				Message msg = myHandler.obtainMessage();
				msg.obj=OutDevice;
				myHandler.sendMessage(msg);
	
			}
			
			else {//textview.setText("this machine has no Bluetooth");}
			
		}
			
	  }
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

