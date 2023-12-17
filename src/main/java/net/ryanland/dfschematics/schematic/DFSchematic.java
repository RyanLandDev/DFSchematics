package net.ryanland.dfschematics.schematic;

import net.sandrohc.schematic4j.schematic.Schematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;

import java.util.HashMap;
import java.util.Map;

public class DFSchematic {

    private final Schematic schematic;
    private final StructureContainer structure = new StructureContainer();
    private final TemplateFactory templateFactory;

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

    private static final Map<String, String> DEFAULT_BLOCK_STATES = Map.of(
        "waterlogged", "false",
        "snowy", "false",
        "axis", "y",
        "lit", "false",
        "powered", "false",
        "open", "false"
    );

    private void read() {
        // iterates through all x, then z, then y - same as RepeatOnGrid
        SchematicBlockPos offset = schematic.offset();
        for (int y = 0; y < schematic.height(); y++) {
            for (int z = 0; z < schematic.length(); z++) {
                for (int x = 0; x < schematic.width(); x++) {
                    //System.out.println("xyz: " + x + " " + y + " " + z);
                    //SchematicBlock block = schematic.getBlock(x+offset[0], y+offset[1], z+offset[2]);
                    SchematicBlock block = schematic.block(x, y, z);

                    String material = block.block;
                    Map<String, String> states = new HashMap<>(block.states);
                    DEFAULT_BLOCK_STATES.forEach(states::remove);
                    String result = material + (states.isEmpty() ? "" : "[" + String.join(",",
                        states.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toList()) + "]");

                    structure.addToPalette(result); // includes block states such as facing=up
                    structure.addBlock(result);

                    if (x == 18 && y == 12 && z == 24) System.out.println(result);
                }
            }
        }
        structure.finalizePart();
    }

}
