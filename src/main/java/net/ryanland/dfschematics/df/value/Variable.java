package net.ryanland.dfschematics.df.value;

import com.google.gson.JsonObject;

public class Variable/*<T extends Value>*/ implements Value {

    private final String name;
    private final Scope scope;

    /**
     * Creates a variable with {@link Scope#LOCAL}
     * @param name The variable name
     */
    public Variable(String name) {
        this(name, Scope.LOCAL);
    }

    /**
     * Creates a variable
     * @param name The variable name
     * @param scope The variable scope
     */
    public Variable(String name, Scope scope) {
        this.name = name;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public String getType() {
        return "var";
    }

    @Override
    public JsonObject getData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("scope", scope.getId());
        return json;
    }

    @Override
    public boolean equals(Object obj) {
        return getName().equals(((Variable) obj).getName()) &&
            getScope().equals(((Variable) obj).getScope());
    }
}
