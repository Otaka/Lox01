package com.loxassembler;

/**
 * @author Dmitry
 */
public class Utils {

    public static byte[] shortToByteArray(short x) {
        return new byte[]{
            (byte) (x & 0xff),
            (byte) ((x >> 8) & 0xff)
        };
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
            (byte) value,
            (byte) (value >>> 8),
            (byte) (value >>> 16),
            (byte) (value >>> 24)
        };
    }

    public static <T> int indexOf(T[] arr, T key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(key)) {
                return i;
            }
        }

        return -1;
    }
}
