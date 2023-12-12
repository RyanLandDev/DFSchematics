package net.ryanland.dfschematics.schematic;

import java.util.ArrayList;
import java.util.List;

public class StructureContainer {

    private final List<String> palette = new ArrayList<>();
    private final List<StructurePart> parts = new ArrayList<>();

    public List<String> getPalette() {
        return palette;
    }

    public void addToPalette(String material) {
        material = material.replace("minecraft:", "");
        if (!palette.contains(material)) palette.add(material);
    }

    public int indexOf(String material) {
        return palette.indexOf(material);
    }

    public List<StructurePart> getParts() {
        return parts;
    }

    private StructurePart partInProgress = null;

    public void addBlock(String material) {
        material = material.replace("minecraft:", "");

        if (partInProgress != null && partInProgress.getBlockIndex() != indexOf(material)) {
            finalizePart();
        }
        if (partInProgress == null) {
            partInProgress = new StructurePart(indexOf(material), 0);
        }

        partInProgress.increaseAmount();
    }

    public void finalizePart() {
        addPart(partInProgress);
        partInProgress = null;
    }

    public void addPart(StructurePart part) {
        parts.add(part);
    }
}
