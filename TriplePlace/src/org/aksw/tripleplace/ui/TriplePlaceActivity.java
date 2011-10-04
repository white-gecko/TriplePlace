package org.aksw.tripleplace.ui;

import org.aksw.tripleplace.Node;
import org.aksw.tripleplace.R;
import org.aksw.tripleplace.Triple;
import org.aksw.tripleplace.hexastore.Hexastore;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SlidingDrawer;
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
		go.setOnClickListener(new GoClick(text));
	}
	
	private class GoClick implements OnClickListener {
		
		private TextView text; 
		
		public GoClick (TextView text) {
			this.text = text;
		}
		
		public void onClick(View v) {
			long end = test4k();
			text.setText("Status " + 1000*2*2 + " Tripel in " + end + " ms hinzugefügt, das sind " + (end/1000) + " s und " + (end/1000)/60 + " min done");
		}
	}

	private long test4k() {
		String path = getFilesDir().getAbsolutePath();
		Hexastore hx = new Hexastore(path);

		Node s, p, o;
		Triple triple;

		long start, end;
		start = System.currentTimeMillis();
		try {
			for (int i = 0; i < 1000; i++) {
				s = new Node("<http://" + i + ".eu>");
				for (int j = 0; j < 2; j++) {
					p = new Node("<http://xmlns.com/foaf/0.1/" + i + "," + j
							+ ">");
					for (int k = 0; k < 2; k++) {
						o = new Node("\"Name" + i + "," + j + "," + k + "\"");
						triple = new Triple(s, p, o);
						hx.addTriple(triple);
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Couldn't create new Node", e);
		}
		end = (System.currentTimeMillis() - start);
		Log.v("Benchmark", "Status(" + end + ") done");

		return end;
	}
}