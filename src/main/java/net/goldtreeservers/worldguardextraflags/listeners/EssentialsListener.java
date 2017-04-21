package net.goldtreeservers.worldguardextraflags.listeners;

import net.ess3.api.events.GodStatusChangeEvent;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import net.goldtreeservers.worldguardextraflags.flags.GodmodeFlag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EssentialsListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGodStatusChangeEvent(GodStatusChangeEvent event) {
        if (!WorldGuardExtraFlagsPlugin.getWorldGuard().getSessionManager().hasBypass(event.getController().getBase(), event.getController().getBase().getWorld())) {
            if (WorldGuardExtraFlagsPlugin.getWorldGuard().getSessionManager().get(event.getController().getBase()).getHandler(GodmodeFlag.class).getIsGodModeEnabled() != null) {
                event.setCancelled(true);
            }
        }
    }
}
