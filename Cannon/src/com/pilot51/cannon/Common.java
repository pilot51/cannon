package com.pilot51.cannon;

import android.app.Activity;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.adwhirl.AdWhirlLayout;

public class Common {
	void ad(final Activity a) {
		LinearLayout layout = (LinearLayout)a.findViewById(R.id.layoutAd);
	        if(layout == null) {
//	            Log.e("AdWhirl", "Layout is null!");
	            return;
		    }
		    float density = a.getResources().getDisplayMetrics().density;
//		    int width = (int) (320 * density);
		    int height = (int) (52 * density);
//		    layout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
		    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, height);
		    lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
			layout.addView(new AdWhirlLayout(a, "4180d5072cfd451298d4ec8d19cddcba"), lp);
			layout.invalidate();
			/*
			new CountDownTimer(10000, 10000) {
				public void onTick(long millisUntilFinished) {}
				@Override
				public void onFinish() {
					try {
						if (!((AdView)a.findViewById(R.id.ad)).hasAd()) {
							a.findViewById(R.id.layoutAd).setVisibility(LinearLayout.GONE);
						}
					} catch (Exception e) {}
				}
			}.start();
			*/
	}
}
