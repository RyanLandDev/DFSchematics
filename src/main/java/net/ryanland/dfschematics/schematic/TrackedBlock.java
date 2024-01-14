package net.ryanland.dfschematics.schematic;

import javafx.scene.image.ImageView;
import net.ryanland.dfschematics.df.value.Location;
import net.ryanland.dfschematics.df.value.Variable;

import java.util.ArrayList;
import java.util.List;

public final class TrackedBlock {

    private final Variable variable;
    private final String material;
    private final boolean remove;
    private final ImageView icon;
    private final List<Location> locations = new ArrayList<>();

    public TrackedBlock(Variable variable, String material, boolean remove) {
        this.variable = variable;
        this.material = material;
        this.remove = remove;
        this.icon = null;//new ImageView("https://mcapi.marveldc.me/item/"+material+"?width=20&height=20");
    }

    public Variable getVariable() {
        return variable;
    }

    public String getMaterial() {
        return material;
    }

    public boolean isRemoved() {
        return remove;
    }

    public ImageView getIcon() {
        return icon;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void addLocation(Location location) {
        locations.add(location);
    }

    public int getOccurrences() {
        return locations.size();
    }
}
