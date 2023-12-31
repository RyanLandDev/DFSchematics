package net.ryanland.dfschematics.df.value;

import com.google.gson.JsonObject;

public class Str implements Value {

    private final String value;

    public Str(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getType() {
        return "txt";
    }

    @Override
    public JsonObject getData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", value);
        return json;
    }
}
