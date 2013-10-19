package com.lisa.lisa;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import java.util.ArrayList;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.EditText;

import android.content.Intent;

import android.view.Menu;
import android.view.MenuItem;
	 
	public class MainActivity extends Activity
	{
	    private ListView mList;
	    private ArrayList<String> arrayList;
	    private MyCustomAdapter mAdapter;
	    private Network mTcpClient;
	 
	    @Override
	    public void onCreate(Bundle savedInstanceState)
	    {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	 
	        arrayList = new ArrayList<String>();
	 
	        final EditText editText = (EditText) findViewById(R.id.editText);
	        Button send = (Button)findViewById(R.id.send_button);
	 
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
	                arrayList.add("c: " + message);
	 
	                //sends the message to the server
	                if (mTcpClient != null) {
	                    mTcpClient.sendMessage(message);
	                }
	 
	                //refresh the list
	                mAdapter.notifyDataSetChanged();
	                editText.setText("");
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
