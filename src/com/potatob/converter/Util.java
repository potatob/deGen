package com.potatob.converter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by potatob on 11/20/14.
 */
public class Util {

    public static int[] toIntArray(List<Integer> list)  {
        int[] ret = new int[list.size()];
        if (list instanceof ArrayList) {
            for (int i = 0; i < ret.length; i++) {
                ret[i] = list.get(i);
            }
        } else {
            int i = 0;
            for (Integer e : list) {
                ret[i++] = e;
            }
        }
        return ret;
    }

    public static Vector3D readVector3D(ByteBuffer buffer) {
        return new Vector3D(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
    }

    // mimics Flash's ByteArray.readUTF
    public static String readUTF(ByteBuffer buffer) {
        int len = buffer.getShort() & 0xffff;
        byte[] data = new byte[len];
        buffer.get(data);
        return new String(data, Charset.forName("UTF-8"));
    }

    public static String readUTFInt(ByteBuffer buffer) {
        int len = buffer.getInt();
        byte[] data = new byte[len];
        buffer.get(data);
        return new String(data, Charset.forName("UTF-8"));
    }

    // ZLIB inflater
    public static byte[] inflate(byte[] data, int off, int len) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data, off, len);
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            out.write(buffer, 0, count);
        }
        byte[] output = out.toByteArray();
        inflater.end();
        return output;
    }

    public static String properName(String name) {
        return name.toLowerCase().replaceAll("[^a-zA-Z0-9 \\.]", " ").trim().replaceAll(" ", "_");
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
