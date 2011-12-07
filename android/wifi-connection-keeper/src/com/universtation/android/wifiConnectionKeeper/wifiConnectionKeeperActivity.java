package com.universtation.android.wifiConnectionKeeper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class wifiConnectionKeeperActivity extends Activity {
	private WebView webview;
	private Handler mHandler = new Handler();
	Timer timer;
	private int timeSlot = 10000;
	private String connectUrl = "http://www.google.com";
	private EditText timeNumber;
	private TextView lastUpdated;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		timeNumber = (EditText) findViewById(R.id.editText1);
		lastUpdated = (TextView) findViewById(R.id.textViewLastUpdate);
		
		goWebView(connectUrl);

		// ---ToggleButton---
		ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
		toggleButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((ToggleButton) v).isChecked())
					doAction(true);
				else
					doAction(false);
			}
		});
	}

	private void doAction(boolean toggle) {
		if (toggle) {
			Log.v("CONNECTION-KEEPER", "toggled");
			setTimeSlot();
			timer = new Timer();
			timer.schedule(new MyTimerTask(), 0, timeSlot);
		} else {
			if (timer != null) {
				Log.v("CONNECTION-KEEPER", "un-toggled");
				timer.cancel();
				Log.v("CONNECTION-KEEPER", "timer stopped");
			}
		}
	}

	private void setTimeSlot() {
		String value = timeNumber.getText().toString();
		Log.v("CONNECTION-KEEPER", "Time slot is : " + value);
		timeSlot = Integer.valueOf(value) * 1000;
	}

	private String getCurrentTime() {
		String result = "";
		String format = "yyyy-MM-dd hh:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		result = sdf.format(new Date());
		return result;
	}

	private void goWebView(String url) {
		webview = (WebView) findViewById(R.id.webView1);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadUrl(url);
		webview.setWebViewClient(new HelloWebViewClient());
	}

	private class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			// goWebView(url);
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(connectUrl);
			try {
				HttpResponse execute = client.execute(httpGet);
				InputStream content = execute.getEntity().getContent();
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(content));
				String s = "";
				while ((s = buffer.readLine()) != null) {
					response += s;
				}
				Log.v("CONNECTION-KEEPER", "Page loaded");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return response;
		}
	}

	public class MyTimerTask extends TimerTask {
		public void run() {
			mHandler.post(new Runnable() {
				public void run() {
					new DownloadWebPageTask().execute("http://media.daum.net");
					lastUpdated.setText(getCurrentTime());
				};
			});
		}
	}
}