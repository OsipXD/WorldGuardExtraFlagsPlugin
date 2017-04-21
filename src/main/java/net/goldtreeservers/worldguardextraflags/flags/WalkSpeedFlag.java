package net.goldtreeservers.worldguardextraflags.flags;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WalkSpeedFlag extends FlagValueChangeHandler<Double> {
    public static final Factory FACTORY = new Factory();
    private Float originalWalkSpeed;

    protected WalkSpeedFlag(Session session) {
        super(session, WorldGuardExtraFlagsPlugin.walkSpeed);
    }

    private void updateWalkSpeed(Player player, Double newValue, World world) {
        if (!this.getSession().getManager().hasBypass(player, world) && newValue != null) {
            if (newValue > 1.0) {
                newValue = 1.0;
            } else if (newValue < -1.0) {
                newValue = -1.0;
            }

            if (player.getWalkSpeed() != newValue.floatValue()) {
                if (this.originalWalkSpeed == null) {
                    this.originalWalkSpeed = player.getWalkSpeed();
                }

                player.setWalkSpeed(newValue.floatValue());
            }
        } else {
            if (this.originalWalkSpeed != null) {
                player.setWalkSpeed(this.originalWalkSpeed);
                this.originalWalkSpeed = null;
            }
        }
    }

    @Override
    protected void onInitialValue(Player player, ApplicableRegionSet set, Double value) {
        this.updateWalkSpeed(player, value, player.getWorld());
    }

    @Override
    protected boolean onSetValue(Player player, Location from, Location to, ApplicableRegionSet toSet, Double currentValue, Double lastValue, MoveType moveType) {
        this.updateWalkSpeed(player, currentValue, to.getWorld());
        return true;
    }

    @Override
    protected boolean onAbsentValue(Player player, Location from, Location to, ApplicableRegionSet toSet, Double lastValue, MoveType moveType) {
        this.updateWalkSpeed(player, null, player.getWorld());
        return true;
    }

    public static class Factory extends Handler.Factory<WalkSpeedFlag> {
        @Override
        public WalkSpeedFlag create(Session session) {
            return new WalkSpeedFlag(session);
        }
    }
}
