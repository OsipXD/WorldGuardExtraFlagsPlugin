package net.goldtreeservers.worldguardextraflags.flags;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class GodmodeFlag extends FlagValueChangeHandler<State> {
    public static final Factory FACTORY = new Factory();
    private Boolean isGodModeEnabled;
    private Boolean originalEssentialsGodMode;

    protected GodmodeFlag(Session session) {
        super(session, WorldGuardExtraFlagsPlugin.godMode);
    }

    @Nullable
    @Override
    public State getInvincibility(Player player) {
        if (!this.getSession().getManager().hasBypass(player, player.getWorld())) {
            ApplicableRegionSet regions = WorldGuardExtraFlagsPlugin.getWorldGuard().getRegionContainer().createQuery().getApplicableRegions(player.getLocation());
            return regions.queryValue(WorldGuardExtraFlagsPlugin.getWorldGuard().wrapPlayer(player), WorldGuardExtraFlagsPlugin.godMode);
        } else {
            return null;
        }
    }

    public void updateGodMode(Player player, State newValue, World world) {
        if (!this.getSession().getManager().hasBypass(player, world)) {
            this.isGodModeEnabled = newValue == null ? null : newValue == State.ALLOW;

            if (this.isGodModeEnabled != null) {
                if (WorldGuardExtraFlagsPlugin.isEssentialsEnabled()) {
                    if (this.originalEssentialsGodMode == null) {
                        this.originalEssentialsGodMode = WorldGuardExtraFlagsPlugin.getEssentialsPlugin().getUser(player).isGodModeEnabledRaw();
                    }

                    WorldGuardExtraFlagsPlugin.getEssentialsPlugin().getUser(player).setGodModeEnabled(this.isGodModeEnabled);
                }
            } else {
                this.isGodModeEnabled = null;

                if (this.originalEssentialsGodMode != null) {
                    WorldGuardExtraFlagsPlugin.getEssentialsPlugin().getUser(player).setGodModeEnabled(this.originalEssentialsGodMode);
                    this.originalEssentialsGodMode = null;
                }
            }
        }
    }

    @Override
    protected void onInitialValue(Player player, ApplicableRegionSet set, State value) {
        this.updateGodMode(player, value, player.getWorld());
    }

    @Override
    protected boolean onSetValue(Player player, Location from, Location to, ApplicableRegionSet toSet, State currentValue, State lastValue, MoveType moveType) {
        this.updateGodMode(player, currentValue, player.getWorld());
        return true;
    }

    @Override
    protected boolean onAbsentValue(Player player, Location from, Location to, ApplicableRegionSet toSet, State lastValue, MoveType moveType) {
        this.updateGodMode(player, null, player.getWorld());
        return true;
    }

    public Boolean getIsGodModeEnabled() {
        return this.isGodModeEnabled;
    }

    public static class Factory extends Handler.Factory<GodmodeFlag> {
        @Override
        public GodmodeFlag create(Session session) {
            return new GodmodeFlag(session);
        }
    }
}
