package net.ryanland.dfschematics.df.code;

import net.ryanland.dfschematics.df.value.Value;
import net.ryanland.dfschematics.df.value.Variable;

import java.util.ArrayList;
import java.util.List;

public class SetVariableAppendValue extends SetVariable {

    private final Variable variable;
    private final List<Value> values;

    public SetVariableAppendValue(Variable variable, List<? extends Value> values) {
        this.variable = variable;
        this.values = (List<Value>) values;
    }

    @Override
    public List<Value> getParameters() {
        List<Value> parameters = new ArrayList<>();
        parameters.add(variable);
        parameters.addAll(values);
        return parameters;
    }

    @Override
    public List<Tag> getTags() {
        return null;
    }

    @Override
    public String getAction() {
        return "AppendValue";
    }

    @Override
    public int getWeight() {
        if (getParameters().get(1).getType().equals("item")) return 1;// block entities
        return getParameters().size();
    }
}
