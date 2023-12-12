package net.ryanland.dfschematics.df.code;

import com.google.gson.JsonObject;

public record Tag(String name, String option) {

    public JsonObject toJson(String block, String action, int slot) {
        if (block.equals("func")) action = "dynamic";

        JsonObject data = new JsonObject();
        data.addProperty("option", option);
        data.addProperty("tag", name);
        data.addProperty("action", action);
        data.addProperty("block", block);

        JsonObject item = new JsonObject();
        item.addProperty("id", "bl_tag");
        item.add("data", data);

        JsonObject json = new JsonObject();
        json.add("item", item);
        json.addProperty("slot", slot);
        return json;
    }

}
