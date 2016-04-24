package com.ash.xhfw3;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import java.util.logging.*;
 
public class Movable implements View.OnTouchListener {
        final Window mWindow;
        final LayoutParams param;
        //final AeroSnap mAeroSnap;
        final boolean mReturn;
        
        private static Float screenX ;
    	private static Float screenY ;
    	private static Float viewX ;
    	private static Float viewY ;
    	private static Float leftFromScreen ;
    	private static Float topFromScreen ;
    	private View offsetView;
       
        public Movable(Window window, boolean return_value) {
                mWindow=window;
        		param = mWindow.getAttributes(); 
        		//mAeroSnap = aerosnap;
        		mReturn = return_value;
        }
        
        public Movable(Window window, View v){
        	this(window, false);
        	offsetView = v;
        }
        
        @Override
        public boolean onTouch(View v, MotionEvent event) {
        	switch (event.getAction()){
        	case MotionEvent.ACTION_DOWN:
        		viewX = event.getX();
    			viewY = event.getY();
        		if (offsetView != null) {
        			int[] location = {0,0};
        			offsetView.getLocationInWindow(location);
        			viewX = viewX + location[0];
        			viewY = viewY + location[1];
        		}
                break;
        	case MotionEvent.ACTION_MOVE:
        		screenX = event.getRawX();
        		screenY = event.getRawY();
        		leftFromScreen = (screenX - viewX);
        		topFromScreen = (screenY - viewY);
        		//mWindow.setGravity(Gravity.LEFT | Gravity.TOP);
				MovableWindow.move(leftFromScreen.intValue(),topFromScreen.intValue());
        		//updateView(mWindow, leftFromScreen, topFromScreen);
				//if (MovableWindow.mAeroSnap != null) {
				if (MovableWindow.mWindowHolder.isSnapped) {
					//MovableWindow.mAeroSnap.restoreOldPosition();
				}
        		break;
        	}
        	/*if (mAeroSnap != null) {
        		mAeroSnap.dispatchTouchEvent(event);
        	}*/
        	return mReturn;
        }
        /*private void updateView(Window mWindow, float x , float y){
    		param.x = (int)x;	
    		param.y = (int)y;
    		mWindow.setAttributes(param);
			MovableWindow.mWindowHolder.setWindow(mWindow);
    		MovableWindow.pullAndSyncLayoutParams();
    	}*/
}
