package com.lisa.lisa;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import java.util.ArrayList;
import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.EditText;

import android.content.Intent;

import android.view.Menu;
import android.view.MenuItem;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import com.lisa.speech.activation.SpeechActivationService;

import android.widget.Toast;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

	public class MainActivity extends Activity implements OnInitListener
	{
		
		protected static final int RESULT_SPEECH = 1;
		
		public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
		public TextToSpeech myTTS;
		protected static final int MY_DATA_CHECK_CODE = 0;

    	private EditText editText;
    	private Button send;
	    private ListView mList;
	    private ArrayList<String> arrayList;
	    private MyCustomAdapter mAdapter;
	    private Network mTcpClient;
	    
	    
	    @Override
	    public void onInit(int status) {       
	    	if (status == TextToSpeech.SUCCESS) {
	    		int result = myTTS.setLanguage(Locale.getDefault());
	    		
	    		if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
		          	Toast.makeText(MainActivity.this,
		          			"This Language is not supported", Toast.LENGTH_LONG).show();	    			
	    		}
	    		else {
	    			Toast.makeText(MainActivity.this,
	    				"Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
	    		}
	        }
	        else if (status == TextToSpeech.ERROR) {
	          	Toast.makeText(MainActivity.this,
	          			"Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
	        }
	    }
	    
	    @Override
	    public void onCreate(Bundle savedInstanceState)
	    {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	 
	        //Intent i = SpeechActivationService.makeStartServiceIntent(this,"WordActivator");
	        //startService(i);
	        
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

	        arrayList = new ArrayList<String>();

	        editText = (EditText) findViewById(R.id.editText);
	        send = (Button)findViewById(R.id.send_button);
	 
	        //relate the listView from java to the one created in xml
	        mList = (ListView)findViewById(R.id.list);
	        mAdapter = new MyCustomAdapter(this, arrayList);
	        mList.setAdapter(mAdapter);
	 
	        // connect to the server
	        new connectTask().execute("");
	 
	        send.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	 
	                String message = editText.getText().toString();
	 
	                //add the text in the arrayList
	                arrayList.add("> " + message);
	 
	                //sends the message to the server
	                if (mTcpClient != null) {
	                    mTcpClient.sendMessage(message);
	                }
	 
	                //refresh the list
	                mAdapter.notifyDataSetChanged();
	                editText.setText("");
	            }
	        });
	        
	        Button btnSpeak = (Button) findViewById(R.id.speak_button);
		    
	        btnSpeak.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
	            public void onClick(View v) {
	 
	                Intent intent = new Intent(
	                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	 
	                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
	 
	                try {
	                    startActivityForResult(intent, RESULT_SPEECH);
	                    editText.setText("");
	                } catch (ActivityNotFoundException a) {
	                    Toast t = Toast.makeText(getApplicationContext(),
	                            "Opps! Your device doesn't support Speech to Text",
	                            Toast.LENGTH_SHORT);
	                    t.show();
	                }
	            }
	        });

	 
	    }

	    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                getMenuInflater().inflate(R.menu.activity_menu, menu);
                return true;
        }
        
        
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
                case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
                default:
                return super.onOptionsItemSelected(item);
            }
        }
	    
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
        	super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
	        case RESULT_SPEECH: {
	            if (resultCode == RESULT_OK && null != data) {
	 
	                ArrayList<String> text = data
	                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	 
	                editText.setText(text.get(0));
	                send.performClick();
	            }
	            break;
	        }
	        case MY_DATA_CHECK_CODE: {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // the user has the necessary data - create the TTS
                    myTTS = new TextToSpeech(this, this);
                } else {
                    // no data - install it now
                    Intent installTTSIntent = new Intent();
                    installTTSIntent
                            .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
	            break;
	        }
            }
	        
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            myTTS.shutdown();
        }
        
        public class connectTask extends AsyncTask<String,String,Network> {
	 
	        @Override
	        protected Network doInBackground(String... message) {
	 
	            //we create a TCPClient object and
	            mTcpClient = new Network(getBaseContext(), new Network.OnMessageReceived() {
	                @Override
	                //here the messageReceived method is implemented
	                public void messageReceived(String message) {
	                    //this method calls the onProgressUpdate
	                	Log.w("message", message);
	                    publishProgress(message);
	                    Log.e("TCP", message);
	                    myTTS.speak(message, TextToSpeech.QUEUE_ADD, null);
	                }
	            });
	            mTcpClient.run();
	 
	            return null;
	        }
	 
	        @Override
	        protected void onProgressUpdate(String... values) {
	            super.onProgressUpdate(values);
	 
	            //in the arrayList we add the messaged received from server
	            arrayList.add(values[0]);
	            // notify the adapter that the data set has changed. This means that new message received
	            // from server was added to the list
	            mAdapter.notifyDataSetChanged();
	        }
	    }
	}
