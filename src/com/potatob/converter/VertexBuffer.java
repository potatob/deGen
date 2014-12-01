package com.potatob.converter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by potatob on 11/21/14.
 */
public class VertexBuffer {

    public static final int POSITION = 1;
    public static final int MAX_POSITION = 3;
    public static final int TEX_COORD = 4;
    public static final int MAX_TEX_COORD = 11;
    public static final int NORMAL = 12;
    public static final int MAX_NORMAL = 18;
    public static final int TANGENT = 19;
    public static final int MAX_TANGENT = 25;
    public static final int COLOR = 26;
    public static final int MAX_COLOR = 32;
    public static final int BLEND_WEIGHT = 33;
    public static final int MAX_BLEND_WEIGHT = 39;
    public static final int BLEND_INDEX = 40;
    public static final int MAX_BLEND_INDEX = 47;

    public static List<VertexBuffer> parse(ByteBuffer buffer) {
        List<VertexBuffer> vertexBuffers = new ArrayList<VertexBuffer>();

        int numVertexBuffers = buffer.getInt();

        for (int i = 0; i < numVertexBuffers; i++) {
            VertexBuffer vertexBuffer = new VertexBuffer();

            vertexBuffer.count = buffer.getInt();

            // unused
            /*int stride = */buffer.getInt();

            int elementCount = buffer.getInt();

            vertexBuffer.elements = new Element[elementCount];

            int slot = 0;
            int offset = 0;

            for (int j = 0; j < elementCount; j++) {
                Element element = new Element();
                element.type = buffer.get();
                element.slot = slot;
                element.offset = offset;

                int startOffset = offset;

                if (element.type >= POSITION && element.type <= MAX_POSITION) {
                    element.baseType = POSITION;
                    element.format = "float3";
                    offset += 3;
                    vertexBuffer.positionIndex = j;
                    vertexBuffer.positions = new Vector3D[vertexBuffer.count];
                } else if (element.type >= TEX_COORD && element.type <= MAX_TEX_COORD) {
                    element.baseType = TEX_COORD;
                    element.format = "float2";
                    offset += 2;
                    vertexBuffer.texCoordIndex = j;
                    vertexBuffer.texCoords = new Vector2D[vertexBuffer.count];
                } else if (element.type >= NORMAL && element.type <= MAX_NORMAL) {
                    element.baseType = NORMAL;
                    element.format = "float4";
                    offset += 4;
                    vertexBuffer.normalsIndex = j;
                    vertexBuffer.normals = new Vector3D[vertexBuffer.count];
                } else if (element.type >= TANGENT && element.type <= MAX_TANGENT) {
                    element.baseType = TANGENT;
                    element.format = "float4";
                    offset += 4;
                    vertexBuffer.tangents = new Vector3D[vertexBuffer.count];
                } else if (element.type >= COLOR && element.type <= MAX_COLOR) {
                    element.baseType = COLOR;
                    element.format = "float4";
                    offset += 4;
                } else if (element.type >= BLEND_WEIGHT && element.type <= MAX_BLEND_WEIGHT) {
                    element.baseType = BLEND_WEIGHT;
                    vertexBuffer.blendWeights = new int[vertexBuffer.count][];
                } else if (element.type >= BLEND_INDEX && element.type <= MAX_BLEND_INDEX) {
                    element.baseType = BLEND_INDEX;
                    vertexBuffer.blendIndices = new int[vertexBuffer.count][];
                }
                if (startOffset != offset) {
                    slot++;
                }
                vertexBuffer.elements[j] = element;
            }

            vertexBuffer.stride = offset; // We actually calculate the stride ourselves

            vertexBuffer.slots = slot; // This is the number of different elements
            // (not including blend weight and blend indices)

            vertexBuffer.data = new float[vertexBuffer.count * vertexBuffer.stride];

            int pos = 0;

            for (int j = 0; j < vertexBuffer.count; j++) {
                for (int k = 0; k < elementCount; k++) {
                    Element element = vertexBuffer.elements[k];
                    switch (element.baseType) {

                        case POSITION: {
                            float x = vertexBuffer.data[pos++] = buffer.getFloat();
                            float y = vertexBuffer.data[pos++] = buffer.getFloat();
                            float z = vertexBuffer.data[pos++] = buffer.getFloat();
                            float w = buffer.getFloat();
                            vertexBuffer.positions[j] = new Vector3D(x, y, z, w);
                            break;
                        }

                        case TANGENT:
                        case NORMAL: {
                            float x = vertexBuffer.data[pos++] = buffer.getFloat();
                            float y = vertexBuffer.data[pos++] = buffer.getFloat();
                            float z = vertexBuffer.data[pos++] = buffer.getFloat();
                            float w = vertexBuffer.data[pos++] = buffer.getFloat();

                            if (element.baseType == TANGENT) {
                                vertexBuffer.tangents[j] = new Vector3D(x, y, z, w);
                            } else {
                                vertexBuffer.normals[j] = new Vector3D(x, y, z, w);
                            }
                            break;
                        }

                        case BLEND_WEIGHT: {
                            vertexBuffer.blendWeights[j] = new int[] {
                                buffer.get() & 0xff, buffer.get() & 0xff, buffer.get() & 0xff, buffer.get() & 0xff
                            };
                            break;
                        }

                        case BLEND_INDEX: {
                            vertexBuffer.blendIndices[j] = new int[] {
                                buffer.get() & 0xff, buffer.get() & 0xff, buffer.get() & 0xff, buffer.get() & 0xff
                            };
                            break;
                        }

                        case TEX_COORD: {
                            float u = vertexBuffer.data[pos++] = buffer.getFloat();
                            float v = vertexBuffer.data[pos++] = buffer.getFloat();
                            vertexBuffer.texCoords[j] = new Vector2D(u, v);
                            break;
                        }

                        case COLOR: {
                            //rgba? argb? other?
                            vertexBuffer.data[pos++] = (buffer.get() & 0xff) / 255f;
                            vertexBuffer.data[pos++] = (buffer.get() & 0xff) / 255f;
                            vertexBuffer.data[pos++] = (buffer.get() & 0xff) / 255f;
                            vertexBuffer.data[pos++] = (buffer.get() & 0xff) / 255f;
                            break;
                        }
                    }
                }
            }

            vertexBuffers.add(vertexBuffer);
        }

        return vertexBuffers;
    }

