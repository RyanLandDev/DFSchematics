package net.ryanland.dfschematics.schematic.special;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.ryanland.dfschematics.df.value.Item;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;

import java.util.List;

public record Sign(SchematicBlockPos pos, Side front, Side back) implements Item {

    // example sign
    // {Count:1b,DF_NBT:3578,id:\"minecraft:oak_sign\",tag:{display:{Lore:['{\"extra\":[{\"text\":\"1front\"}]}','{\"extra\":[{\"text\":\"2front\"}]}','{\"extra\":[{\"text\":\"3front\"}]}','{\"extra\":[{\"text\":\"4front\"}]}','{\"extra\":[{\"text\":\"1back\"}]}','{\"extra\":[{\"text\":\"2back\"}]}','{\"extra\":[{\"text\":\"3back\"}]}','{\"extra\":[{\"text\":\"4back\"}]}']}}}

    @Override
    public String getItemNBT() {
        JsonObject item = new JsonObject();
        item.addProperty("id", "minecraft:oak_sign");
        item.addProperty("count", 1);

        JsonArray lore = new JsonArray(8);
        for (String frontLine : front.lines) lore.add(frontLine);
        lore.add(front.getDFColor());
        lore.add(front.getDFGlowing());
        for (String backLine : back.lines) lore.add(backLine);
        lore.add(back.getDFColor());
        lore.add(back.getDFGlowing());

        JsonObject components = new JsonObject();
        components.add("lore", lore);
        components.addProperty("custom_name", extra("%s,%s,%s".formatted(pos.x, pos.y, pos.z)));
        item.add("components", components);

        return item.toString();
        // Results in an oak sign with
        // Item name as xyz coordinates
        // 1-4 lore lines as sign's front lines & 5-6 lore lines as sign's front color and glow
        // 7-10 lore lines as sign's back lines & 11-12 lore lines as sign's back color and glow
    }

    private static String extra(String input) {
        return "{\"extra\":[{\"text\":\"%s\"}],\"text\":\"\"}".formatted(input);
    }

    public boolean isEmpty() {
        return front.lines.stream().allMatch(this::isComponentEmpty) &&
            back.lines.stream().allMatch(this::isComponentEmpty);
    }

    private boolean isComponentEmpty(String json) {
        if (json.isEmpty()) return true;
        Component component = GsonComponentSerializer.gson().deserialize(json);
        return LegacyComponentSerializer.legacySection().serialize(component).isEmpty();
    }

    public record Side(List<String> lines, boolean glowing, String color) {

        private String getDFGlowing() {
            return extra(glowing ? "Enable" : "Disable");
        }

        private String getDFColor() {
            return extra(color.substring(0, 1).toUpperCase() + color.substring(1).replaceAll("_", " "));
        }

    }
}
