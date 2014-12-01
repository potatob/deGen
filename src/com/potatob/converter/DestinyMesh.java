package com.potatob.converter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by potatob on 11/20/14.
 */
public class DestinyMesh {

    public static List<DestinyMesh> parse(ByteBuffer buffer) {
        int numMeshes = buffer.getInt();
        List<DestinyMesh> meshes = new ArrayList<DestinyMesh>(numMeshes);

        for (int i = 0; i < numMeshes; i++) {
            DestinyMesh mesh = new DestinyMesh();

            mesh.vertexBuffers = VertexBuffer.parse(buffer);

            int numIndices = buffer.getInt();
            int[] indices = mesh.indices = new int[numIndices];
            for (int j = 0; j < numIndices; j++) {
                indices[j] = buffer.getShort() & 0xffff;
            }


            mesh.boundsMin = Util.readVector3D(buffer);
            mesh.boundsMax = Util.readVector3D(buffer);
            mesh.offset = Util.readVector3D(buffer);
            mesh.scale = Util.readVector3D(buffer);

            int lastIndex = 0;
            int currentIndex = 0;

            List<Integer> indexBufferData = new ArrayList<Integer>();

            int numMeshParts = buffer.getInt();
            mesh.parts = new ArrayList<Part>(numMeshParts);
            for (int j = 0; j < numMeshParts; j++) {
                Part part = Part.parseSingle(buffer);

                int triangleCount = 0;
                int start = part.indexStart;
                int count = part.indexCount;
                part.firstIndex = indexBufferData.size();
                if (part.primitive == 5){
                    for (int k = 0; k < count; k++) {
                        int index = start + k;
                        if (triangleCount == 0) { // first triangle
                            indexBufferData.add(indices[index]);
                            indexBufferData.add(indices[index + 1]);
                            indexBufferData.add(indices[index + 2]);
                            lastIndex = indices[index + 1];
                            currentIndex = indices[index + 2];
                            k += 2;
                        } else {
                            if (lastIndex == currentIndex || lastIndex == indices[index] || currentIndex == indices[index]){
                                lastIndex = currentIndex;
                                currentIndex = indices[index];
                                continue;
                            }
                            if ((triangleCount % 2) == 1){
                                indexBufferData.add(indices[index]);
                                indexBufferData.add(currentIndex);
                                indexBufferData.add(lastIndex);
                            } else {
                                indexBufferData.add(lastIndex);
                                indexBufferData.add(currentIndex);
                                indexBufferData.add(indices[index]);
                            }
                            lastIndex = currentIndex;
                            currentIndex = indices[index];
                        }
                        triangleCount++;
                    }
                } else if (part.primitive == 3){
                    for (int k = 0; k < count; k++) {
                        indexBufferData.add(indices[start + k]);
                    }
                    triangleCount = (count / 3);
                }
                part.triangleCount = triangleCount;

                mesh.parts.add(part);
            }

            mesh.indexBuffer = Util.toIntArray(indexBufferData);

            meshes.add(mesh);
        }

        return meshes;
    }

    private List<VertexBuffer> vertexBuffers;
    private int[] indexBuffer;
    private List<Part> parts;
    private Vector3D boundsMin;
    private Vector3D boundsMax;
    private Vector3D offset;
    private Vector3D scale;
    private int[] indices;
    private boolean visible;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<Part> getParts() {
        return parts;
    }

    public List<VertexBuffer> getVertexBuffers() {
        return vertexBuffers;
    }

    public int[] getIndexBuffer() {
        return indexBuffer;
    }

    public int[] getIndices() {
        return indices;
    }

    public static class Part {

        public static Part parseSingle(ByteBuffer buffer) {
            Part part = new Part();
            part.indexStart = buffer.getShort() & 0xffff;
            part.indexCount = buffer.getShort() & 0xffff;
            part.indexMin = buffer.getShort() & 0xffff;
            part.indexMax = buffer.getShort() & 0xffff;
            part.flags = buffer.getInt() & 0xffffffffL;
            part.primitive = buffer.getInt();

            int dyeIndex = buffer.getInt();
            switch (dyeIndex) {

            case 1:
                part.usePrimaryColor = false;
                break;

            case 3:
                part.usePrimaryColor = false;
            case 2:
                part.dyeIndex = 1;
                break;

            case 5:
                part.usePrimaryColor = false;
            case 4:
                part.dyeIndex = 2;
                break;

            case 6:
            case 7:
                part.dyeIndex = 3;
                break;
            }

            part.externalId = buffer.getInt() & 0xffffffffL;
            part.levelOfDetail = buffer.getInt();
            part.levelOfDetailName = Util.readUTF(buffer);

            int numTextures = buffer.getInt();
            part.textures = new String[numTextures];
            for (int i = 0; i < numTextures; i++) {
                part.textures[i] = Util.readUTF(buffer);
            }

            if (numTextures >= 5) {
                part.diffuseTexture = part.textures[0];
                part.normalTexture = part.textures[2];
                part.stackTexture = part.textures[4];
            } else if (numTextures > 0 && !part.textures[0].contains("detail")) {
                part.diffuseTexture = part.textures[0];
            }

            if ((part.flags & 32) != 0 || part.diffuseTexture != null
                    || part.normalTexture != null || part.stackTexture != null) {
                part.hasProgram = true;
            }

            return part;
        }

        private int firstIndex;
        private int indexStart;
        private int indexCount;
        private int indexMin;
        private int indexMax;
        private long flags;
        private int primitive;
        private int triangleCount;
        private int dyeIndex;
        private long externalId;
        private int levelOfDetail;
        private String levelOfDetailName;
        private boolean usePrimaryColor = true;
        private boolean hasProgram;

        private String[] textures;

        private String diffuseTexture;
        private String normalTexture;
        private String stackTexture;

        public int getFirstIndex() {
            return firstIndex;
        }

        public boolean hasProgram() {
            return hasProgram;
        }

        public int getIndexStart() {
            return indexStart;
        }

        public int getTriangleCount() {
            return triangleCount;
        }

        public int getIndexCount() {
            return indexCount;
        }

        public int getPrimitive() {
            return primitive;
        }

        public long getFlags() {
            return flags;
        }

        public int getDyeIndex() {
            return dyeIndex;
        }

        public boolean usePrimaryColor() {
            return usePrimaryColor;
        }

        public String getLodName() {
            return levelOfDetailName;
        }

        public DestinyTexture getDiffuseTexture() {
            return diffuseTexture == null ? null : DestinyTexture.getTexture(diffuseTexture);
        }

        public DestinyTexture getNormalTexture() {
            return diffuseTexture == null ? null : DestinyTexture.getTexture(normalTexture);
        }

        public DestinyTexture getStackTexture() {
            return diffuseTexture == null ? null : DestinyTexture.getTexture(stackTexture);
        }
    }
}
