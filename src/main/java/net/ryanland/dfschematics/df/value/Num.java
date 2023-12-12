package net.ryanland.dfschematics.df.value;

import com.google.gson.JsonObject;

public class Num implements Value {

    private final String value;

    public Num(String value) {
        this.value = value;
    }

    public Num(float value) {
        this.value = String.valueOf(value).replaceAll("\\.0+$", "");// optionally remove .00 at the end
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getType() {
        return "num";
    }

    @Override
    public JsonObject getData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", value);
        return json;
    }
}
