package org.aksw.tripleplace.hexastore;

import android.util.Log;

public class Util {
	
	public static byte[] packLong(long[] number) {
		byte[] data = new byte[8*number.length];

		for (int i = 0; i < number.length; i++) {
			for (int j = 0; j < 8; j++) {
				data[(i*8) + (7 - j)] = (byte) (number[i] >>> (j * 8));
			}
		}
		return data;
	}	
	
	public static byte[] packLong(long[] number, int[] order) {
		byte[] data = new byte[8*number.length];

		for (int i = 0; i < number.length; i++) {
			for (int j = 0; j < 8; j++) {
				data[(i*8) + (7 - j)] = (byte) (number[order[i]] >>> (j * 8));
			}
		}
		return data;
	}

	public static byte[] packLong(long number) {
		byte[] data = new byte[8];
		for (int i = 0; i < data.length; i++) {
			data[7 - i] = (byte) (number >>> (i * 8));
		}
		return data;
	}

	public static long unpackLong(byte[] data) {
		return unpackLong(data, false);
	}

	public static long unpackLong(byte[] data, boolean verbous) {
		long l = 0;
		for (int i = 0; i < data.length; i++) {
			l = (l << 8) + (data[i] & 0xff);
		}
		if (verbous) {
			Log.v("Util", "long(" + data.length + "): " + l);
		}

		return l;
	}

	public static long[] unpackLongs(byte[] data) {
		return unpackLongs(data, false);
	}

	public static long[] unpackLongs(byte[] data, int[] order) {
		long[] longs = new long[(data.length / 8)];

		for (int i = 0; i < longs.length; i++) {
			long l = 0;
			for (int j = 0; j < 8; j++) {
				l = (l << 8) + (data[(i * 8) + j] & 0xff);
			}
			longs[order[i]] = l;
		}
		return longs;
	}

	public static long[] unpackLongs(byte[] data, boolean verbous) {
		long[] longs = new long[(data.length / 8)];

		for (int i = 0; i < longs.length; i++) {
			long l = 0;
			for (int j = 0; j < 8; j++) {
				l = (l << 8) + (data[(i * 8) + j] & 0xff);
			}
			longs[i] = l;
			if (verbous) {
				Log.v("Util", "long(" + data.length + "): " + l);
			}
		}
		return longs;
	}
}
