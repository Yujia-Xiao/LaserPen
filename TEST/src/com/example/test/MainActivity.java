package com.example.test;


import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	private float x ;
	private float y ;
	private float height, width;
	private boolean fx = false;
	private boolean fy = false;
	private AcceleratorClass draw;
	private LinearLayout linear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        DisplayMetrics dm = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(dm);
    	//float density = dm.density;
    	height = dm.heightPixels-(int) 200.0;
    	
    	String h = height+"";
    	Log.v("height=", h);
    	
    	width = dm.widthPixels;
    	String w = width+"";
    	Log.v("width=", w);
    	
    	y = height/2;
    	x = width/2;
    	
    	linear = (LinearLayout)findViewById(R.id.linear);
    	draw = new AcceleratorClass(this);
    	
    	new Thread(new TimeThread()).start();
    	
    	linear.addView(draw);		
    	}
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
    	if(fx == false){
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
    	}
    	String xx = x+"";
    	String yy = y+"";
    	Log.v("x=",xx);
    	Log.v("y=",yy);
    	
    }
    }

