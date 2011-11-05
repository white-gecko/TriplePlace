package org.aksw.tripleplace.hexastore;

import java.nio.ByteBuffer;

import android.util.Log;

public class Util {
	public static byte[] packLong(long[] numbers) {
		ByteBuffer buffer = ByteBuffer.allocate(numbers.length * 8);
		for (long number : numbers) {
			buffer.putLong(number);
		}
		return buffer.array();
	}

	public static byte[] packLong(long[] numbers, int[] order) {
		ByteBuffer buffer = ByteBuffer.allocate(numbers.length * 8);
		for (int i : order) {
			buffer.putLong(numbers[i]);
		}
		return buffer.array();
	}
	
	public static byte[] packLong(long number) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(number);
		return buffer.array();
	}
	
	public static long unpackLong(byte[] bytes) {
		return unpackLong(bytes, false);
	}
	
	public static long unpackLong(byte[] bytes, boolean verbous) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		if (verbous) {
			long l = buffer.getLong();
			Log.v("Util", "long(" + bytes.length + "): " + l);
			return l;
		}
		return buffer.getLong();
	}

	public static long[] unpackLongs(byte[] bytes) {
		return unpackLongs(bytes, false);
	}

	public static long[] unpackLongs(byte[] bytes, int[] order) {
		long[] longs = new long[(bytes.length / 8)];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		for (int i : order) {
			longs[i] = buffer.getLong();
		}
		return longs;
	}
	
	public static long[] unpackLongs(byte[] bytes, boolean verbous) {
		long[] longs = new long[(bytes.length / 8)];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		for (int i = 0; i < longs.length; i++) {
			longs[i] = buffer.getLong();
			if (verbous) {
				Log.v("Util", "long(" + i + "," + bytes.length + "): " + longs[i]);
			}
		}
		return longs;
	}
}
