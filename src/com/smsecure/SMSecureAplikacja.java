package com.smsecure;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class SMSecureAplikacja extends Application implements OnSharedPreferenceChangeListener{
	SharedPreferences ustawienia;

	@Override
	public void onCreate() {
		super.onCreate();
		ustawienia = PreferenceManager.getDefaultSharedPreferences(this);
		
	}

	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub
	}

}
