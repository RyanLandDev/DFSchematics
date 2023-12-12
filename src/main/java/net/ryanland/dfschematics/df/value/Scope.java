package net.ryanland.dfschematics.df.value;

public enum Scope {

    LOCAL("local"),
    GAME("unsaved"),
    SAVED("saved"),
    LINE("line")
    ;

    private String id;

    Scope(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
