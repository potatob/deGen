package com.potatob.converter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by potatob on 11/20/14.
 */
public class DestinyGeometry {

    public static List<DestinyGeometry> parse(ByteBuffer buffer) {
        List<DestinyGeometry> geometry = new ArrayList<DestinyGeometry>();

        int numGeometry = buffer.getInt();

        for (int i = 0; i < numGeometry; i++) {
            DestinyGeometry geom = new DestinyGeometry();

            geom.name = Util.readUTF(buffer);
            geom.meshes = DestinyMesh.parse(buffer);
            geom.texturePlates = TexturePlate.parse(buffer);

            if (geom.texturePlates.size() > 0) {
                geom.diffuseTexture = geom.texturePlates.get(0).getTexture();
                geom.normalTexture = geom.texturePlates.get(1).getTexture();
                geom.stackTexture = geom.texturePlates.get(2).getTexture();
            }

            geometry.add(geom);
        }

        return geometry;
    }

    private String name;
    private List<DestinyMesh> meshes;
    private List<TexturePlate> texturePlates;
    private DestinyTexture diffuseTexture;
    private DestinyTexture normalTexture;
    private DestinyTexture stackTexture;

    public String getName() {
        return name;
    }

    public List<DestinyMesh> getMeshes() {
        return meshes;
    }

    public List<TexturePlate> getTexturePlates() {
        return texturePlates;
    }

    public DestinyTexture getDiffuseTexture() {
        return diffuseTexture;
    }

    public DestinyTexture getNormalTexture() {
        return normalTexture;
    }

    public DestinyTexture getStackTexture() {
        return stackTexture;
    }

    public void setVisible(boolean visible) {
        for (DestinyMesh destinyMesh : meshes) {
            destinyMesh.setVisible(visible);
        }
    }
}
