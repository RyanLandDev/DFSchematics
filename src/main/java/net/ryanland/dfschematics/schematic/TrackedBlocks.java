package net.ryanland.dfschematics.schematic;

import java.util.ArrayList;
import java.util.List;

public class TrackedBlocks {

    private final List<TrackedBlock> blocks = new ArrayList<>();
    private double[] offset = new double[3];

    public List<TrackedBlock> getBlocks() {
        return blocks;
    }

    public void add(TrackedBlock block) {
        blocks.add(block);
    }

    public void remove(TrackedBlock block) {
        blocks.remove(block);
    }

    protected void reset() {
        for (TrackedBlock block : blocks) block.getLocations().clear();
    }

    public double[] getOffset() {
        return offset;
    }

    public void setOffset(int index, double offset) {
        this.offset[index] = offset;
    }
}
