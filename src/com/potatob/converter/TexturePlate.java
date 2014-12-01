package com.potatob.converter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by potatob on 11/21/14.
 */
public class TexturePlate {

    public static List<TexturePlate> parse(ByteBuffer buffer) {
        List<TexturePlate> texturePlates = new ArrayList<TexturePlate>();

        int numTexturePlates = buffer.getInt();

        for (int i = 0; i < numTexturePlates; i++) {
            TexturePlate texturePlate = new TexturePlate();

            texturePlate.name = Util.readUTF(buffer);
            texturePlate.id = Util.readUTF(buffer);
            texturePlate.index = buffer.getInt();
            texturePlate.width = buffer.getShort();
            texturePlate.height = buffer.getShort();
            texturePlate.parts = Part.parse(buffer);

            if (texturePlate.name.equals("diffuse")
                    || texturePlate.name.equals("gearstack")
                    || texturePlate.name.equals("normal")) {
                texturePlate.texture = DestinyTexture.getTexture(texturePlate.id + ".png");
            }

            texturePlates.add(texturePlate);
        }

        return texturePlates;
    }

    private String name;
    private String id;
    private int index;
    private int width;
    private int height;
    private List<Part> parts;
    private DestinyTexture texture;

    public DestinyTexture getTexture() {
        return texture;
    }

    public static class Part {

        public static List<Part> parse(ByteBuffer buffer) {
            int numTexturePlateParts = buffer.getInt();
            List<Part> texturePlateParts = new ArrayList<Part>(numTexturePlateParts);
            
            for (int i = 0; i < numTexturePlateParts; i++) {
                Part texturePlatePart = new Part();
                texturePlatePart.name = Util.readUTF(buffer);
                texturePlatePart.index = buffer.getInt();
                texturePlatePart.width = buffer.getShort();
                texturePlatePart.height = buffer.getShort();
                texturePlatePart.x = buffer.getShort();
                texturePlatePart.y = buffer.getShort();
                texturePlateParts.add(texturePlatePart);
            }
            
            return texturePlateParts;
        }
        
        public String name;
        public int index;
        public int width;
        public int height;
        public int x;
        public int y;
    }
}
