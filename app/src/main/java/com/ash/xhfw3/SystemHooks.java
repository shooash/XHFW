package com.ash.xhfw3;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.*;
import android.content.*;
import android.content.pm.*;
import java.util.*;
import android.os.*;

public class SystemHooks
{
	static boolean isMovable = false;
	public static void hookActivityRecord(Class<?> classActivityRecord, final MainXposed mMainXposed) throws Throwable {

		XposedBridge.hookAllConstructors(classActivityRecord, 
			new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					ActivityInfo mActivityInfo = (ActivityInfo) param.args[MainXposed.mCompatibility.ActivityRecord_ActivityInfo];
					String packageName = mActivityInfo.applicationInfo.packageName;
					isMovable = false;
					if ((packageName.startsWith("com.android.systemui"))||(packageName.equals("android"))) return;
					
					Intent mIntent = (Intent) param.args[MainXposed.mCompatibility.ActivityRecord_Intent];
					isMovable = Util.isFlag(mIntent.getFlags(), MainXposed.mPref.getInt(Common.KEY_FLOATING_FLAG, Common.FLAG_FLOATING_WINDOW));
					isMovable = isMovable || 
						checkInheritFloatingFlag(packageName,
							(MainXposed.mCompatibility.ActivityRecord_ActivityStack==-1)? 
								MainXposed.mCompatibility.getActivityRecord_ActivityStack(param.args[MainXposed.mCompatibility.ActivityRecord_StackSupervisor]) 
								: param.args[MainXposed.mCompatibility.ActivityRecord_ActivityStack]);
					isMovable = isMovable || mMainXposed.mMovablePackages.contains(packageName);
					if(!isMovable) {
						MovableWindow.DEBUG(packageName + " hookActivityRecord.isMovable:[" + isMovable + "]");
						return;
						}
					if(!mMainXposed.mMovablePackages.contains(packageName))
						mMainXposed.mMovablePackages.add(packageName);
					XposedHelpers.setBooleanField(param.thisObject, "fullscreen", false);
					setIntentFlags(mIntent);
					}
			});//XposedBridge.hookAllConstructors(classActivityRecord, XC_MethodHook);
	}

	private static Intent setIntentFlags(Intent mIntent){
		int flags = mIntent.getFlags();
		flags = flags | MainXposed.mPref.getInt(Common.KEY_FLOATING_FLAG, Common.FLAG_FLOATING_WINDOW);
		flags = flags | Intent.FLAG_ACTIVITY_NO_USER_ACTION;
		flags &= ~Intent.FLAG_ACTIVITY_TASK_ON_HOME;

		if (!MainXposed.mPref.getBoolean(Common.KEY_SHOW_APP_IN_RECENTS, Common.DEFAULT_SHOW_APP_IN_RECENTS)) {
			flags = flags | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
		} else if (MainXposed.mPref.getBoolean(Common.KEY_FORCE_APP_IN_RECENTS, Common.DEFAULT_FORCE_APP_IN_RECENTS)) {
			flags &= ~Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
		}

		mIntent.setFlags(flags);
		return mIntent;
	}
	
	private static boolean checkInheritFloatingFlag(String packageName, Object activityStack){
		ArrayList<?> taskHistory = (ArrayList<?>) XposedHelpers.getObjectField(activityStack, MainXposed.mCompatibility.ActivityRecord_TaskHistory);
		if(taskHistory==null || taskHistory.size()==0) return false;
		Object lastRecord = taskHistory.get(taskHistory.size() - 1);
		Intent lastIntent = (Intent) XposedHelpers.getObjectField(lastRecord, "intent");
		if(lastIntent==null) return false;
		if((packageName.equals(lastIntent.getPackage()))) return Util.isFlag(lastIntent.getFlags(), MainXposed.mPref.getInt(Common.KEY_FLOATING_FLAG, Common.FLAG_FLOATING_WINDOW));
		//todo check if should inherit anyway
		return false;
	}
	
	public static void removeAppStartingWindow(final Class<?> hookClass) throws Throwable {
		XposedBridge.hookAllMethods(hookClass, "setAppStartingWindow", new XC_MethodHook() {
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					MovableWindow.DEBUG("setAppStartingWindow.isMovable:[" + isMovable + "]");
					if (!isMovable) return;
					//if (!mHasHaloFlag && (MovableWindow.mWindowHolder==null || !MovableWindow.mWindowHolder.isFloating)) return;
					if ("android".equals((String) param.args[1])) return;
					// Change boolean "createIfNeeded" to FALSE
					if (param.args[param.args.length - 1] instanceof Boolean) {
						param.args[param.args.length - 1] = Boolean.FALSE;
						// Last param of the arguments
						// It's length has changed in almost all versions of Android.
						// Since it is always the last value, we use this to our advantage.
					}
				}
			});
		}
	
	public static void hookActivityStack(Class<?> hookClass){
		/* This is a Kitkat work-around to make sure the background is transparent */
		XposedBridge.hookAllMethods(hookClass, "startActivityLocked", new XC_MethodHook() {
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					MovableWindow.DEBUG("HaloFloating startActivityLocked [" + isMovable + "]");
					if (!isMovable&&!MovableWindow.isMovable) return;
					if (param.args[1] instanceof Intent) return;
					Object activityRecord = param.args[0];
					XposedHelpers.setBooleanField(activityRecord, "fullscreen", false);
				}
			});

		if (Build.VERSION.SDK_INT < 19) {
			/*
			 * Prevents the App from bringing the home to the front.
			 * Doesn't exists on Kitkat so it is not needed
			 */
			XposedBridge.hookAllMethods(hookClass, "moveHomeToFrontFromLaunchLocked", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						int launchFlags = (Integer) param.args[0];
						if (Util.isFlag(launchFlags,Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME)) {
							if (Util.isFlag(launchFlags,MainXposed.mPref.getInt(Common.KEY_FLOATING_FLAG, Common.FLAG_FLOATING_WINDOW))) param.setResult(null);
							// if the app is a floating app, and is a new task on home.
							// then skip this method.
						} else {
							param.setResult(null);
							// This is not a new task on home. Dont allow the method to continue.
							// Since there is no point to run method which checks for the same thing
						}
					}
				});
			}//for SDK_INT 19 only
	}
	
}
