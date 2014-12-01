package com.potatob.converter;

/**
 * Created by potatob on 11/21/14.
 */
public class Vector3D {

    private final float x;
    private final float y;
    private final float z;
    private final float w;

    public Vector3D(float x, float y, float z) {
        this(x, y, z, 0);
    }

    public Vector3D(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
    }
}
