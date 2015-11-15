package com.example.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class AcceleratorClass extends View{
	public float currentX = 40;
	public float currentY = 50;
	public float length = 10;
    public float speed = 20;
    public float len = 0;
    public float stopPointx = 0;
    public float f = 0;

	public AcceleratorClass(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		Paint point = new Paint();
		point.setColor(Color.RED);
		if(f == 0){
		     canvas.drawCircle(currentX, currentY, 15, point);
	    }else{
	    	len = length*speed;
			stopPointx = currentX + len;
			canvas.drawLine(currentX, currentY, stopPointx, currentY, point);
			
	    }
	}
	
}
