package net.ryanland.dfschematics.schematic;

import net.sandrohc.schematic4j.schematic.Schematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockEntity;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;

import java.util.*;

public class DFSchematic {

    private final Schematic schematic;
    private final StructureContainer structure = new StructureContainer();
    private final TemplateFactory templateFactory;

    private final List<Head> heads = new ArrayList<>();
    private final List<Sign> signs = new ArrayList<>();

    public DFSchematic(Schematic schematic) {
        this.schematic = schematic;
        read();
        templateFactory = new TemplateFactory(this);
    }

    public Schematic getSchematic() {
        return schematic;
    }

    public StructureContainer getStructure() {
        return structure;
    }

    public TemplateFactory getTemplateFactory() {
        return templateFactory;
    }

    public List<Head> getHeads() {
        return heads;
    }

    public List<Sign> getSigns() {
        return signs;
    }

    private static final Map<String, String> DEFAULT_BLOCK_STATES = Map.of(
        "snowy", "false",
        "axis", "y",
        "lit", "false",
        "powered", "false",
        "open", "false"
    );

    private void read() {
        // iterates through all x, then z, then y - same as RepeatOnGrid
        SchematicBlockPos offset = schematic.offset();
        System.out.println("offset: " + offset);
        for (int y = 0; y < schematic.height(); y++) {
            for (int z = 0; z < schematic.length(); z++) {
                for (int x = 0; x < schematic.width(); x++) {
                    //System.out.println("xyz: " + x + " " + y + " " + z);
                    SchematicBlock offsetBlock = schematic.block(x + offset.x, y + offset.y, z + offset.z);
                    SchematicBlock block = schematic.block(x, y, z);

                    //System.out.println("offset: " + offsetBlock.block + " regular: " + block.block);

                    String material = block.block;
                    Map<String, String> states = new HashMap<>(block.states);
                    DEFAULT_BLOCK_STATES.forEach(states::remove);
                    String result = material + (states.isEmpty() ? "" : "[" + String.join(",",
                        states.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toList()) + "]");

                    structure.addToPalette(result); // includes block states such as facing=up
                    structure.addBlock(result);
                }
            }
        }
        structure.finalizePart();

        //TODO support sign glowing ink sac
        for (SchematicBlockEntity block : schematic.blockEntities().toList()) {
            if (block.data.containsKey("front_text")) {
                // Signs after MC 1.20 ----------
               String[] front = ((List<String>) ((Map<String, Object>) block.data.get("front_text")).get("messages")).toArray(String[]::new);
                String[] back = ((List<String>) ((Map<String, Object>) block.data.get("back_text")).get("messages")).toArray(String[]::new);
                Sign sign = new Sign(block.pos, front, back);
                if (!sign.isEmpty()) signs.add(sign);
            } else if (block.data.containsKey("Text1")) {
                // Signs before MC 1.20 ---------
                Sign sign = new Sign(block.pos, new String[]{
                    ((String) block.data.get("Text1")),
                    ((String) block.data.get("Text2")),
                    ((String) block.data.get("Text3")),
                    ((String) block.data.get("Text4"))},
                    new String[]{"", "", "", ""});
                if (!sign.isEmpty()) signs.add(sign);
            } else if (block.data.containsKey("SkullOwner")) {
                // Heads -------
                Map<String, Object> data = (TreeMap<String, Object>) block.data.get("SkullOwner");
                String owner = (String) data.get("Name");
                String value = (String) ((Map<String, Object>) ((List<Object>) ((Map<String, Object>) data.get("Properties")).get("textures")).get(0)).get("Value");
                String texture = owner == null || owner.equals("DF-HEAD") ? value : owner;
                heads.add(new Head(block.pos(), texture));
            }
        }
    }
}
