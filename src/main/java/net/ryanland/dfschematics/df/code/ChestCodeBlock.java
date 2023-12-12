package net.ryanland.dfschematics.df.code;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ryanland.dfschematics.df.value.Value;

import java.util.List;

/**
 * Represents a code block with a chest (can have parameters + tags)
 */
public interface ChestCodeBlock extends CodeBlock {

    List<Value> getParameters();

    List<Tag> getTags();

    @Override
    default JsonObject toJson() {
        JsonObject json = CodeBlock.super.toJson();
        JsonObject args = new JsonObject();
        JsonArray items = new JsonArray();

        if (getParameters() != null) {
            int i = 0;
            for (Value parameter : getParameters()) {
                items.add(parameter.toJson(i));
                i++;
            }
        }
        if (getTags() != null) {
            int i = 26;
            for (Tag tag : getTags()) {
                items.add(tag.toJson(getBlock(), getAction(), i));
                i--;
            }
        }

        args.add("items", items);
        json.add("args", args);
        return json;
    }
}
