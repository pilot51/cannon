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
			app_name = a.getString(R.string.app_name),
			app_ver = a.getString(R.string.app_version),
			app_web = a.getString(R.string.app_web),
			about_text = a.getString(R.string.about_text),
			dev_name = a.getString(R.string.dev_name),
			dev_email = a.getString(R.string.dev_email),
			dev_web = a.getString(R.string.dev_web),
			dev_twitter = a.getString(R.string.dev_twitter),
			about = a.getString(R.string.about);

		AlertDialog dialogAbout = new AlertDialog.Builder(a).create();
		
		TextView messageAbout = new TextView(a);
		messageAbout.setMovementMethod(LinkMovementMethod.getInstance());
		messageAbout.setTextColor(Color.LTGRAY);
		SpannableString s = new SpannableString(app_name + " - version "
				+ app_ver + "\n" + app_web + "\nBy " + dev_name + "\n" + dev_email + "\n"
				+ dev_web + "\n" + dev_twitter + "\n\n\n" + about_text);
		Linkify.addLinks(s, Linkify.WEB_URLS);
		messageAbout.setText(s);
		dialogAbout.setTitle(about);
		dialogAbout.setIcon(R.drawable.icon);
		dialogAbout.setView(messageAbout);
		dialogAbout.setButton("More apps",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						a.startActivity(new Intent(Intent.ACTION_VIEW, Uri
								.parse("market://search?q=pub:Pilot_51")));
		}});
		dialogAbout.show();
	}
}