package com.ash.xhfw3;

/**
 * Created by andrey on 21.04.16.
 */
import android.app.Activity;
import android.view.Gravity;
import android.view.Window;

import de.robv.android.xposed.XSharedPreferences;
import android.view.*;

import java.util.ArrayList;

public class WindowHolder{
    public boolean isSnapped = false;
    public boolean isMaximized = false;
    public boolean serviceConnected = false;
    public int SnapGravity = 0; //Gravity flag, eg TOP | LEFT for TopLeft window
    public float dim;
    public float alpha;
    public int width = -1;
    public int height = -1;
    public int x = 0;
    public int y = 0;
    public int cachedOrientation;
    public int cachedRotation;
    public Window mWindow;
    public static ArrayList<Window> mWindows = new ArrayList<Window>();
    public String packageName;
    public Activity mActivity;
    //public boolean isSet=false;
    //public boolean mReceiverRegistered = false;

    public WindowHolder(Activity sActivity, XSharedPreferences mPref){
        mActivity = sActivity;
        mPref.reload();
        alpha = mPref.getFloat(Common.KEY_ALPHA, Common.DEFAULT_ALPHA);
        dim = mPref.getFloat(Common.KEY_DIM, Common.DEFAULT_DIM);
        cachedOrientation=mActivity.getResources().getConfiguration().orientation;
       // cachedRotation = Util.getDisplayRotation(mActivity);
		/*TODO: Get use of EXTRA_SNAP extras to keep snap gravity*/
		/*if(mActivity.getIntent().hasExtra(Common.EXTRA_SNAP)) SnapGravity = mActivity.getIntent().getIntExtra(Common.EXTRA_SNAP, 0);
			else */
        SnapGravity = Compatibility.snapSideToGravity(mActivity.getIntent().getIntExtra(Common.EXTRA_SNAP_SIDE, Compatibility.AeroSnap.SNAP_NONE));
        //mActivity.getIntent().getIntExtra(Common.EXTRA_SNAP, 0);
        isSnapped=(SnapGravity != 0);
        isMaximized=(SnapGravity == Gravity.FILL);
        setWindow(mActivity);
        //updateWindow();
        packageName = mActivity.getPackageName();
    }
	
	/* constructor to clone values*/
	public WindowHolder (final WindowHolder sWindowHolder){
		alpha = sWindowHolder.alpha;
        width = sWindowHolder.width;
        height = sWindowHolder.height;
        x = sWindowHolder.x;
        y = sWindowHolder.y;
	}
	
	public void setWindow (Activity sActivity){
        mActivity = sActivity;
		//if(!mWindows.contains(mWindow)) mWindows.add(mWindow);
        setWindow(sActivity.getWindow());
	}

    public void setWindow (Window sWindow){
        mWindow = sWindow;
		if(!mWindows.contains(mWindow)) 
			mWindows.add(mWindow);
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    }

    public void updateSnap(int newSnap){
        SnapGravity = newSnap;
    }

    public boolean updateSnap(Activity sActivity){
        int newSnap = sActivity.getIntent().getIntExtra(Common.EXTRA_SNAP, 0);
        if(newSnap == 0) return false;
        if(SnapGravity == newSnap) return false;
        SnapGravity = newSnap;
        isSnapped=(SnapGravity != 0);
        isMaximized=(SnapGravity == Gravity.FILL);
        return true;
    }

    public void updateWindow(Window sWindow){
        mWindow = sWindow;
        updateWindow();
    }

    public void updateWindow(){
        alpha = mWindow.getAttributes().alpha;
        width = mWindow.getAttributes().width;
        height = mWindow.getAttributes().height;
        x = mWindow.getAttributes().x;
        y = mWindow.getAttributes().y;
        packageName = mWindow.getAttributes().packageName;
    }

    

    public void setMaximized(){
        width = ViewGroup.LayoutParams.MATCH_PARENT;
        height = ViewGroup.LayoutParams.MATCH_PARENT;
        x=0;
        y=0;
        SnapGravity=Gravity.FILL;
        isMaximized=true;
    }

