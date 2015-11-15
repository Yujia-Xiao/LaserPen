package com.example.test;


import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;

//import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
//import android.hardware.SensorEventListener2;
//import android.hardware.SensorListener;
import android.hardware.SensorManager;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {

	private float ax;
	private float ay;
	private float a0;//
	private float ssta;//sensitivity of accl
	private float vx;
	private float vy;
	private float sstv;//sensitivity of velocity
	private float x ;
	private float y ;
	private float height, width;
	//private boolean fx = false;
	//private boolean fy = false;
	private AcceleratorClass draw;
	private LinearLayout linear;
	
	private SensorManager sensorMgr;//acc
	private TextView msg;//acc
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);//acc
		msg=(TextView)findViewById(R.id.msg);//acc
		msg.setText("initialization");//acc
        
        DisplayMetrics dm = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(dm);
    	//float density = dm.density;
    	height = dm.heightPixels-(int) 200.0;
    	
    	String h = height+"";
    	Log.v("height=", h);
    	
    	width = dm.widthPixels;
    	String w = width+"";
    	Log.v("width=", w);
    	
    	//initial
    	y = height/2;
    	x = width/2;
    	a0 = 1;  	
    	ax = 0;
    	ay = 0;
    	ssta = 1;
    	vx = 0;
    	vy = 0;
    	sstv = 1;
    	
    	linear = (LinearLayout)findViewById(R.id.linear);
    	draw = new AcceleratorClass(this);
    	
    	new Thread(new TimeThread()).start();
    	
    	linear.addView(draw);		
    	}
    
    /////////////////acc
	SensorEventListener listener = new SensorEventListener(){
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			Sensor sensor = event.sensor;
			StringBuilder sensorInfo = new StringBuilder();
			sensorInfo.append("sensor name: " + sensor.getName() + "\n");
			sensorInfo.append("sensor type: " + sensor.getType() + "\n");
			sensorInfo.append("used power: " + sensor.getPower() + "mA\n");
			sensorInfo.append("value: \n");
			float[] values = event.values;
			ax = -values[0];//acc
			ay = values[1];//acc
			for (int i = 0; i< values.length; i++)
				sensorInfo.append("-values[" + i + "] = " + values[i] + "\n");
			msg.setText(sensorInfo);	
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub	
		}
	};
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
	
	

    /////////////////acc
    class TimeThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!Thread.currentThread().isInterrupted()){
				try{
					Thread.sleep(100);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				updatePosition();
				draw.currentX = x;
		    	draw.currentY = y;
				draw.postInvalidate();
				
			}
		}
    	
    }
    
    private void updatePosition(){
    	
    	vx = vx + ax * ssta;
    	vy = vy + ay * ssta;
    	x = x + vx * sstv;
    	y = y + vy * sstv;
    	if( x >= width){
    		x = 1;
    		vx = 0;
    	}
    	if(x <= 0){
    		x = width - 1;
    		vx = 0;
    	}
    	if( y >= height){
    		y = 1;
    		vy = 0;
    	}
    	if( y <= 0){
    		y = height -1;
    		vy = 0;
    	}
    	
   /* 	if(fx == false){
    		x = x+10;
    		if(x >= width){
    			fx = true;
    		}
    	}else{
    		x = x-10;
    		if(x <= 0){
    			fx = false;
    		}
    	}
    	if(fy == false){
    		y = y+10;
    		if(y >= height){
    			fy = true;
    		}
    	}else{
    		y = y-10;
    		if(y <= 0){
    			fy = false;
    		}
    	}*/
    	String xx = x+"";
    	String yy = y+"";
    	Log.v("x=",xx);
    	Log.v("y=",yy);
    	
    }
    }

