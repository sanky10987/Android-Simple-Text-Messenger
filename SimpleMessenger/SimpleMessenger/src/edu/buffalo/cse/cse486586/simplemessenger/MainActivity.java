/*
 * Author : Sanket Kulkarni
 * UBID : 3749-7801
 * Email : sanketku@buffalo.edu 
 * Project Name : SimpleMessenger, Assignment 1 - Distributed Systems CSE 586.
 */
package edu.buffalo.cse.cse486586.simplemessenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * The Class MainActivity.
 */
public class MainActivity extends Activity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TelephonyManager tel = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		final String portStr = tel.getLine1Number().substring(
				tel.getLine1Number().length() - 4);

		try {
			ServerSocket serverSock = new ServerSocket(10000);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					serverSock);
		} catch (IOException e) {
			Log.v("ServerError", "Error during Socket Creation");
		}

		Button button = (Button) findViewById(R.id.button1);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText editText = (EditText) findViewById(R.id.editText1);
				String send = editText.getText().toString();
				if (!send.isEmpty()) {
					TextView textView = (TextView) findViewById(R.id.textView1);
					textView.setMovementMethod(new ScrollingMovementMethod());
					textView.append("Sent : ");
					textView.append(send);
					textView.append("\n");
					new ClientTask().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, send, portStr);
				}
				editText.setText("");
			}
		});
		final EditText editText = (EditText) findViewById(R.id.editText1);
		editText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if ((arg2.getAction() == KeyEvent.ACTION_DOWN)
						&& (arg1 == KeyEvent.KEYCODE_ENTER)) {
					String send = editText.getText().toString();
					if (!send.isEmpty() || send.length() < 128) {
						TextView textView = (TextView) findViewById(R.id.textView1);
						textView.setMovementMethod(new ScrollingMovementMethod());
						textView.append("Sent : ");
						textView.append(send.trim());
						textView.append("\n");
						new ClientTask().executeOnExecutor(
								AsyncTask.THREAD_POOL_EXECUTOR, send, portStr);
					}
					editText.setText("");
				}
				return false;
			}
		});
	}

	/**
	 * The Class ServerTask.
	 */
	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(ServerSocket... params) {
			ServerSocket sock = params[0];
			Socket socket;
			try {
				while (true) {
					// accept the client socket
					// read the stream
					socket = sock.accept();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					char[] out = new char[128];
					br.read(out);
					publishProgress(new String(out));
					socket.close();
				}
			} catch (IOException e) {
				Log.v("Error", "Error during reading from socket");
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(String... values) {
			// this is run in Activity thread not in new thread
			// This sets the value in textview
			TextView textView = (TextView) findViewById(R.id.textView1);
			textView.setMovementMethod(new ScrollingMovementMethod());
			textView.append("Received: ");
			textView.append(values[0].trim());
			textView.append("\n");
			super.onProgressUpdate(values);
		}

	}

	/**
	 * The Class ClientTask.
	 */
	private class ClientTask extends AsyncTask<String, String, Void> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(String... params) {
			Socket socket = null;
			try {
				// avd port check is done
				// and message is sent on the client socket
				if (params[1].equals("5554"))
					socket = new Socket(InetAddress.getByName("10.0.2.2"),
							11112);
				else if (params[1].equals("5556"))
					socket = new Socket(InetAddress.getByName("10.0.2.2"),
							11108);
				socket.getOutputStream().write(params[0].getBytes());
			} catch (IOException e) {
				Log.v("Error", "Error while sending the message");
			}
			return null;
		}
	}
}
