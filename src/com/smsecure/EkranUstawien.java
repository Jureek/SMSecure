package com.smsecure;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class EkranUstawien extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.ustawienia);
		addPreferencesFromResource(R.xml.ustawienia);
	}

}
