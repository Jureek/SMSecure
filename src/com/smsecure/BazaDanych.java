package com.smsecure;

import java.io.UnsupportedEncodingException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.telephony.SmsMessage;
import android.util.Log;

public class BazaDanych {

	public static final String P_ID = BaseColumns._ID;
	public static final String P_NADAWCA = "Nadawca";
	public static final String P_TRESC = "Tresc";
	public static final String P_DATA = "TimeStamp";
	public static final String P_NAZWA = "Nazwa";
	public static final String P_WARTOSC = "Wartosc";

	Context context;
	DbManage zarzadzaj;

	public BazaDanych(Context context) {
		this.context = context;
		zarzadzaj = new DbManage();
	}

	public void close() {
		zarzadzaj.close();
	}

	public long insert(ContentValues values) {
		SQLiteDatabase db = zarzadzaj.getWritableDatabase();

		long ret;
		try {
			ret = db.insertOrThrow(DbManage.TABELA_SMS, null, values);
		} catch (SQLException e) {
			ret = -1;
		} finally {
			db.close();
		}
		return ret;
	}

	public long insert(SmsMessage[] sms, int i) throws UnsupportedEncodingException {
		ContentValues wartosci = new ContentValues();
		wartosci.put(BazaDanych.P_ID, sms[i].hashCode());
		wartosci.put(BazaDanych.P_NADAWCA, sms[i].getOriginatingAddress());
		wartosci.put(BazaDanych.P_TRESC, sms[i].getMessageBody().substring(1).getBytes("UTF-8"));
		wartosci.put(BazaDanych.P_DATA, sms[i].getTimestampMillis());
		Log.d("kurwaaaaa",String.format("%d", sms[i].getMessageBody().substring(1).getBytes("UTF-8").length));
		return this.insert(wartosci);
	}

	// public long insert(byte[] klucz, int id) {
	// String nazwa;
	// if (id == 1)
	// nazwa = "priv";
	// else
	// nazwa = "pub";
	// ContentValues wartosci = new ContentValues();
	// wartosci.put(BazaDanych.P_ID, id);
	// wartosci.put(BazaDanych.P_NAZWA, nazwa);
	// wartosci.put(BazaDanych.P_WARTOSC, klucz);
	// return this.insert(wartosci, 1);
	// }

	public void delete(long timestamp) {
		SQLiteDatabase db = zarzadzaj.getWritableDatabase();
		db.delete(DbManage.TABELA_SMS, P_DATA + "=" + timestamp, null);
		db.close();
	}

	public Cursor query() {
		SQLiteDatabase db = zarzadzaj.getReadableDatabase();//getWritableDatabase();// 

		return db.query(DbManage.TABELA_SMS, null, null, null, null, null,
				P_DATA + " DESC");
	}

	// public Cursor kluczPrywatny() {
	// SQLiteDatabase db = zarzadzaj.getReadableDatabase();
	// return db.query(DbManage.TABELA_KEY, null, P_NAZWA + "='priv'", null,
	// null, null, null);
	// }

	private class DbManage extends SQLiteOpenHelper {
		public static final String DB_NAME = "messages.dat";
		public static final int DB_VERSION = 1;
		public static final String TABELA_SMS = "SMSY";

		public DbManage() {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			String sql = String
					.format("CREATE TABLE %s (%s INT PRIMARY KEY,%s INT,%s TEXT,%s BLOB);",
							TABELA_SMS, P_ID, P_DATA, P_NADAWCA, P_TRESC);
			db.execSQL(sql);
			// sql = String.format(
			// "CREATE TABLE %s (%s INT PRIMARY KEY,%s TEXT,%s TEXT);",
			// TABELA_KEY, P_ID, P_NAZWA, P_WARTOSC);
			// db.execSQL(sql);
			// sql = String.format("INSERT INTO %s VALUES (1,'%s','%s');",
			// TABELA_KEY, "priv", "S"+kluczp);
			// db.execSQL(sql);
			// sql = String.format("INSERT INTO %s VALUES (2,'%s','%s');",
			// TABELA_KEY, "pub", "S"+kluczpub);
			// db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			this.onCreate(db);
		}

	}

}
