package net.ryanland.dfschematics.schematic;

import net.ryanland.dfschematics.Controller;
import net.ryanland.dfschematics.df.value.Location;
import net.sandrohc.schematic4j.schematic.Schematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockEntity;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;

import java.util.*;

public class DFSchematic {

    private final Schematic schematic;
    private final TemplateFactory templateFactory;

    private StructureContainer structure;
    private List<Head> heads;
    private List<Sign> signs;
    private final TrackedBlocks trackedBlocks = new TrackedBlocks();

    private String name;
    private String author;

    public DFSchematic(Schematic schematic) {
        this.schematic = schematic;
        read();

        name = Objects.requireNonNullElse(schematic.name(), Controller.selectedFile.getName().replaceAll("\\.schem$|\\.litematic$|\\.schematic$|", ""));
        author = Objects.requireNonNullElse(schematic.author(), "Unknown");

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

    public TrackedBlocks getTrackedBlocks() {
        return trackedBlocks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    private static final Map<String, String> DEFAULT_BLOCK_STATES = Map.of(
        "snowy", "false",
        "axis", "y",
        "lit", "false",
        "powered", "false",
        "open", "false"
    );

    public void read() {
        //reset in case of a re-read
        structure = new StructureContainer();
        heads = new ArrayList<>();
        signs = new ArrayList<>();
        trackedBlocks.reset();

        // iterates through all x, then z, then y - same as RepeatOnGrid
        SchematicBlockPos offset = schematic.offset();
        System.out.println("offset: " + offset);
        for (int y = 0; y < schematic.height(); y++) {
            for (int z = 0; z < schematic.length(); z++) {
                for (int x = 0; x < schematic.width(); x++) {
                    //System.out.println("xyz: " + x + " " + y + " " + z);
                    SchematicBlock offsetBlock = schematic.block(x + offset.x, y + offset.y, z + offset.z);
                    SchematicBlock block = schematic.block(x, y, z);
                    String material = block.block;
                    //System.out.println("offset: " + offsetBlock.block + " regular: " + block.block);

                    // Tracked Blocks
                    boolean remove = false;
                    for (TrackedBlock trackedBlock : trackedBlocks.getBlocks()) {
                        if (material.equals("minecraft:"+trackedBlock.getMaterial())) {
                            trackedBlock.addLocation(new Location(x, y, z));
                            remove = trackedBlock.isRemoved();
                            break;
                        }
                    }
                    if (remove) {
                        material = "air";
                        block = new SchematicBlock("air");
                    }

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

        for (SchematicBlockEntity block : schematic.blockEntities().toList()) {
            if (block.data.containsKey("front_text")) {
                // Signs after MC 1.20 ----------
                Map<String, Object> frontCompound = ((Map<String, Object>) block.data.get("front_text"));
                Map<String, Object> backCompound = ((Map<String, Object>) block.data.get("back_text"));

                Sign.Side front = new Sign.Side(
                    ((List<String>) frontCompound.get("messages")).toArray(String[]::new),
                    ((byte) frontCompound.get("has_glowing_text")) == 1,
                    (String) frontCompound.get("color")
                );

                Sign.Side back = new Sign.Side(
                    ((List<String>) backCompound.get("messages")).toArray(String[]::new),
                    ((byte) backCompound.get("has_glowing_text")) == 1,
                    (String) backCompound.get("color")
                );

                Sign sign = new Sign(block.pos, front, back);
                if (!sign.isEmpty()) signs.add(sign);

            } else if (block.data.containsKey("Text1")) {
                // Signs before MC 1.20 ---------
                Sign sign = new Sign(block.pos, new Sign.Side(new String[]{
                    ((String) block.data.get("Text1")),
                    ((String) block.data.get("Text2")),
                    ((String) block.data.get("Text3")),
                    ((String) block.data.get("Text4"))}, false, "black"),
                    new Sign.Side(new String[]{"", "", "", ""}, false, "black"));
                if (!sign.isEmpty()) signs.add(sign);
            } else if (block.data.containsKey("SkullOwner")) {
                // Heads -------
                Map<String, Object> data = (TreeMap<String, Object>) block.data.get("SkullOwner");
                String owner = (String) data.get("Name");
                String value = (String) ((Map<String, Object>) ((List<Object>) ((Map<String, Object>) data.get("Properties")).get("textures")).get(0)).get("Value");
                String texture = (owner == null || owner.equals("DF-HEAD") ? value.substring(88) : owner);//substring removes unnecessary eyJ0ZX....
                heads.add(new Head(block.pos(), texture));
            }
        }
    }
}
