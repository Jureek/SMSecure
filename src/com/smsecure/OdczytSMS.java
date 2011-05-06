package com.smsecure;

import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class OdczytSMS extends Activity {
	ListView listaSms;

	Cursor cursor;
	SimpleCursorAdapter adapter;
	BazaDanych db;
	Button nowySMS;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listasms);

		nowySMS = (Button) findViewById(R.id.nowySMS);
		listaSms = (ListView) findViewById(R.id.listaSms);

		nowySMS.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(OdczytSMS.this, TworzenieSMS.class)
						.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
			}
		});

		wlaczListe();

		listaSms.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				final CharSequence[] items = { "Usuń" };

				AlertDialog.Builder builder = new AlertDialog.Builder(
						OdczytSMS.this);
				builder.setTitle("Wybierz opcję");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Cursor cos;
						// switch (item) {
						// case 0:
						cos = (Cursor) listaSms.getAdapter().getItem(arg2);
						db.delete(cos.getLong(1));
						wlaczListe();
						// adapter.notifyDataSetChanged();
					}
				});
				AlertDialog alert = builder.create();
				alert.setOwnerActivity(OdczytSMS.this);
				alert.show();
				return false;
			}
		});
	}

	private void wlaczListe() {
		db = new BazaDanych(this);
		cursor = db.query();
		startManagingCursor(cursor);

		String[] from = { BazaDanych.P_DATA, BazaDanych.P_NADAWCA,
				BazaDanych.P_TRESC };
		int[] to = { R.id.data, R.id.autor, R.id.wiadomosc };

		adapter = new SimpleCursorAdapter(this, R.layout.wiersz, cursor, from,
				to);
		adapter.setViewBinder(VIEW_BINDER);
		listaSms.setAdapter(adapter);
		db.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		wlaczListe();
	}

	public final ViewBinder VIEW_BINDER = new ViewBinder() {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			//
			//Zamiana timestamp na relativeTime
			//
			if (cursor.getColumnIndex(BazaDanych.P_DATA) == columnIndex) {
				long timestamp = cursor.getLong(columnIndex);
				CharSequence relativeTime = DateUtils
						.getRelativeTimeSpanString(timestamp);
				((TextView) view).setText(relativeTime);
				return true;
			} else if (cursor.getColumnIndex(BazaDanych.P_TRESC) == columnIndex) {
				//
				//Odszyfrowanie każdej wiadomości osobno
				//
				byte[] zaszyfrowany_tekst = cursor.getBlob(columnIndex);
				
				Log.d("wiadomosc odebrana", new String(zaszyfrowany_tekst) + "||"+zaszyfrowany_tekst.length);
				
				byte[] klucz_prywatny = new byte[512];
				try {
					FileInputStream plik = openFileInput("prywatny");
					plik.read(klucz_prywatny);
					plik.close();
				} catch (Exception cos) {
					Log.e("tag", "brak pliku");
				}

				try {
					PrivateKey privateKey = KeyFactory.getInstance("RSA", "BC")
							.generatePrivate(
									new PKCS8EncodedKeySpec(klucz_prywatny));

					Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding",
							"BC");
					cipher.init(Cipher.DECRYPT_MODE, privateKey);
					byte[] tekst = cipher.doFinal(zaszyfrowany_tekst);
					Log.d("tag", new String(tekst));
					((TextView) view).setText(new String(tekst));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			} else
				return false;
		}

	};
}
