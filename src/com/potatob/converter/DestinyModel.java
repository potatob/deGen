package com.potatob.converter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * Created by potatob on 11/20/14.
 */
public class DestinyModel {

    private final String id;

    private boolean loaded;

    private List<DestinyTexture> textures = null;
    private List<DestinyGeometry> geometry = null;

    private int[] maleGeometry;
    private int[] maleTextures;
    private int[] femaleGeometry;
    private int[] femaleTextures;
    private int[] dyeGeometry;
    private int[] dyeTextures;
    private boolean dyeLocked;
    private boolean genderMale;
    private JsonArray dyeInfo;

    public DestinyModel(String id) {
        this(id, true);
    }

    public DestinyModel(String id, boolean genderMale) {
        this.id = id;
        this.genderMale = genderMale;
    }

    public String getId() {
        return id;
    }

    public List<DestinyGeometry> getGeometry() {
        return geometry;
    }

    public List<DestinyTexture> getTextures() {
        return textures;
    }

    public boolean dyeLocked() {
        return dyeLocked;
    }

    public JsonArray getDyeInfo() {
        return dyeInfo;
    }

    // Fully parses a model's components
    private void parse(byte[] data) {
        // The file uses little endian format:
        long magicNumber = ((data[3] & 0xffL) << 24)
                          | ((data[2] & 0xffL) << 16)
                          | ((data[1] & 0xffL) << 8)
                          | (data[0] & 0xffL);
        // There is a magic number at the top of their geom files
        // it's basically a version number
        if (magicNumber != 572728357L) {
            System.err.println("Invalid magic number: " + magicNumber);
            // do nothing different for now
            
            // return;
        }

        try {
            // The next part is compressed with ZLIB
            data = Util.inflate(data, 4, data.length - 4);
        } catch (DataFormatException e) {
            e.printStackTrace();
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        this.textures = DestinyTexture.parse(buffer);
        this.geometry = DestinyGeometry.parse(buffer);

        int numMaleGeometry = buffer.getInt();
        this.maleGeometry = new int[numMaleGeometry];
        for (int i = 0; i < numMaleGeometry; i++) {
            this.maleGeometry[i] = buffer.getInt();
        }

        int numMaleTextures = buffer.getInt();
        this.maleTextures = new int[numMaleTextures];
        for (int i = 0; i < numMaleTextures; i++) {
            this.maleTextures[i] = buffer.getInt();
        }

        int numFemaleGeometry = buffer.getInt();
        this.femaleGeometry = new int[numFemaleGeometry];
        for (int i = 0; i < numFemaleGeometry; i++) {
            this.femaleGeometry[i] = buffer.getInt();
        }

        int numFemaleTextures = buffer.getInt();
        this.femaleTextures = new int[numFemaleTextures];
        for (int i = 0; i < numFemaleTextures; i++) {
            this.femaleTextures[i] = buffer.getInt();
        }

        int numDyeGeometry = buffer.getInt();
        this.dyeGeometry = new int[numDyeGeometry];
        for (int i = 0; i < numDyeGeometry; i++) {
            this.dyeGeometry[i] = buffer.getInt();
        }

        int numDyeTextures = buffer.getInt();
        this.dyeTextures = new int[numDyeTextures];
        for (int i = 0; i < numDyeTextures; i++) {
            this.dyeTextures[i] = buffer.getInt();
        }

        String gearJson = Util.readUTFInt(buffer);
        JsonObject gear = (JsonObject) new JsonParser().parse(gearJson);

        JsonArray lockedDyes = gear.getAsJsonArray("locked_dyes");

        if (lockedDyes != null && lockedDyes.size() != 0) {
            this.dyeLocked = true;
            this.dyeInfo = lockedDyes;
        } else {
            JsonArray customDyes = gear.getAsJsonArray("custom_dyes");

            if (customDyes != null && customDyes.size() != 0) {
                this.dyeInfo = customDyes;
            } else {
                this.dyeInfo = gear.getAsJsonArray("default_dyes");
            }
        }

        // If male and female use the same model, set all geometry to visible
        if (this.geometry.size() != 0 && numMaleGeometry == 0 && numFemaleGeometry == 0) {
            for (DestinyGeometry destinyGeometry : this.geometry) {
                destinyGeometry.setVisible(true);
            }
        } else if (genderMale) { // male model
            for (int i = 0; i < numMaleGeometry; i++) {
                this.geometry.get(this.maleGeometry[i]).setVisible(true);
            }
        } else { // female model
            for (int i = 0; i < numFemaleGeometry; i++) {
                this.geometry.get(this.femaleGeometry[i]).setVisible(true);
            }
        }

        System.out.println(buffer.remaining());
    }

    // Retrieve root model file from server
    public boolean load() {
        boolean proceed = false;
        synchronized (this) {
            if (!loaded) {
                loaded = proceed = true;
            }
        }
        if (proceed) {
            System.out.println("Loading model with id: " + this.id);
            byte[] data = null;
            try {
                data = AsyncHttp.get("geom/mobile/" + this.id + ".geom").get();
                if (data == null) {
                    throw new Exception("Failed to get response from server.");
                }
            } catch (Exception e) {
                System.err.println("Failed to load model geometry: " + this.id);
                e.printStackTrace();
                return false;
            }
            parse(data);
        }
        return true;
    }
}
