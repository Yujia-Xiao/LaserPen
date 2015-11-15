package edu.osu.laserpen;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.app.AlertDialog;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;

import android.view.MotionEvent;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 227;
	boolean flag=false;
	int colorNum=0;
	float dx=0;
	float vx=0;
	private SensorManager sensorMgr;
	private TextView msg;
	private TextView myBluetoothaddress;
	
	private Button exposeMeButton = null;
	private Button scanAroundButton = null;
	private Button Send2Button = null;
    
	//this will be used in several functions
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothReceiver bluetoothReceiver = null;
	private GestureDetector gestureDetector;
    private String[] colors = new String[]{"Red","Blue","Yellow","Green","Orange"};
	private boolean[] areaState = new boolean[]{true,false,false,false,false};
	ImageButton mybtn;
	ImageButton btnClear;
	ImageButton btnColor;

	private String NAME="BlueSamServer";
	private UUID MY_UUID;
	private Handler myHandler;
	private Handler myHandler2;
	private int MESSAGE_READ;
	
	private BluetoothDevice RemoteDevice=null;
	private BluetoothSocket FianlSocket=null;
	private ConnectedThread mConnedtedThread = null;
	private String UIString = null;
    private static int FLG = 0;
    private double ACINT = 0;
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE); 
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_main);
        sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
		//searchingBluetoothStatus = (TextView)findViewById(R.id.ShowingSearchingStatus);
		//searchBluetoothAddress = (TextView)findViewById(R.id.SearchedBluetoothDevice);
		myBluetoothaddress = (TextView)findViewById(R.id.myDeviceAddress);
		msg=(TextView)findViewById(R.id.msg);
		msg.setText("initialization");
		//mEditText = (EditText)findViewById(R.id.typeTextID); 
        final TextView comment1 = (TextView) this.findViewById(R.id.textView2);
		 mybtn = (ImageButton) this.findViewById(R.id.imageButton1);
		exposeMeButton = (Button)findViewById(R.id.exposeMeButtonId);
		exposeMeButton.setOnClickListener(new ExposeMeButtonListener());
	    btnClear = (ImageButton) this.findViewById(R.id.imageButton2);
		scanAroundButton = (Button)findViewById(R.id.scanAroundButtonId);
		scanAroundButton.setOnClickListener(new ScanAroundButtonListener());
		
      	btnColor = (ImageButton) this.findViewById(R.id.imageButton3);
		
		mybtn.setOnClickListener(new OnClickListener()
	        {   
			    	    public void onClick(View v){
    	    	
    	    	if(v == mybtn){
    				if(flag==false) {
    					comment1.setText("swipe anywhere right/left to highlight");
    					//send("start");
    					BluetoothSend("start");
    					Toast.makeText(getApplicationContext(), "LASER PEN ON", Toast.LENGTH_SHORT).show();
    					flag=true;
    				}
    				else{
    					comment1.setText("");
    					BluetoothSend("stopp");
    					Toast.makeText(getApplicationContext(), "LASER PEN OFF", Toast.LENGTH_SHORT).show();
    					flag=false;
    				}
    			}
    	    }
        });   
        
        btnClear.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				BluetoothSend("clear");
				Toast.makeText(getApplicationContext(), "CLEAR CLICKED", Toast.LENGTH_SHORT).show();							}        });
        
        btnColor.setOnClickListener(new AlertClickListener());
        
        gestureDetector = new GestureDetector(MainActivity.this,onGestureListener);

		
		
		
		
		
		
		/*Send2Button = (Button)findViewById(R.id.sendBtnId);
		Send2Button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String Mainmsg = "second";
				
				BluetoothSend(Mainmsg);
			}
			
		});
*/		//responsible for local BlueTooth device activity
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter==null){
			//searchingBluetoothStatus.setText("Bluetooth is not supported in this phone.");
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
	   
		
		String str = "second";
		//BluetoothSend(str);
		
		
	}
	
	SensorEventListener listener = new SensorEventListener(){

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			ACINT=ACINT+0.05;
			Sensor sensor = event.sensor;
			StringBuilder sensorInfo = new StringBuilder();
			sensorInfo.append("sensor name: " + sensor.getName() + "\n");
			sensorInfo.append("sensor type: " + sensor.getType() + "\n");
			sensorInfo.append("used power: " + sensor.getPower() + "mA\n");
			sensorInfo.append("value: \n");
			float[] values = event.values;
			for (int i = 0; i< values.length; i++)
				sensorInfo.append("-values[" + i + "] = " + values[i] + "\n");
			msg.setText(sensorInfo);
			if(ACINT ==1.00){
				BluetoothSend(Encode(event));
				ACINT=0.00;
			}
		
			
		}
		
		public String Encode(SensorEvent event){
			float[] values = event.values;
			String BtInfo = "MoveP" + Float.toString(values[0]).substring(0,5) + Float.toString(values[1]).substring(0,5);		
			return BtInfo;
		}
		public float[] Decode(String BtInfo){
			float[] accl = new float[2];
			accl[0] = Float.parseFloat(BtInfo.substring(5,10));
			accl[1] = Float.parseFloat(BtInfo.substring(10,15));
			return accl;
		}
   
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
	};
	
	
	
	public synchronized void BluetoothSend(String str){
		if(mConnedtedThread!=null){
			Log.d(NAME, "button");
			 mConnedtedThread.write(str.getBytes());
			 Log.d(NAME, "button2");
		}
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
				//searchingBluetoothStatus.setText("Searching around Bluetooth...");
				//searchBluetoothAddress.setText(device.getAddress());
				Log.d("devices", device.getAddress());
				//String address = "68:76:4F:20:93:4A";
				String address = "E0:75:7D:19:CE:A6";
				address = "F8:F1:B6:F8:F9:7A";
				address = "";
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
			mConnedtedThread = (ConnectedThread)connectedThread;
			String s = "hello";
			byte[] bytes = new byte[1024];
			bytes=s.getBytes();
			
			SendFromUIHandler msendfromUIHandler = new SendFromUIHandler();
			Message msg = msendfromUIHandler.obtainMessage();
			msg.obj=connectedThread;
			msendfromUIHandler.sendMessage(msg);
			connectedThread.start();
			((ConnectedThread) connectedThread).write(bytes);
			/*while(true){
				if(FLG==true){
					bytes=UIString.getBytes();
					((ConnectedThread) connectedThread).write(bytes);
					FLG=false;
				}
			}*/
			//connectedThread.start();
			
		}
	}
	
	class SendFromUIHandler extends Handler{
		
		@Override
		public void handleMessage(Message msg){
			Thread t = (Thread)msg.obj;
			t=(ConnectedThread)mConnedtedThread;
		}
	}
	
	
	 // judge the distance of a swipe
    private GestureDetector.OnGestureListener onGestureListener =   
            new GestureDetector.SimpleOnGestureListener() {  
            @Override  
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
                    float velocityY) {  
                dx = e2.getX() - e1.getX();  
                vx = velocityX;
                if (flag==true){
                	BluetoothSend("pline"+String.valueOf(dx).substring(0,5)+String.valueOf(vx).substring(0,5));
	                if (dx > 0) {  
	                	Toast.makeText(getApplicationContext(), "GO RIGHT "+String.valueOf(dx)+"; velocity:"+velocityX, Toast.LENGTH_SHORT).show();
	                } else if (dx < 0) {  
	                	Toast.makeText(getApplicationContext(), "GO LEFT "+String.valueOf(dx)+"; velocity:"+velocityX, Toast.LENGTH_SHORT).show();
	                }  
                }
                return true;  
            }  
        };  
        
     @Override
    public boolean onTouchEvent(MotionEvent event){
    	return gestureDetector.onTouchEvent(event);
    }
    
     //judge the color chosen
    class AlertClickListener implements OnClickListener{
    	@Override
    	public void onClick(View v){
    		new AlertDialog.Builder(MainActivity.this).setTitle("Choose Color").setItems(colors, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					BluetoothSend("Color"+colors[which]);
					Toast.makeText(MainActivity.this, "Have chosen:"+which+":"+colors[which], Toast.LENGTH_LONG).show();
					dialog.dismiss();
				}
			}).show();
    	}
    }
   
	
	@Override
	protected void onResume(){
		super.onResume();
		sensorMgr.registerListener(listener, 
				sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
	}
	@Override
	protected void onPause(){
		super.onPause();
		sensorMgr.unregisterListener(listener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

