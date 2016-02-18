package com.example.arduinocheck;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;
import java.util.ArrayList;

import android.R.bool;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	PowerManager.WakeLock wL;   // wake-lock
	private static final String TAG = "bluetooth2";
	
	

	//Button sendData;  //While pressing this button data from edittext willbe sent from Android to Arduino
	//EditText getText;  //This edit text takes user input
	//TextView txtArduino; //This show the temperature received from arduino 24
	Handler h;        //when a data is received from bluetooth then it gets a message from ConnectedThread 

	 TextView temp,bpm;
	final int RECIEVE_MESSAGE = 1; // Status for Handler
	private BluetoothAdapter btAdapter = null;    //get the access of the bluetooth of the android phone 
	private BluetoothSocket btSocket = null;      
	private StringBuilder sb = new StringBuilder();   //stores the data sent by arduino

	private ConnectedThread mConnectedThread;       //All times run and check is there any data has come or not

	// SPP UUID service
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB"); // bluetooth
																	// connection
																	// er unique
																	// id

	// MAC-address of Bluetooth module (you must edit this line)
	//private static String address = "98:D3:31:20:04:41";
	private static String address = "98:D3:31:50:11:0D";
	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Defination wake-lock
		PowerManager pM = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WakeLock wL = pM.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"whatever");

		super.onCreate(savedInstanceState);
		wL.acquire();   //start wake-lock

		setContentView(R.layout.app);
          
		
		//new MailAsyctask().execute("asad");
		
   		//sendData = (Button) findViewById(R.id.sendbutton1);
		//txtArduino = (TextView) findViewById(R.id.txtArduino); 
		
		//getText = (EditText)findViewById(R.id.editText1);
		
		temp = (TextView) findViewById(R.id.tempapp);
		bpm = (TextView) findViewById(R.id.bpmapp);
		
		h = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case RECIEVE_MESSAGE: // if receive massage
					byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1); // create
																		// string
																		// from
																		// bytes
																		// array
					sb.append(strIncom); // append string
					int endOfLineIndex = sb.indexOf("\r\n"); // determine the
																// end-of-line
					if (endOfLineIndex > 0) { // if end-of-line,
						String sbprint = sb.substring(0, endOfLineIndex); // extract
																			// string
						sb.delete(0, sb.length()); // and clear
						
						//String str="t82*b236";
					    char[] chars=sbprint.toCharArray();   //Converting string to character array
					    Character[] characters=new Character[chars.length];
					    for (int i = 0; i < chars.length; i++) {
					        characters[i]=chars[i];
					        System.out.println(chars[i]);
					        Log.e(""+i, ""+chars[i]);
					    }
					    /*I am decodding temperature*/
					    String faketemp = "   ";
					    char[] temperature = faketemp.toCharArray();
					    //Now i am decodding the temparature value//
					    
					    for (int i = 0; i < 3; i++) {
					    	temperature[i]=characters[i+1];   
					        //System.out.println(temperature[i]);
					        Log.e(""+i, ""+temperature[i]);
					    }
					    
					    String Temperature = "";        

					    for (Character c : temperature)
					    	Temperature += c.toString();
					    temp.setText(Temperature);
					    temp.setTextColor(Color.GREEN);
					    Log.e("TEMPERATURE: ",Temperature);
					    
					    /*I am decodding bpm*/
					    String fakebpm = "   ";
					    char[] bpm = fakebpm.toCharArray();
					    //Now i am decodding the temparature value//
					    int j=0;
					    for (int i = 4; i < 7; i++) {
					    	bpm[j]=characters[i+1];   
					        //System.out.println(temperature[i]);
					        Log.e(""+j, ""+bpm[j]);
					        j++;
					    }
					    
					    String Bpm = "";

					    for (Character c : bpm)
					    	Bpm += c.toString();
					   MainActivity.this.bpm.setText(Bpm);
					   MainActivity.this.bpm.setTextColor(Color.GREEN);
					    Log.e("BPM: ",Bpm);
					    /*Converting is over*/
						
						
						
						
						
						//txtArduino.setText("temperature: " + sbprint + " degree fahrenheit "); // update TextView
					
																				

						// newly edited

					break;
				}
			};
			}
			};
		
		btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth
															// adapter
		checkBTState();
		/*
		sendData.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				String data = getText.getText().toString();
				mConnectedThread.write(data);
			}
		});
		*/


		}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
			throws IOException {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				Log.e(TAG, "Could not create Insecure RFComm Connection", e);
			}
		}
		return device.createRfcommSocketToServiceRecord(MY_UUID);
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d(TAG, "...onResume - try connect...");

		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.

		try {
			btSocket = createBluetoothSocket(device);
		} catch (IOException e) {
			errorExit("Fatal Error", "In onResume() and socket create failed: "
					+ e.getMessage() + ".");
		}

		/*
		 * try { btSocket = device.createRfcommSocketToServiceRecord(MY_UUID); }
		 * catch (IOException e) { errorExit("Fatal Error",
		 * "In onResume() and socket create failed: " + e.getMessage() + "."); }
		 */

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection. This will block until it connects.
		Log.d(TAG, "...Connecting...");
		try {
			btSocket.connect();
			Log.d(TAG, "....Connection ok...");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				errorExit("Fatal Error",
						"In onResume() and unable to close socket during connection failure"
								+ e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		Log.d(TAG, "...Create Socket...");

		mConnectedThread = new ConnectedThread(btSocket);
		mConnectedThread.start();
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.d(TAG, "...In onPause()...");

		try {
			btSocket.close();
		} catch (IOException e2) {
			errorExit("Fatal Error", "In onPause() and failed to close socket."
					+ e2.getMessage() + ".");
		}
	}

	private void checkBTState() {
		// Check for Bluetooth support and then check to make sure it is turned
		// on
		// Emulator doesn't support Bluetooth and will return null
		if (btAdapter == null) {
			errorExit("Fatal Error", "Bluetooth not support");
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "...Bluetooth ON...");
			} else {
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 1);
			}
		}
	}

	private void errorExit(String title, String message) {
		Toast.makeText(getBaseContext(), title + " - " + message,
				Toast.LENGTH_LONG).show();
		finish();
	}

	private class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[256]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer); // Get number of bytes and
														// message in "buffer"
					h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer)    
							.sendToTarget(); // Send to message queue Handler
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(String message) {
			Log.d(TAG, "...Data to send: " + message + "...");
			byte[] msgBuffer = message.getBytes();
			try {
				mmOutStream.write(msgBuffer);
			} catch (IOException e) {
				Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
			}
		}
	}
}
