package org.aksw.tripleplace.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.tripleplace.Node;
import org.aksw.tripleplace.R;
import org.aksw.tripleplace.Triple;
import org.aksw.tripleplace.hexastore.Hexastore;
import org.aksw.tripleplace.hexastore.Util;

import tokyocabinet.BDB;
import tokyocabinet.BDBCUR;

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
		Button insertButton = (Button) this.findViewById(R.id.startInsert);
		Button insertSmallButton = (Button) this.findViewById(R.id.startInsertSmall);
		Button queryButton = (Button) this.findViewById(R.id.startQuery);
		Handler handler = new MyHandler(text);

		insertButton.setOnClickListener(new InsertClick(handler));
		insertSmallButton.setOnClickListener(new InsertSmallClick(handler));
		queryButton.setOnClickListener(new QueryClick(handler));
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

	private class InsertClick implements OnClickListener {

		private Handler handler;

		public InsertClick(Handler handler) {
			this.handler = handler;
		}

		public void onClick(View v) {
			String path = getFilesDir().getAbsolutePath();
			HexaBenchmarkInsert b = new HexaBenchmarkInsert(1000, path, handler);
			b.start();

			handler.sendMessage(handler.obtainMessage(1, "insert läuft ..."));
		}
	}

	private class InsertSmallClick implements OnClickListener {

		private Handler handler;

		public InsertSmallClick(Handler handler) {
			this.handler = handler;
		}

		public void onClick(View v) {
			String path = getFilesDir().getAbsolutePath();
			HexaBenchmarkInsert b = new HexaBenchmarkInsert(1, path, handler);
			b.start();

			handler.sendMessage(handler.obtainMessage(1, "insert läuft ..."));
		}
	}

	private class QueryClick implements OnClickListener {

		private Handler handler;

		public QueryClick(Handler handler) {
			this.handler = handler;
		}

		public void onClick(View v) {
			String path = getFilesDir().getAbsolutePath();
			HexaBenchmarkQuery b = new HexaBenchmarkQuery(path, handler);
			b.start();

			handler.sendMessage(handler.obtainMessage(1, "query läuft ..."));
		}
	}

	private class HexaBenchmarkQuery extends Thread {

		private Handler handler;

		public HexaBenchmarkQuery(String path, Handler handler) {
			this.handler = handler;
		}

		public void run() {
			String path = getFilesDir().getAbsolutePath();
			Hexastore hx = new Hexastore(path);

			Node s, p, o;
			Triple triple;

			long start, end;
			start = System.currentTimeMillis();

			List<Triple> result = null;
			List<Triple> result2 = null;
			try {
				s = hx.getNode("<http://0.eu>");
				//p = hx.getNode("<http://xmlns.com/foaf/0.1/0,0>");
				//p = hx.getNode("<http://xmlns.com/foaf/0.1/0,1>");
				p = hx.getNode("<http://xmlns.com/foaf/0.1/0,1>");
				o = hx.getNode("\"Name0,1,1\"");
				Log.v(TAG, "Ask for s=" + s.getNodeString() + " p=" + p.getNodeString() + " o=" + o.getNodeString());
				triple = new Triple(s, p, o);
				result = hx.query(triple);
				

				s = hx.getNode("<http://0.eu>");
				//p = hx.getNode("<http://xmlns.com/foaf/0.1/0,0>");
				p = hx.getNode("?p");
				//p = new Node(0);
				o = hx.getNode("\"Name0,1,1\"");
				Log.v(TAG, "Ask for s=" + s.getNodeString() + " p=" + p.getNodeString() + " o=" + o.getNodeString());
				triple = new Triple(s, p, o);
				result2 = hx.query(triple);
				
			} catch (IOException e) {
				Log.e(TAG, "Exception on querying Triples", e);
			} catch (Exception e) {
				Log.e(TAG, "Exception on querying Triples", e);
			}
			end = (System.currentTimeMillis() - start);
			
			if (result != null) {

				for (Triple triple2 : result) {
					Node[] nodes = triple2.getNodes();
					Log.v(TAG, "Got Triple s=" + nodes[0].getNodeString() + " p=" + nodes[1].getNodeString() + " o=" + nodes[2].getNodeString());
				}
				
				handler.sendMessage(handler.obtainMessage(1, "Query: In " + end
						+ " ms, das sind " + (end / 1000) + " s und "
						+ (end / 1000) / 60 + " min done. Got " + result.size()
						+ ""));
			} else {
				handler.sendMessage(handler.obtainMessage(1,
						"Got null on querying"));
			}
			
			if (result2 != null) {
				for (Triple triple2 : result2) {
					Node[] nodes = triple2.getNodes();
					Log.v(TAG, "Got Triple2 s=" + nodes[0].getNodeString() + " p=" + nodes[1].getNodeString() + " o=" + nodes[2].getNodeString());
				}
				
				handler.sendMessage(handler.obtainMessage(1, "Query: In " + end
						+ " ms, das sind " + (end / 1000) + " s und "
						+ (end / 1000) / 60 + " min done. Got " + result2.size()
						+ ""));
			} else {
				handler.sendMessage(handler.obtainMessage(1,
						"Got null on querying"));
			}
		}

	}

	private class HexaBenchmarkInsert extends Thread {

		private Handler handler;
		private int count;

		public HexaBenchmarkInsert(int count, String path, Handler handler) {
			this.count = count;
			this.handler = handler;
		}

		public void run() {
			String path = getFilesDir().getAbsolutePath();
			Hexastore hx = new Hexastore(path);

			Node s, p, o;
			ArrayList<Triple> tripleList = new ArrayList<Triple>();

			long start, end, nodes, triples;
			start = System.currentTimeMillis();
			try {
				for (int i = 0; i < (count); i++) {
					s = hx.getNode("<http://" + i + ".eu>");
					for (int j = 0; j < 2; j++) {
						p = hx.getNode("<http://xmlns.com/foaf/0.1/" + (i%100) + ","
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
			Log.v("Benchmark", "Status(" + nodes + ") " + tripleList.size() + " nodes done");

			try {
				// TODO Auto-generated catch block
				for (Triple triple : tripleList) {
					hx.addTriple(triple);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			end = (System.currentTimeMillis() - start);
			triples = (end - nodes);
			
			Log.v("Benchmark", "Status(" + triples + ") triples done");
			Log.v("Benchmark", "Status(" + end + ") done");

			handler.sendMessage(handler.obtainMessage(1, "Status " + count * 2
					* 2 + " Tripel in " + end + " ms hinzugefügt, das sind "
					+ (end / 1000) + " s und " + (end / 1000) / 60
					+ " min done. Nodes: " + nodes + " ms (" + (nodes / 1000)
					+ " s), Triples: " + triples + " ms (" + (triples / 1000)
					+ " s)."));
		}
	}
	
	public void lol(){


		String path = getFilesDir().getAbsolutePath();
		
	    // create the object
	    BDB bdb = new BDB();

	    // open the database

		// set comparator to 64bit int
		//bdb.setcmpfunc(BDB.CMPINT64);
		// set database to use 64bit int bucket-arrays which allows the
		// DB to get larger than 2GB
		bdb.tune(-1, -1, -1, -1, -1, BDB.TLARGE);
	    if(!bdb.open(path + "/casket.tcb", BDB.OWRITER | BDB.OCREAT)){
	      int ecode = bdb.ecode();
	      System.err.println("open error: " + BDB.errmsg(ecode));
	    }

	    byte[] key1 = Util.packLong(new long[] {-7095513297421747150L,-3846273158350191334L});
	    byte[] key2 = Util.packLong(new long[] {-7095513297421747150L,-403445358534883849L});
	    byte[] o1 = Util.packLong(-8441093956662597840L);
	    byte[] o2 = Util.packLong(-2483864365444698929L);
	    byte[] o3 = Util.packLong(-2483884365444698929L);
	    byte[] o4 = Util.packLong(-4398598216062786538L);
	    
	    
	    // store records
	    if(!bdb.putdup(key1, o1) ||
	       !bdb.putdup(key1, o2) ||
	       !bdb.putdup(key2, o3) ||
	       !bdb.putdup(key2, o4)){
	      int ecode = bdb.ecode();
	      System.err.println("put error: " + BDB.errmsg(ecode));
	    }

	    // retrieve records
	    byte[] value = bdb.get(key1);
	    if(value != null){
	      System.out.println(Util.unpackLong(value));
	    } else {
	      int ecode = bdb.ecode();
	      System.err.println("get error: " + BDB.errmsg(ecode));
	    }

	    // traverse records
	    BDBCUR cur = new BDBCUR(bdb);
	    cur.first();
	    byte[] key;
	    while((key = cur.key()) != null){
	      value = cur.val();
	      if(value != null){
	    	 long[] keys = Util.unpackLongs(key);
	        System.out.println(keys[0] + "," + keys[1] + ":" + Util.unpackLong(value));
	      }
	      cur.next();
	    }

	    key = Util.packLong(new long[] {-7095513297421747150L});
	    
		List<byte[]> keys = bdb.fwmkeys(key, -1); // -1 means
													// no limit
		Log.v(TAG, "Got " + keys.size() + " keys in range");
		for (byte[] bs : keys) {
			Util.unpackLongs(bs, true);
		}

	    // close the database
	    if(!bdb.close()){
	      int ecode = bdb.ecode();
	      System.err.println("close error: " + BDB.errmsg(ecode));
	    }

	  }

}