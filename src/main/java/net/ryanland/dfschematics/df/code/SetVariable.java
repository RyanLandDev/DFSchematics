package net.ryanland.dfschematics.df.code;

public abstract class SetVariable implements CodeBlock {

    @Override
    public String getBlock() {
        return "set_var";
    }
}
