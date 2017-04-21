package net.goldtreeservers.worldguardextraflags.flags;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import org.bukkit.Material;

public class MaterialFlag extends Flag<Material> {
    public MaterialFlag(String name) {
        super(name);
    }

    @Override
    public Object marshal(Material o) {
        return o.toString();
    }

    @Override
    public Material parseInput(FlagContext context) throws InvalidFlagFormat {
        return Material.getMaterial(context.getUserInput().trim().toUpperCase());
    }

    @Override
    public Material unmarshal(Object o) {
        return Material.getMaterial(o.toString());
    }
}
