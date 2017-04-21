package net.goldtreeservers.worldguardextraflags.flags;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;

public class ConsoleCommandOnEntryFlag extends Handler {
    public static final Factory FACTORY = new Factory();

    protected ConsoleCommandOnEntryFlag(Session session) {
        super(session);
    }

    @Override
    public final void initialize(Player player, Location current, ApplicableRegionSet set) {
        for (ProtectedRegion region : set) {
            this.runCommands(region.getFlag(WorldGuardExtraFlagsPlugin.CONSOLE_COMMAND_ON_ENTRY), player);
        }
    }

    @Override
    public boolean onCrossBoundary(Player player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        for (ProtectedRegion region : entered) {
            this.runCommands(region.getFlag(WorldGuardExtraFlagsPlugin.CONSOLE_COMMAND_ON_ENTRY), player);
        }

        return true;
    }

    public void runCommands(Set<String> commands, Player player) {
        if (commands != null) {
            for (String command : commands) {
                WorldGuardExtraFlagsPlugin.getInstance().getServer().dispatchCommand(WorldGuardExtraFlagsPlugin.getInstance().getServer().getConsoleSender(), command.substring(1).replace("%username%", player.getName()));
            }
        }
    }

    public static class Factory extends Handler.Factory<ConsoleCommandOnEntryFlag> {
        @Override
        public ConsoleCommandOnEntryFlag create(Session session) {
            return new ConsoleCommandOnEntryFlag(session);
        }
    }
}
