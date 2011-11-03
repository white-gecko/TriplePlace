package org.aksw.tripleplace.hexastore;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import tokyocabinet.BDB;
import android.util.Log;

/**
 * A triple index. Can be spo, sop, pso, pos, osp or ops
 * 
 * @author natanael
 * 
 */
public class Index {
	private String TAG = "Index";

	public static final int[] ORDER_SPO = { 0, 1, 2 };
	public static final int[] ORDER_SOP = { 0, 2, 1 };
	public static final int[] ORDER_PSO = { 1, 0, 2 };
	public static final int[] ORDER_POS = { 1, 2, 0 };
	public static final int[] ORDER_OSP = { 2, 0, 1 };
	public static final int[] ORDER_OPS = { 2, 1, 0 };

	/**
	 * One of ORDER_SPO, ORDER_SOP, ...
	 */
	private int[] order;

	private BDB index;
	private String path;

	public Index(int[] orderIn, String pathIn) {
		order = orderIn.clone();
		path = pathIn;
		TAG += "(" + _orderToString(order) + ")";
		index = new BDB();
		Log.v(TAG, "Initialized Index (" + _orderToString(order) + ") at \""
				+ path + "\"");
	}

	/**
	 * The function ignorres if the same triple already exists
	 * 
	 * @param nodes
	 */
	public void addTriple(long[] nodes) {
		// set comparator to 64bit int
		index.setcmpfunc(BDB.CMPINT64);
		// set database to use 64bit int bucket-arrays which allows the DB to
		// get larger than 2GB
		index.tune(-1, -1, -1, -1, -1, BDB.TLARGE);
		if (!index.open(path, BDB.OWRITER | BDB.OCREAT)) {
			Log.e(TAG, "Couldn't open Index tree. Error(" + index.ecode()
					+ "): \"" + index.errmsg() + "\"");
		}
		try {

			ByteBuffer buffer = ByteBuffer.allocate(16);

			// Log.v(TAG, "Adding Triple to index");

			buffer.putLong(nodes[order[0]]);
			buffer.putLong(nodes[order[1]]);
			byte[] key = buffer.array();
			
			ByteBuffer buffer2 = ByteBuffer.allocate(8);
			
			// write terminalelement
			buffer2.putLong(nodes[order[2]]);
			byte[] value = buffer2.array();

			// index.put(key, new byte[] {0});
			// maybe check first if this value already exists to get no
			// duplicates in the list
			// but maybe this is solved if we put the result of getlist in a
			// kind of set or so
			if (!index.putdup(key, value)) {
				throw new IOException("Could not insert nut triple to index. Error("
						+ index.ecode() + "): \""
						+ index.errmsg() + "\"");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error inserting triple (s=" + nodes[0] + ",p="
					+ nodes[1] + ",o=" + nodes[2] + ") into index ("
					+ _orderToString(order) + ")", e);
		} finally {
			index.close();
		}
	}

	public List<long[]> getTriples(long[] pathern) throws IOException {
		if (pathern[order[2]] == 0) {
			if (pathern[order[0]] != 0 && pathern[order[1]] != 0) {
				// set comparator to 64bit int
				index.setcmpfunc(BDB.CMPINT64);
				// set database to use 64bit int bucket-arrays which allows the
				// DB to get larger than 2GB
				index.tune(-1, -1, -1, -1, -1, BDB.TLARGE);
				if (index.open(path, BDB.OREADER)) {
					try {
						ByteBuffer buffer = ByteBuffer.allocate(16);

						for (int i = 0; i < 2; i++) {
							buffer.putLong(pathern[order[i]]);
						}
						byte[] key = buffer.array();

						List<byte[]> values = index.getlist(key);

						if (values != null) {
							List<long[]> result = new ArrayList<long[]>();
							for (byte[] binding : values) {
								ByteBuffer buffer2 = ByteBuffer.allocate(8);
								buffer2 = ByteBuffer.wrap(binding);

								// copy exisiting pathern
								long[] nodes = pathern.clone();

								// replace variable with result
								nodes[order[2]] = buffer2.getLong();

								// add result to answer set
								result.add(nodes);
							}
							return result;
						} else {
							throw new IOException(
									"Got null result on getting list from index. Error("
											+ index.ecode() + "): \""
											+ index.errmsg() + "\"");
						}

					} finally {
						index.close();
					}
				}
			}
		}
		return null;
	}

	private static String _orderToString(int[] order) {
		String out = "";
		for (int i = 0; i < order.length; i++) {
			switch (order[i]) {
			case 0:
				out += "s";
				continue;
			case 1:
				out += "p";
				continue;
			case 2:
				out += "o";
				continue;
			default:
				out += "(error)";
			}
		}
		return out;
	}
}
