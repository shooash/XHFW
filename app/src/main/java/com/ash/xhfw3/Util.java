package com.ash.xhfw3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import de.robv.android.xposed.XposedHelpers;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.app.*;
import android.view.*;
import android.content.res.*;


public class Util
{
	/* Get System DPI from build.prop 
	 * Some ROMs have Per-App DPI and it might make our views inconsistent 
	 * Fallback to app dpi if it fails*/
	public static int realDp(int dp, Context c) {
		String dpi = "";
		try {
			Process p = new ProcessBuilder("/system/bin/getprop", "ro.sf.lcd_density")
				.redirectErrorStream(true).start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				dpi = line;
			}
			p.destroy();
		} catch (Exception e) {
			dpi = "0";
			//failed, set to zero.
		}
		float scale = Integer.parseInt(dpi);
		if (scale == 0) {
			// zero means it failed in getting dpi, fallback to app dpi 
			scale = c.getResources().getDisplayMetrics().density;
		} else {
			scale = (scale / 160);
		}
		return (int) (dp * scale + 0.5f);
	}

	/* Get App DPI */
	public static int dp(int dp, Context c) {
		float scale = c.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	/* Create a Border */
	public static ShapeDrawable makeOutline(int color, int thickness) {
		ShapeDrawable rectShapeDrawable = new ShapeDrawable(new RectShape());
		Paint paint = rectShapeDrawable.getPaint();
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(thickness);
		return rectShapeDrawable;
	}

	public static ShapeDrawable makeCircle(int color, int diameter) {
		ShapeDrawable shape = new ShapeDrawable(new OvalShape());
		Paint paint = shape.getPaint();
		paint.setColor(color);
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		shape.setIntrinsicHeight(diameter);
		shape.setIntrinsicWidth(diameter);
		return shape;
	}

	public static LayerDrawable makeDoubleCircle(int colorouter, int colorinner, int diameterouter, int diameterinner)
	{
		//ShapeDrawable shape = new ShapeDrawable(new OvalShape());
		LayerDrawable result = new LayerDrawable(new Drawable[]{makeCircle(colorouter,diameterouter), makeCircle(colorinner,diameterinner)});
		//(makeCircle(colorouter, diameterouter).g);
		if(Build.VERSION.SDK_INT>=23)
			result.setLayerGravity(result.getNumberOfLayers()-1, Gravity.CENTER);
		else
			result.setLayerInset(result.getNumberOfLayers()-1, (diameterouter-diameterinner)/2,  (diameterouter-diameterinner)/2, (diameterouter-diameterinner)/2, (diameterouter-diameterinner)/2);
		return result;
	}
	
	/* Set background drawable based on the API */
	@SuppressWarnings("deprecation")
	public static void setBackgroundDrawable(View view, Drawable drawable) {
		if (Build.VERSION.SDK_INT >= 16) {
			view.setBackground(drawable);
		} else {
			view.setBackgroundDrawable(drawable);
		}
	}
	
	public static int getDisplayRotation(Context mActivity) {
        Display display = ((WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getRotation();
    }
	
	
	public static int getScreenOrientation(Activity mActivity)
	{
		Point screenSize = new Point();
		((WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(screenSize);
		int orientation;
		if(screenSize.x < screenSize.y){
			orientation = Configuration.ORIENTATION_PORTRAIT;
		}else {
			orientation = Configuration.ORIENTATION_LANDSCAPE;
		}
		return orientation;
	}

	public static boolean isFlag(int flagHolder, int flag){
		return ((flagHolder & flag) == flag);
	}
	
	public static void addPrivateFlagNoMoveAnimationToLayoutParam(WindowManager.LayoutParams params) {
		if (Build.VERSION.SDK_INT <= 15) return;

		try {
			Field fieldPrivateFlag = XposedHelpers.findField(WindowManager.LayoutParams.class, "privateFlags");
			fieldPrivateFlag.setInt(params, (fieldPrivateFlag.getInt(params) | 0x00000040));
		} catch (Exception e) {
			//Just pass
		}
	}
	/* this private flag is only in JB and above to turn off move animation.
	 * we need this to speed up our resizing */
	// params.privateFlags |= 0x00000040; //PRIVATE_FLAG_NO_MOVE_ANIMATION
	

}
