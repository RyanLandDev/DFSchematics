package net.ryanland.dfschematics.df.value;

import com.google.gson.JsonObject;

public class Location implements Value {

    private final float x;
    private final float y;
    private final float z;
    private final float pitch;
    private final float yaw;

    public Location(float x, float y, float z) {
        this(x, y, z, 0, 0);
    }

    public Location(float x, float y, float z, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
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

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public Location add(Location location) {
        return new Location(x+location.x, y+location.y, z+location.z, pitch+location.pitch, yaw+location.yaw);
    }

    @Override
    public String getType() {
        return "loc";
    }

    @Override
    public JsonObject getData() {
        JsonObject loc = new JsonObject();
        loc.addProperty("x", x);
        loc.addProperty("y", y);
        loc.addProperty("z", z);
        loc.addProperty("pitch", pitch);
        loc.addProperty("yaw", yaw);

        JsonObject json = new JsonObject();
        json.addProperty("isBlock", false);//what is this for???
        json.add("loc", loc);
        return json;
    }
}
