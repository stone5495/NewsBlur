package com.newsblur.util;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.WHITE;
import static android.graphics.PorterDuff.Mode.DST_IN;

import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.newsblur.R;
import com.newsblur.activity.NewsBlurApplication;

public class UIUtils {
	
	/*
	 * Based on the RoundedCorners code from Square / Eric Burke's "Android UI" talk 
	 * and the GitHub Android code.
	 * https://github.com/github/android
	 */
	
	public static Bitmap roundCorners(Bitmap source, final float radius) {
        int width = source.getWidth();
        int height = source.getHeight();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(WHITE);

        Bitmap clipped = Bitmap.createBitmap(width, height, ARGB_8888);
        Canvas canvas = new Canvas(clipped);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(DST_IN));
        
        Bitmap rounded = Bitmap.createBitmap(width, height, ARGB_8888);
        canvas = new Canvas(rounded);
        canvas.drawBitmap(source, 0, 0, null);
        canvas.drawBitmap(clipped, 0, 0, paint);

        clipped.recycle();

        return rounded;
    }
	
	/*
	 * Convert from device-independent-pixels to pixels for use in custom view drawing, as
	 * used throughout Android. 
	 * See: http://bit.ly/MfsAUZ (Romain Guy's comment)  
	 */
	public static int dp2px(Context context, int dp) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

    public static float px2dp(Context context, int px) {
        return ((float) px) / context.getResources().getDisplayMetrics().density;
    }

    /**
     * Sets the alpha of a view, totally hiding the view if the alpha is so low
     * as to be invisible, but also obeying intended visibility.
     */
    public static void setViewAlpha(View v, float alpha, boolean visible) {
        v.setAlpha(alpha);
        if ((alpha < 0.001f) || !visible) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up our customised ActionBar view that features the specified icon and title, sized
     * away from system standard to meet the NewsBlur visual style.
     */
    public static void setCustomActionBar(Activity activity, String imageUrl, String title) { 
        ImageView iconView = setupCustomActionbar(activity, title);
        ((NewsBlurApplication) activity.getApplicationContext()).getImageLoader().displayImage(imageUrl, iconView, false);
    }

    public static void setCustomActionBar(Activity activity, int imageId, String title) { 
        ImageView iconView = setupCustomActionbar(activity, title);
        iconView.setImageResource(imageId);
    }

    private static ImageView setupCustomActionbar(final Activity activity, String title) {
        // we completely replace the existing title and 'home' icon with a custom view
        activity.getActionBar().setDisplayShowCustomEnabled(true);
        activity.getActionBar().setDisplayShowTitleEnabled(false);
        activity.getActionBar().setDisplayShowHomeEnabled(false);
        View v = LayoutInflater.from(activity).inflate(R.layout.actionbar_custom_icon, null);
        TextView titleView = ((TextView) v.findViewById(R.id.actionbar_text));
        titleView.setText(title);
        ImageView iconView = ((ImageView) v.findViewById(R.id.actionbar_icon));
        // using a custom view breaks the system-standard ability to tap the icon or title to return
        // to the previous activity. Re-implement that here.
        titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        iconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        activity.getActionBar().setCustomView(v, new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
        return iconView;
    }

    /**
     * Shows a toast in a circumstance where the context might be null.  This can very
     * rarely happen when toasts are done from async tasks and the context is finished
     * before the task completes, resulting in a crash.  This prevents the crash at the 
     * cost of the toast not being shown.
     */
    public static void safeToast(Context c, int rid, int duration) {
        if (c != null) {
            Toast.makeText(c, rid, duration).show();
        }
    }

    public static void safeToast(Context c, String text, int duration) {
        if ((c != null) && (text != null)) {
            Toast.makeText(c, text, duration).show();
        }
    }

    /**
     * Restart an activity. See http://stackoverflow.com/a/11651252/70795
     * We post this on the Handler to allow onResume to finish before the activity restarts
     * and avoid an exception.
     */
    public static void restartActivity(final Activity activity) {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                Intent intent = activity.getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.overridePendingTransition(0, 0);
                activity.finish();

                activity.overridePendingTransition(0, 0);
                activity.startActivity(intent);
            }
        });
    }
}
