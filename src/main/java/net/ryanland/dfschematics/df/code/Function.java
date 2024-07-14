package net.ryanland.dfschematics.df.code;

import com.google.gson.JsonObject;
import net.ryanland.dfschematics.df.value.Value;

import java.util.List;

public class Function implements ChestCodeBlock {

    private final String name;

    public Function(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public List<Value> getParameters() {
        return null;
    }

    @Override
    public List<Tag> getTags() {
        return List.of(new Tag("Is Hidden", "False"));
    }

    @Override
    public String getBlock() {
        return "func";
    }

    @Override
    public String getAction() {
        return null;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = ChestCodeBlock.super.toJson();
        json.remove("action");
        json.addProperty("data", getName());
        return json;
    }
}