    private int count;
    private int stride;
    private int slots;
    private int positionIndex;
    private int normalsIndex;
    private int texCoordIndex;

    private float[] data; // actual vertex buffer data
    private Element[] elements;

    private Vector3D[] positions;
    private Vector3D[] normals;
    private Vector3D[] tangents;
    private Vector2D[] texCoords;

    private int[][] blendWeights;
    private int[][] blendIndices;

    public float[] getBuffer() {
        return data;
    }

    public int getStride() {
        return stride;
    }

    public int getCount() {
        return count;
    }

    public int getElementCount() {
        return elements.length;
    }

    public Element getPositionInfo() {
        return elements[positionIndex];
    }

    public Element getNormalsInfo() {
        return elements[normalsIndex];
    }

    public Element getTexCoordsInfo() {
        return elements[texCoordIndex];
    }

    public Vector3D[] getPositions() {
        return positions;
    }

    public Vector3D[] getNormals() {
        return normals;
    }

    public Vector3D[] getTangents() {
        return tangents;
    }

    public Vector2D[] getTexCoords() {
        return texCoords;
    }

    public static class Element {
        private int type;
        private int slot;
        private int offset;
        private int baseType;
        private String format;

        public int getSlot() {
            return slot;
        }

        public int getOffset() {
            return offset;
        }

        public int getBaseType() {
            return baseType;
        }

        public int getType() {
            return type;
        }
    }
}
