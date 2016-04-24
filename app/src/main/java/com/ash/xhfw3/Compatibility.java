package com.ash.xhfw3;

import android.view.Gravity;

import java.util.ArrayList;
import java.util.Arrays;
import android.os.*;
import de.robv.android.xposed.*;

public class Compatibility
{
	
	
	public static class Hooks{
		public static int ActivityRecord_Intent = 4;
		public static int ActivityRecord_ActivityInfo = 6;
		public static int ActivityRecord_ActivityStack = -1;
		public static int ActivityRecord_StackSupervisor = 13;
		public static String ActivityRecord_TaskHistory = "mTaskHistory";
		public static Object getActivityRecord_ActivityStack(Object stackSupervisor){
			Object activityStack;
			try{
				activityStack = XposedHelpers.callMethod(stackSupervisor, "getFocusedStack");
				} catch(Exception e){
					activityStack = XposedHelpers.getObjectField(stackSupervisor, "mFocusedStack");
				}
			return activityStack;
		}
		
		Hooks(){
			int SDK = (Build.VERSION.SDK_INT<17?17:Build.VERSION.SDK_INT);
			switch(SDK){
				case 17:
					ActivityRecord_Intent = 4;
					ActivityRecord_ActivityInfo = 6;
					ActivityRecord_ActivityStack = 1;
					ActivityRecord_StackSupervisor = -1;
					ActivityRecord_TaskHistory = "mHistory";
					break;
				case 18:
					ActivityRecord_Intent = 5;
					ActivityRecord_ActivityInfo = 7;
					ActivityRecord_ActivityStack = 1;
					ActivityRecord_StackSupervisor = -1;
					ActivityRecord_TaskHistory = "mHistory";
					break;
				case 19:
				case 20:
				case 21:
				case 22:
					ActivityRecord_Intent = 4;
					ActivityRecord_ActivityInfo = 6;
					ActivityRecord_ActivityStack = -1;
					ActivityRecord_StackSupervisor = 12;
					ActivityRecord_TaskHistory = "mTaskHistory";
					break;
				case 23:
				default:
					ActivityRecord_Intent = 4;
					ActivityRecord_ActivityInfo = 6;
					ActivityRecord_ActivityStack = -1;
					ActivityRecord_StackSupervisor = 13;
					ActivityRecord_TaskHistory = "mTaskHistory";
					break;
			}
			
		}
	}
	
	public final class AeroSnap{
		public final static int SNAP_NONE = 0;
		public final static int SNAP_LEFT = 1;
		public final static int SNAP_TOP = 2;
		public final static int SNAP_RIGHT = 3;
		public final static int SNAP_BOTTOM = 4;
		//4WAYMOD snaps
		public final static int SNAP_TOPLEFT = 21;
		public final static int SNAP_TOPRIGHT = 23;
		public final static int SNAP_BOTTOMLEFT = 41;
		public final static int SNAP_BOTTOMRIGHT = 43;
	}

	//public final static int AeroSnapNone = 0;

	final static ArrayList<Integer> snapSideReplaceTable = new ArrayList<Integer>(Arrays.asList(AeroSnap.SNAP_TOPLEFT, AeroSnap.SNAP_TOP, AeroSnap.SNAP_TOPRIGHT,
			AeroSnap.SNAP_RIGHT,
			AeroSnap.SNAP_BOTTOMRIGHT, AeroSnap.SNAP_BOTTOM, AeroSnap.SNAP_BOTTOMLEFT,
			AeroSnap.SNAP_LEFT, AeroSnap.SNAP_NONE));
	final static ArrayList<Integer> snapGravityReplaceTable = new ArrayList<Integer>(Arrays.asList(Gravity.TOP | Gravity.LEFT, Gravity.TOP, Gravity.TOP | Gravity.RIGHT,
			Gravity.RIGHT,
			Gravity.BOTTOM | Gravity.RIGHT, Gravity.BOTTOM, Gravity.BOTTOM | Gravity.LEFT,
			Gravity.LEFT, 0));

	public static int snapGravityToSide(int snapGravity){
		int index = snapGravityReplaceTable.indexOf(snapGravity);
		if(index >=0) return snapSideReplaceTable.get(index);
		else return AeroSnap.SNAP_NONE;
	}

	public static int snapSideToGravity(int snapSide){
		int index = snapSideReplaceTable.indexOf(snapSide);
		if(index>=0) return snapGravityReplaceTable.get(index);
		else return 0;
	}
}
