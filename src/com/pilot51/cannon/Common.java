package com.pilot51.cannon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class Common {
	void menu(final Activity a) {
		String
			appName = a.getString(R.string.app_name),
			appVer = a.getString(R.string.app_version),
			appWeb = a.getString(R.string.app_web),
			devName = a.getString(R.string.dev_name),
			devEmail = a.getString(R.string.dev_email),
			devWeb = a.getString(R.string.dev_web),
			aboutInstructions = a.getString(R.string.about_instructions);
		
		TextView messageAbout = new TextView(a);
		messageAbout.setMovementMethod(LinkMovementMethod.getInstance());
		messageAbout.setTextColor(Color.LTGRAY);
		SpannableString s = new SpannableString(a.getString(R.string.about_text, appName, appVer, appWeb, devName, devEmail, devWeb, aboutInstructions));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		messageAbout.setText(s);
		AlertDialog dialogAbout = new AlertDialog.Builder(a).create();
		dialogAbout.setTitle(a.getString(R.string.about));
		dialogAbout.setIcon(R.drawable.icon);
		dialogAbout.setView(messageAbout);
		dialogAbout.setButton(a.getString(R.string.more_apps), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Pilot_51")));
			}
		});
		dialogAbout.show();
	}
}