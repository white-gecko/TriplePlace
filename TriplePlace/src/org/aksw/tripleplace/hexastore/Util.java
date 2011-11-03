package org.aksw.tripleplace.hexastore;

import java.nio.ByteBuffer;

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
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return buffer.getLong();
	}
	
	public static long[] unpackLongs(byte[] bytes) {
		long[] longs = new long[(bytes.length / 8)];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		for (int i = 0; i < longs.length; i++) {
			longs[i] = buffer.getLong();
		}
		return longs;
	}
}
