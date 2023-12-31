package net.ryanland.dfschematics.schematic;

public final class StructurePart {

    private final int blockIndex;
    private int amount;

    public StructurePart(int blockIndex, int amount) {
        this.blockIndex = blockIndex;
        this.amount = amount;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void increaseAmount() {
        amount++;
    }

    @Override
    public String toString() {
        return "StructurePart[" +
            "blockIndex=" + blockIndex + ", " +
            "amount=" + amount + ']';
    }

    private static final String[] compressChars;
    static {
        compressChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!#|%&+/<>?@()-=*,:;[]^_`{}~".split("");
    }

    public String getText() {
        int length = compressChars.length; // 80, index 0-79

        int char1Index = (int) Math.floor(blockIndex / length) - 1;
        String char1;
        if (char1Index == -1) char1 = "";
        else char1 = compressChars[char1Index];

        String char2 = compressChars[blockIndex % length];

        return char1 + char2 + (amount == 1 ? "" : amount);
    }

}
