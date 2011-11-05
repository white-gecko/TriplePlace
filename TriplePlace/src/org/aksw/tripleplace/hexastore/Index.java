package org.aksw.tripleplace.hexastore;

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
		order = orderIn;
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
	public void addTriple(long[] nodes) throws IOException {
		// set comparator to 64bit int
		// index.setcmpfunc(BDB.CMPINT64);
		// set database to use 64bit int bucket-arrays which allows the DB
		// to
		// get larger than 2GB
		index.tune(-1, -1, -1, -1, -1, BDB.TLARGE);
		try {
			if (index.open(path, BDB.OWRITER | BDB.OCREAT)) {
				byte[] key = Util.packLong(nodes, order);

				byte[] value = {0};

				if (!index.put(key, value)) {
					throw new IOException(
							"Could not insert new triple to index. Error("
									+ index.ecode() + "): \"" + index.errmsg()
									+ "\"");
				}

			} else {
				throw new IOException("Couldn't open Index tree. Error("
						+ index.ecode() + "): \"" + index.errmsg() + "\"");
			}
		} catch (IOException e) {
			throw new IOException("Error inserting triple (s=" + nodes[0]
					+ ",p=" + nodes[1] + ",o=" + nodes[2] + ") into index ("
					+ _orderToString(order) + ")", e);
		} finally {
			index.close();
		}
	}

	public List<long[]> getTriples(long[] pathern) throws IOException {

		int fixedNum = 0;
		for (int i : order) {
			if (pathern[i] != 0) {
				fixedNum++;
			}
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(fixedNum * 8);
		for (int i : order) {
			if (pathern[i] != 0) {
				buffer.putLong(pathern[i]);
			}
		}
		byte[] keyPrefix = buffer.array();

		// set database to use 64bit int bucket-arrays which allows the
		// DB to get larger than 2GB
		index.tune(-1, -1, -1, -1, -1, BDB.TLARGE);
		if (index.open(path, BDB.OREADER)) {
			try {
				// -1 means no limit
				List<byte[]> keys = index.fwmkeys(keyPrefix, -1);

				List<long[]> result = new ArrayList<long[]>();
				for (byte[] key : keys) {
					long[] nodes = Util.unpackLongs(key, order);

					// add result to answer set
					result.add(nodes);
				}
				return result;

			} finally {
				index.close();
			}
		} else {
			throw new IOException("Couldn't open Index tree. Error("
					+ index.ecode() + "): \"" + index.errmsg() + "\"");
		}
	}

	/**
	 * WARNING! This method will return false, if the triple contains a variable
	 * node, because the index can't contain 0 nodes
	 * 
	 * @param nodes
	 * @return
	 * @throws IOException
	 *             if the index can't be opened
	 */
	public boolean hasTriple(long[] nodes) throws IOException {
		// set comparator to 64bit int
		// index.setcmpfunc(BDB.CMPINT64);
		// set database to use 64bit int bucket-arrays which allows the
		// DB to get larger than 2GB
		index.tune(-1, -1, -1, -1, -1, BDB.TLARGE);
		if (index.open(path, BDB.OREADER)) {
			try {
				byte[] key = Util.packLong(nodes, order);

				if (index.get(key) != null) {
					return true;
				} else {
					return false;
				}
			} finally {
				index.close();
			}
		} else {
			// check if file not found
			if (index.ecode() == 3) {
				return false;
			} else {
				throw new IOException(
						"Could not open index for reading. Error("
								+ index.ecode() + "): \"" + index.errmsg()
								+ "\"");
			}
		}
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
