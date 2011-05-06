package com.smsecure;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public final class TworzenieSMS extends Activity {

	// public static final String ODBIORCA = "com.smsecure.ODBIORCA";
	Button przyciskWyslij;
	EditText numerTelefonu;
	EditText tekstWiadomosci;
	ToggleButton wlaczenieSzyfrowania;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// if (getIntent().hasExtra(ODBIORCA)) {
		// ((TextView) findViewById(R.id.numerTelefonu)).setText(getIntent()
		// .getExtras().getString(ODBIORCA));
		// ((TextView) findViewById(R.id.tekstWiadomosci)).requestFocus();
		// }
		numerTelefonu = (EditText) TworzenieSMS.this
				.findViewById(R.id.numerTelefonu);
		tekstWiadomosci = (EditText) TworzenieSMS.this
				.findViewById(R.id.tekstWiadomosci);
		przyciskWyslij = (Button) TworzenieSMS.this
				.findViewById(R.id.przyciskWyslij);
		wlaczenieSzyfrowania = (ToggleButton) TworzenieSMS.this
				.findViewById(R.id.wlaczenieSzyfrowania);

		wlaczenieSzyfrowania.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (wlaczenieSzyfrowania.isChecked())
					try {
						Toast.makeText(
								getBaseContext(),
								zaszyfruj(numerTelefonu.getText().toString(),
										tekstWiadomosci.getText().toString()),
								Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						Log.e("blad", e.getMessage(), e.getCause());
						Toast.makeText(getBaseContext(), "blad",
								Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
			}
		});

		przyciskWyslij.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String numer = numerTelefonu.getText().toString();
				String wiadomosc = tekstWiadomosci.getText().toString();
				if (TextUtils.isEmpty(numer))
					Toast.makeText(getBaseContext(), R.string.brakNumeru,
							Toast.LENGTH_SHORT).show();
				else if (TextUtils.isEmpty(wiadomosc))
					Toast.makeText(getBaseContext(), R.string.brakWiadomosci,
							Toast.LENGTH_SHORT).show();
				else {
					try {
						wyslijSMS(numer, zaszyfruj(numer, wiadomosc));
					} catch (Exception e) {
						e.printStackTrace();
					}
					tekstWiadomosci.setText("");
				}
			}
		});
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), R.string.wyslano,
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), R.string.bladGeneric,
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), R.string.brakZasiegu,
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), R.string.pustyPDU,
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), R.string.siecWylaczona,
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter("WYSYLANIE"));

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), R.string.dostarczono,
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), R.string.nieDostarczono,
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter("DOSTARCZANIE"));
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ustawienia:
			startActivity(new Intent(this, EkranUstawien.class));
			break;
		}
		return true;
	}

	private void wyslijSMS(String numer, String tekst) {

		PendingIntent PIw = PendingIntent.getBroadcast(this, 0, new Intent(
				"WYSYLANIE"), 0);
		PendingIntent PId = PendingIntent.getBroadcast(this, 0, new Intent(
				"DOSTARCZANIE"), 0);

		SmsManager sms = SmsManager.getDefault();

		List<String> wiadomosci = sms.divideMessage(tekst);
		if (wiadomosci.size() == 1)
			sms.sendTextMessage(numer, null, "♥" + wiadomosci.get(0), PIw, PId);
		else
			for (int i = 0; i < wiadomosci.size(); i++) {
				if (i == 0)
					sms.sendTextMessage(numer, null, "♥" + wiadomosci.get(i),
							null, null);
				else if (i == wiadomosci.size() - 1)
					sms.sendTextMessage(numer, null, wiadomosci.get(i), PIw,
							PId);
				else
					sms.sendTextMessage(numer, null, wiadomosci.get(i), null,
							null);
			}
	}

	private String zaszyfruj(String numer, String tekst) throws Exception {
		try {
			FileInputStream fos = openFileInput("prywatny");
			fos.close();
		} catch (Exception cos) {
			SecureRandom sr = new SecureRandom();
			KeyPairGenerator generator = null;
			try {
				generator = KeyPairGenerator.getInstance("RSA", "BC");
			} catch (Exception e) {
				e.printStackTrace();
			}
			generator.initialize(512, sr);
			KeyPair kp = generator.generateKeyPair();
			
			PrivateKey prywatny = kp.getPrivate();
			PublicKey publiczny = kp.getPublic();

			FileOutputStream fos = openFileOutput("prywatny",
					Context.MODE_PRIVATE);
			fos.write(prywatny.getEncoded());
			fos.close();

			FileOutputStream fos2 = openFileOutput("publiczny",
					Context.MODE_PRIVATE);
			fos2.write(publiczny.getEncoded());
			fos2.close();
		}

		Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");

		byte[] klucz_publiczny = new byte[128];
		try {
			FileInputStream plik = openFileInput("publiczny");
			int bytes = plik.read(klucz_publiczny);
			plik.close();
			Log.d("tag",String.format("%d",bytes));
			Log.d("tag", new String(klucz_publiczny));
		} catch (Exception cos) {
			Log.e("tag", "brak pliku");
		}
		
		PublicKey publicKey = KeyFactory.getInstance("RSA", "BC")
				.generatePublic(new X509EncodedKeySpec(klucz_publiczny));

		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] ciphertext = cipher.doFinal(tekst.getBytes());
		
		Log.d("wiadomosc wyslana",new String(ciphertext)+"|| "+ciphertext.length);
		
		return new String(ciphertext);
	}

	/*public static byte[] encrypt() throws Exception {
		String seed = "SuperSecretPassword";
		String plaintext = "This is insecure data!";

		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		SecureRandom secrand = SecureRandom.getInstance("SHA1PRNG");

		secrand.setSeed(seed.getBytes());
		keygen.init(128, secrand);

		SecretKey seckey = keygen.generateKey();
		byte[] rawKey = seckey.getEncoded();

		SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(plaintext.getBytes());

		return encrypted;
	}

	public byte[] SHA(String tekst) throws NoSuchAlgorithmException {

		MessageDigest md;
		md = MessageDigest.getInstance("SHA");
		md.update(tekst.getBytes());

		return md.digest();
	}*/
}