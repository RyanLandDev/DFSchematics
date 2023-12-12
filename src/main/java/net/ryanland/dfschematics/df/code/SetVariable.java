package net.ryanland.dfschematics.df.code;

public abstract class SetVariable implements ChestCodeBlock {

    @Override
    public String getBlock() {
        return "set_var";
    }
}
