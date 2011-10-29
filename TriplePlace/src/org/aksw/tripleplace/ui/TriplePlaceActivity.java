package org.aksw.tripleplace.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.aksw.tripleplace.Node;
import org.aksw.tripleplace.R;
import org.aksw.tripleplace.Triple;
import org.aksw.tripleplace.hexastore.Hexastore;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TriplePlaceActivity extends Activity {
	private static final String TAG = "TriplePlaceActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		TextView text = (TextView) this.findViewById(R.id.hello);
		Button go = (Button) this.findViewById(R.id.start);
		Handler handler = new MyHandler(text);

		go.setOnClickListener(new GoClick(handler));
	}

	private class MyHandler extends Handler {
		private TextView text;

		public MyHandler(TextView text) {
			this.text = text;
		}

		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				String textString = (String) msg.obj;
				text.setText(textString);
				Log.v(TAG, "Got Message");
			} else {
				Log.v(TAG, "Got different Message");
			}
		}
	}

	private class GoClick implements OnClickListener {

		private Handler handler;

		public GoClick(Handler handler) {
			this.handler = handler;
		}

		public void onClick(View v) {
			String path = getFilesDir().getAbsolutePath();
			HexaBenchmark b = new HexaBenchmark(path, handler);
			b.start();

			handler.sendMessage(handler.obtainMessage(1, "läuft ..."));
		}
	}

	private class HexaBenchmark extends Thread {

		private Handler handler;

		public HexaBenchmark(String path, Handler handler) {
			this.handler = handler;
		}

		public void run() {
			String path = getFilesDir().getAbsolutePath();
			Hexastore hx = new Hexastore(path);

			Node s, p, o;
			Triple triple;
			ArrayList<Triple> tripleList = new ArrayList<Triple>();

			long start, end, nodes, triples;
			start = System.currentTimeMillis();
			try {
				for (int i = 0; i < 1000; i++) {
					s = hx.getNode("<http://" + i + ".eu>");
					for (int j = 0; j < 2; j++) {
						p = hx.getNode("<http://xmlns.com/foaf/0.1/" + i + ","
								+ j + ">");
						for (int k = 0; k < 2; k++) {
							o = hx.getNode("\"Name" + i + "," + j + "," + k
									+ "\"");
							tripleList.add(new Triple(s, p, o));
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Couldn't create new Node", e);
			}

			nodes = (System.currentTimeMillis() - start);
			Log.v("Benchmark", "Status(" + nodes + ") nodes done");

			try {
				// TODO Auto-generated catch block
				for (Triple triple2 : tripleList) {
					hx.addTriple(triple2);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			end = (System.currentTimeMillis() - start);
			triples = (end - nodes);
			Log.v("Benchmark", "Status(" + triples + ") triples done");
			Log.v("Benchmark", "Status(" + end + ") done");

			handler.sendMessage(handler.obtainMessage(1, "Status " + 1000 * 2
					* 2 + " Tripel in " + end + " ms hinzugefügt, das sind "
					+ (end / 1000) + " s und " + (end / 1000) / 60
					+ " min done. Nodes: " + nodes + " ms (" + (nodes/1000) + " s), Triples: " + triples + " ms (" + (triples/1000) + " s)."));
		}
	}

}