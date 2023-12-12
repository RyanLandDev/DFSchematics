package net.ryanland.dfschematics.df.value;

import com.google.gson.JsonObject;

public interface Value {

    String getType();

    JsonObject getData();

    default JsonObject toJson(int slot) {
        JsonObject data = new JsonObject();
        data.addProperty("id", getType());
        data.add("data", getData());

        JsonObject json = new JsonObject();
        json.add("item", data);
        json.addProperty("slot", slot);
        return json;
    }
}
