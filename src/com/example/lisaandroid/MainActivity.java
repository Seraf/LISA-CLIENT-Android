package com.example.lisaandroid;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private static final int REQUEST_CODE = 1234;
	private ListView		resultList;	
	
	private static final int LISA_PORT = 10042;
	private static final String LISA_SRV = "demo.lisa-project.net";
	
	private Socket			socket;
	private InputStream 	mInStream;
	private OutputStream	mOutStream;
			

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button speakButton = (Button) findViewById(R.id.speakButton);
		resultList = (ListView) findViewById(R.id.list);

		// Disable button if no recognition service is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(
		new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			speakButton.setEnabled(false);
			Toast.makeText(getApplicationContext(), "Speech recognizer Not Found", 1000).show();
		}
		
		//TODO ca degage des que c'est OK pour la com.
		Button quickTestBtn = (Button) findViewById(R.id.quick_test_btn);
		quickTestBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String cmd = "il y a quoi au cinema";
				JSONObject commandJson = new JSONObject();

				try {
					commandJson.put("type", "Speech");
					commandJson.put("body", cmd);
					commandJson.put("from", "Android");
					commandJson.put("zone", "Android");

					new getData().execute(commandJson.toString());
				} catch (JSONException e2) {
					e2.printStackTrace();
				}
				Toast.makeText(getApplicationContext(), "envoi : 'il y a quoi au cine'", Toast.LENGTH_LONG).show();
			}
		});
		
		speakButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});
	}

	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "LISA Voice Recognition...");
		startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data.getStringArrayListExtra(
			RecognizerIntent.EXTRA_RESULTS);
			resultList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
			matches));

			//TODO : si item 1 > xx% -> envoie direct au serveur.
			//Sinon poser la question entre item 1 ou item 2
			resultList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					String cmd = String.valueOf(((TextView)arg1).getText());
					JSONObject commandJson = new JSONObject();

					try {
						commandJson.put("type", "Speech");
						commandJson.put("body", cmd);
						commandJson.put("from", "Android");
						commandJson.put("zone", "Android");

						new getData().execute(commandJson.toString());
					} catch (JSONException e2) {
						e2.printStackTrace();
					}
				}
			});
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class getData extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... toSend) {

			SocketAddress sockaddr = new InetSocketAddress(LISA_SRV, LISA_PORT);
	        Socket nsocket = new Socket();
	        try {
//				nsocket.setSoTimeout(3000);
		        nsocket.connect(sockaddr, 5000); 
		        if (nsocket.isConnected()) {
		            mInStream = nsocket.getInputStream();
		            mOutStream = nsocket.getOutputStream();
		        }
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				mOutStream.write(toSend[0].getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			byte[] buffer = new byte[1024];
			
			try {
				 BufferedInputStream in = new BufferedInputStream(nsocket.getInputStream());
				 int bytesRead=0;
				 String strStreamContents; 
				 
				 while((bytesRead = in.read(buffer)) != -1){ 
					 strStreamContents = new String(buffer, 0, bytesRead);               
					 Log.w("RECEIVED 2 : ", strStreamContents);
				 }
				 Log.w("RECEIVED 1 : ", buffer.toString());

//				parseAnswer(buffer.toString());

			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		private void parseAnswer(String res) {

			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(res);

				String tmp = jsonObj.getString("body");
				Log.w("REPONSE BODY :", tmp);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}