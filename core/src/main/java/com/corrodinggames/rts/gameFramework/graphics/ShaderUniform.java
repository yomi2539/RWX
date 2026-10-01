package com.corrodinggames.rts.gameFramework.graphics;

public class ShaderUniform {
    /* JADX INFO: renamed from: a */
    public String name;
    public int b = -1;
    /* JADX INFO: renamed from: c */
    public boolean isDirty;
    public boolean d;
    /* JADX INFO: renamed from: e */
    public float[] floatValues = new float[1];
    /* JADX INFO: renamed from: f */
    public Texture texture;
    public boolean g;
    public boolean repeatTexture;
    public boolean linearTexture;
    public ShaderUniformValueType valueType = ShaderUniformValueType.FLOAT;

    public void a(float f) {
        if (this.floatValues.length != 1) {
            this.floatValues = new float[1];
        }
        if (this.floatValues[0] == f) {
            return;
        }
        this.floatValues[0] = f;
        this.isDirty = true;
    }

    public void a(float f, float f2) {
        if (this.floatValues.length != 2) {
            this.floatValues = new float[2];
        }
        if (this.floatValues[0] == f && this.floatValues[1] == f2) {
            return;
        }
        this.floatValues[0] = f;
        this.floatValues[1] = f2;
        this.isDirty = true;
    }

    public void a(float f, float f2, float f3) {
        if (this.floatValues.length != 3) {
            this.floatValues = new float[3];
        }
        if (this.floatValues[0] == f && this.floatValues[1] == f2 && this.floatValues[2] == f3) {
            return;
        }
        this.floatValues[0] = f;
        this.floatValues[1] = f2;
        this.floatValues[2] = f3;
        this.isDirty = true;
    }

    public void a(float f, float f2, float f3, float f4) {
        if (this.floatValues.length != 4) {
            this.floatValues = new float[4];
        }
        if (this.floatValues[0] == f && this.floatValues[1] == f2 && this.floatValues[2] == f3 && this.floatValues[3] == f4) {
            return;
        }
        this.floatValues[0] = f;
        this.floatValues[1] = f2;
        this.floatValues[2] = f3;
        this.floatValues[3] = f4;
        this.isDirty = true;
    }

    public void a(Texture texture) {
        if (this.texture != texture) {
            this.texture = texture;
            this.isDirty = true;
        }
    }

    public void b(Texture texture) {
        this.g = true;
        if (this.texture != texture) {
            this.texture = texture;
            this.isDirty = true;
        }
    }

    public void a(float[] values, ShaderUniformValueType valueType) {
        if (!java.util.Arrays.equals(this.floatValues, values) || this.valueType != valueType) {
            this.floatValues = java.util.Arrays.copyOf(values, values.length);
            this.valueType = valueType;
            this.isDirty = true;
        }
    }
}
