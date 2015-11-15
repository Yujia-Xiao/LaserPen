package edu.osu.laserpen;
import android.R.string;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;

import android.view.MotionEvent;

public class MainActivity extends Activity {
		
	boolean flag=false;
	int colorNum=0;
	float dx=0;
	float vx=0;
	
    private SensorManager sensorMgr;
	private TextView msg;
	
	private GestureDetector gestureDetector;
    private String[] colors = new String[]{"Red","Blue","Yellow","Green","Orange"};
    private boolean[] areaState = new boolean[]{true,false,false,false,false};
    ImageButton mybtn;
	ImageButton btnClear;
    ImageButton btnColor;
	
    @Override    
    protected void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE); 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
		msg=(TextView)findViewById(R.id.msg);
		msg.setText("initialization");

		
        final TextView comment1 = (TextView) this.findViewById(R.id.textView2);
        mybtn = (ImageButton) this.findViewById(R.id.imageButton1);
        btnClear = (ImageButton) this.findViewById(R.id.imageButton2);
        btnColor = (ImageButton) this.findViewById(R.id.imageButton3);
       
        //judge if the button is clicked		
        mybtn.setOnClickListener(new OnClickListener()
        {   
    	    public void onClick(View v){
    	    	
    	    	if(v == mybtn){
    				if(flag==false) {
    					comment1.setText("swipe anywhere right/left to highlight");
    					//send("start");
    					Toast.makeText(getApplicationContext(), "LASER PEN ON", Toast.LENGTH_SHORT).show();
    					flag=true;
    				}
    				else{
    					comment1.setText("");
    					//send("stopp");
    					Toast.makeText(getApplicationContext(), "LASER PEN OFF", Toast.LENGTH_SHORT).show();
    					flag=false;
    				}
    			}
    	    }
        });   
        
        btnClear.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//send("clear");
				Toast.makeText(getApplicationContext(), "CLEAR CLICKED", Toast.LENGTH_SHORT).show();							}        });
        
        btnColor.setOnClickListener(new AlertClickListener());
        
        gestureDetector = new GestureDetector(MainActivity.this,onGestureListener);
    }
    
    
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
			for (int i = 0; i< values.length; i++)
				sensorInfo.append("-values[" + i + "] = " + values[i] + "\n");
			msg.setText(sensorInfo);
			
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
        
    // judge the distance of a swipe
    private GestureDetector.OnGestureListener onGestureListener =   
            new GestureDetector.SimpleOnGestureListener() {  
            @Override  
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
                    float velocityY) {  
                dx = e2.getX() - e1.getX();  
                vx = velocityX;
                if (flag==true){
                	//send("pline"+String.valueOf(dx).substring(0,5)+String.valueOf(vx).substring(0,5));
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
					//send("Color"+colors[which]);
					Toast.makeText(MainActivity.this, "Have chosen:"+which+":"+colors[which], Toast.LENGTH_LONG).show();
					dialog.dismiss();
				}
			}).show();
    	}
    }
    
    public String DecodeColor(String s){
    	return s.substring(5);
    }
    
    public float[] DecodeLine(String s){
    	float[] l=new float[2];
    	l[0]=Float.parseFloat(s.substring(5,10));
    	l[1]=Float.parseFloat(s.substring(10,15));
    	return l;
    }
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
}
