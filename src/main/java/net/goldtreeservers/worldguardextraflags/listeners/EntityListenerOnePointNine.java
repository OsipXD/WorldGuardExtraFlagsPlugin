package net.goldtreeservers.worldguardextraflags.listeners;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class EntityListenerOnePointNine implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            if (event.getEntity() instanceof Player) {
                if (WorldGuardExtraFlagsPlugin.getWorldGuard().getSessionManager().hasBypass((Player) event.getEntity(), ((Player) event.getEntity()).getWorld())) {
                    return;
                }
            }

            ApplicableRegionSet regions = WorldGuardExtraFlagsPlugin.getWorldGuard().getRegionContainer().createQuery().getApplicableRegions(event.getEntity().getLocation());
            State allowGliding = regions.queryValue(event.getEntity() instanceof Player ? WorldGuardExtraFlagsPlugin.getWorldGuard().wrapPlayer((Player) event.getEntity()) : null, WorldGuardExtraFlagsPlugin.glide);
            if (allowGliding != null) {
                event.setCancelled(true);
                ((LivingEntity) event.getEntity()).setGliding(allowGliding == State.ALLOW);
            }
        }
    }
}
