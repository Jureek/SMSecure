package com.smsecure;

import java.io.UnsupportedEncodingException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

//import android.widget.Toast;

public class OtrzymywanieSMS extends BroadcastReceiver {

	BazaDanych db;

	@Override
	public void onReceive(Context con, Intent intent) {
		db = new BazaDanych(con);
		Bundle bundle = intent.getExtras();

		Object[] wiadomosci = (Object[]) bundle.get("pdus");
		SmsMessage[] SMS = new SmsMessage[wiadomosci.length];
		for (int i = 0; i < wiadomosci.length; i++)
			SMS[i] = SmsMessage.createFromPdu((byte[]) wiadomosci[i]);

		// if(SMS[0].getOriginatingAddress().equals("800")) {
		if (SMS[0].getMessageBody().startsWith("♥")) {
			abortBroadcast();
			// Toast.makeText(con,"Masz SMSa z serduszkiem!: " +
			// SMS[0].getMessageBody().replaceFirst("♥",
			// ""),Toast.LENGTH_LONG).show();
			for (int i = 0; i < wiadomosci.length; i++)
				try {
					db.insert(SMS, i);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
		}
		db.close();
	}
}
