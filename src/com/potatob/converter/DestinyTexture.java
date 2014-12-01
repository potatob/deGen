package com.potatob.converter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by potatob on 11/20/14.
 */
public class DestinyTexture {

    private static final Map<String, DestinyTexture> TEXTURE_MAP = new HashMap<String, DestinyTexture>();

    public static synchronized DestinyTexture getTexture(String id) {
        DestinyTexture destinyTexture = TEXTURE_MAP.get(id);
        if (destinyTexture != null) {
            return destinyTexture;
        }
        TEXTURE_MAP.put(id, destinyTexture = new DestinyTexture());
        destinyTexture.id = id;
        destinyTexture.load();
        return destinyTexture;
    }

    public static List<DestinyTexture> parse(ByteBuffer buffer) {
        int numTextures = buffer.getInt();
        List<DestinyTexture> destinyTextures = new ArrayList<DestinyTexture>(numTextures);

        for (int i = 0; i < numTextures; i++) {
            String id = Util.readUTF(buffer);
            destinyTextures.add(getTexture(id));
        }

        return destinyTextures;
    }

    private boolean evaluated;
    private boolean valid;
    private boolean loaded;

    private String id;
    private Future<byte[]> data;

    public String getId() {
        return id;
    }

    public byte[] getData() throws ExecutionException, InterruptedException {
        return data.get();
    }

    public boolean valid() {
        if (evaluated) {
            return valid;
        }
        try {
            valid = data.get() != null;
        } catch (Exception e) {
            valid = false;
        }
        evaluated = true;
        return valid;
    }

    public boolean load() {
        boolean proceed = false;
        synchronized (this) {
            if (!loaded) {
                loaded = proceed = true;
            }
        }
        if (proceed) {
            System.out.println("Loading texture with id: " + this.id);
            this.data = AsyncHttp.get("textures/mobile/" + id);
        }
        return true;
    }
}
