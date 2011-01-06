package com.pilot51.cannon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
	}
	
	void menu(final Activity a) {
		String app_name, app_ver, about_text,
		dev_name, dev_email, dev_web, dev_twitter, about;
		AlertDialog dialogAbout;
		
		app_name = a.getString(R.string.app_name);
		app_ver = a.getString(R.string.app_version);
		about_text = a.getString(R.string.about_text);
		dev_name = a.getString(R.string.dev_name);
		dev_email = a.getString(R.string.dev_email);
		dev_web = a.getString(R.string.dev_web);
		dev_twitter = a.getString(R.string.dev_twitter);
		about = a.getString(R.string.about);

		dialogAbout = new AlertDialog.Builder(a).create();
		
		TextView messageAbout = new TextView(a);
		SpannableString s = new SpannableString(app_name
				+ " - version " + app_ver + "\nBy " + dev_name + "\n"
				+ dev_email + "\n" + dev_web + "\n" + dev_twitter
				+ "\n\n\n" + about_text);
		Linkify.addLinks(s, Linkify.WEB_URLS);
		messageAbout.setText(s);
		messageAbout.setMovementMethod(LinkMovementMethod.getInstance());
		dialogAbout.setTitle(about + " " + app_name);
		dialogAbout.setIcon(R.drawable.icon);
		dialogAbout.setView(messageAbout);
		dialogAbout.setButton("More apps",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						a.startActivity(new Intent(Intent.ACTION_VIEW, Uri
								.parse("market://search?q=pub:Pilot_51")));
					}
				});
		dialogAbout.show();
	}
}