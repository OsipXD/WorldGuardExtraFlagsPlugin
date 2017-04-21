package net.goldtreeservers.worldguardextraflags.flags;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class BlockedEffectsFlag extends Handler {
    public static final Factory FACTORY = new Factory();

    protected BlockedEffectsFlag(Session session) {
        super(session);
    }

    @Override
    public void tick(Player player, ApplicableRegionSet set) {
        if (!this.getSession().getManager().hasBypass(player, player.getWorld())) {
            for (Set<PotionEffectType> potionEffects : set.queryAllValues(WorldGuardExtraFlagsPlugin.getWorldGuard().wrapPlayer(player), WorldGuardExtraFlagsPlugin.blockedEffects)) {
                if (potionEffects != null) {
                    for (PotionEffectType potionEffect : potionEffects) {
                        if (potionEffect != null) {
                            player.removePotionEffect(potionEffect);
                        }
                    }
                }
            }
        }
    }

    public static class Factory extends Handler.Factory<BlockedEffectsFlag> {
        @Override
        public BlockedEffectsFlag create(Session session) {
            return new BlockedEffectsFlag(session);
        }
    }
}
