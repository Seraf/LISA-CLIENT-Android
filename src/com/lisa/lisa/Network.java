package com.lisa.lisa;

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Network {
    private String serverMessage;
    private Context mycontext;
    public static String SERVERIP = "demo.lisa-project.net"; //your computer IP address
    public static int SERVERPORT = 10042;
    public static String ZONE = "Android";
    
    public static String getZONE() {
		return ZONE;
	}

	public static void setZONE(String zone) {
		ZONE = zone;
	}

	private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
 
    PrintWriter out;
    BufferedReader in;
 
    
    public static String getServerip() {
		return SERVERIP;
	}


	public static void setServerip(String serverip) {
		SERVERIP = serverip;
	}


	public static int getServerport() {
		return SERVERPORT;
	}


	public static void setServerport(int serverport) {
		SERVERPORT = serverport;
	}


	/**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public Network(Context context, OnMessageReceived listener) {
    	mycontext = context;
        mMessageListener = listener;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Network.setServerport(Integer.valueOf(sharedPrefs.getString("pref_portLabel", "10042")));
        Network.setServerip(sharedPrefs.getString("pref_ipLabel", "demo.lisa-project.net"));
        Network.setZONE(sharedPrefs.getString("pref_zoneLabel", "Android"));
    }
    
    
    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
		JSONObject commandJson = new JSONObject();

		try {
			commandJson.put("type", "chat");
			commandJson.put("body", message);
			commandJson.put("from", "Android");
			commandJson.put("zone", Network.getZONE());
			
			if (out != null && !out.checkError()) {
	            out.print(commandJson.toString() + "\r\n");
	            out.flush();
	        }
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
    }

    /**
     * Parse the message sent by the server
     * @param res json received by the server
     */
	private String parseAnswer(String res) {

		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(res);

			String tmp = jsonObj.getString("body");
			Log.w("REPONSE BODY :", tmp);
			return tmp;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new String("There was an error decoding the json");
		}
	}

    
    
    public void stopClient(){
        mRun = false;
    }
 
    public void run() {
 
        mRun = true;
 
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
 
            Log.e("TCP Client", "Client Connecting...");
 
            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVERPORT);

            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
 
                Log.e("TCP Client", "Message Sent.");
 
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();
 
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                    	String parsedmessage = parseAnswer(serverMessage);
                        mMessageListener.messageReceived(parsedmessage);
                    }
                    serverMessage = null;
                }
                Log.e("RESPONSE FROM SERVER", "Received Message: '" + serverMessage + "'");
            } catch (Exception e) {
                Log.e("TCP", "Server Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "Client Error", e);
        }
    }
 
    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
 }