    //restore/copy precached data
    public void restore(WindowHolder sWindowHolder){
        alpha = sWindowHolder.alpha;
        width = sWindowHolder.width;
        height = sWindowHolder.height;
		if(width==0) width=-1;
		if(height==0) height = -1;
        x = sWindowHolder.x;
        y = sWindowHolder.y;
        isMaximized = false;
        //isFloating = sWindowHolder.isFloating;
        isSnapped = false;
        SnapGravity = 0;
        //pushToWindow();
    }

    public void restore(SnapWindowHolder sSnapWindowHolder){
        x = sSnapWindowHolder.x;
        y = sSnapWindowHolder.y;
        width = sSnapWindowHolder.width;
        height = sSnapWindowHolder.height;
		if(width==0) width=-1;
		if(height==0) height = -1;
        SnapGravity = sSnapWindowHolder.SnapGravity;
        isSnapped = true;
    }

    //set current window to saved layout params
    public void pushToWindow(){
		/*FIX for floating dialogs that shouldn't be treated as movable or halo windows*/
        if(mWindow.isFloating()) return;
        WindowManager.LayoutParams mWParams = mWindow.getAttributes();
        mWParams.x = x;
        mWParams.y = y;
        mWParams.alpha = alpha;
        mWParams.width = width;
        mWParams.height = height;
        mWParams.dimAmount = dim;
        mWParams.gravity = Gravity.TOP | Gravity.LEFT;
        //Util.addPrivateFlagNoMoveAnimationToLayoutParam(mWParams);
        mWindow.setAttributes(mWParams);
    }

    public void pushToWindow(Window sWindow){
		/*FIX for floating dialogs that shouldn't be treated as movable or halo windows*/
        if(sWindow==null || sWindow.isFloating()) return;
        WindowManager.LayoutParams mWParams = sWindow.getAttributes();
        mWParams.x = x;
        mWParams.y = y;
        mWParams.alpha = alpha;
        mWParams.width = width;
        mWParams.height = height;
        mWParams.gravity = Gravity.TOP | Gravity.LEFT;
        sWindow.setAttributes(mWParams);
    }

    //get current window layout params
    public void pullFromWindow(){
        WindowManager.LayoutParams mWParams = mWindow.getAttributes();
        x = mWParams.x;
        y = mWParams.y;
        alpha = mWParams.alpha;
        width = mWParams.width;
        height = mWParams.height;
        //cachedOrientation = Util.getScreenOrientation(mActivity);
    }
	
	public void position(int newx, int newy){
		//Chrome layout fix
		if(packageName.startsWith("com.android.chrome")&&newx==0&&newy==0){
			if(x==0&&y==0){newx=1; newy=1;}
			else if(x==1&&y==1) {newx=0; newy=0;}
		}
		x = newx;
		y = newy;
	}
	
	public void size(int newwidth, int newheight){
		width = newwidth;
		height = newheight;
	}
	
	public void syncLayout(){
		for(Window w : mWindows){
			pushToWindow(w);
		}
	}

    public int restoreSnap(){
        if(!isSnapped) {
            SnapGravity = 0;
            return 0;
        }
        int newFlag = 0;
        if(width!=-1){
            newFlag |= (x==0)?Gravity.LEFT : Gravity.RIGHT;
        }
        if(height!=-1){
            newFlag |= (y==0)?Gravity.TOP : Gravity.BOTTOM;
        }
        SnapGravity = newFlag;
        return newFlag;
    }

}

class SnapWindowHolder{
    public int x;
    public int y;
    public int height;
    public int width;
    public int SnapGravity;
    public boolean isSnapped = false;
    public void updateSnap(int newSnap){
        //if(SnapGravity == newSnap) return;
        SnapGravity = newSnap;
        isSnapped=(SnapGravity != 0);
        //isMaximized=(SnapGravity == Gravity.FILL);
    }
}
