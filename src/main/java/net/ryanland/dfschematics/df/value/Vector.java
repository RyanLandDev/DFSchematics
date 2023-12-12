package net.ryanland.dfschematics.df.value;

import com.google.gson.JsonObject;

public class Vector implements Value {

    private final float x;
    private final float y;
    private final float z;

    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    @Override
    public String getType() {
        return "vec";
    }

    @Override
    public JsonObject getData() {
        JsonObject json = new JsonObject();
        json.addProperty("x", x);
        json.addProperty("y", y);
        json.addProperty("z", z);
        return json;
    }
}
