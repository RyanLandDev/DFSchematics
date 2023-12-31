package net.ryanland.dfschematics.df.value;

import com.google.gson.JsonObject;

public interface Item extends Value {

    @Override
    default String getType() {
        return "item";
    }

    @Override
    default JsonObject getData() {
        JsonObject json = new JsonObject();
        json.addProperty("item", getItemNBT());
        return json;
    }

    /**
     * Returns a JSON String representing the item's NBT data.
     */
    String getItemNBT();

}
