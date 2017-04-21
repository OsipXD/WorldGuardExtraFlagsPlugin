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

public class ConsoleCommandOnExitFlag extends Handler {
    public static final Factory FACTORY = new Factory();

    protected ConsoleCommandOnExitFlag(Session session) {
        super(session);
    }

    @Override
    public boolean onCrossBoundary(Player player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        for (ProtectedRegion region : exited) {
            Set<String> commands = region.getFlag(WorldGuardExtraFlagsPlugin.consoleCommandOnExit);
            if (commands != null) {
                for (String command : commands) {
                    WorldGuardExtraFlagsPlugin.getPlugin().getServer().dispatchCommand(WorldGuardExtraFlagsPlugin.getPlugin().getServer().getConsoleSender(), command.substring(1).replace("%username%", player.getName()));
                }
            }
        }
        return true;
    }

    public static class Factory extends Handler.Factory<ConsoleCommandOnExitFlag> {
        @Override
        public ConsoleCommandOnExitFlag create(Session session) {
            return new ConsoleCommandOnExitFlag(session);
        }
    }
}
