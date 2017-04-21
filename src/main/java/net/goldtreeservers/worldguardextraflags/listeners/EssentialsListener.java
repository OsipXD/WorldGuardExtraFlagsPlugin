package net.goldtreeservers.worldguardextraflags.listeners;

import com.sk89q.worldguard.session.SessionManager;
import net.ess3.api.events.GodStatusChangeEvent;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import net.goldtreeservers.worldguardextraflags.flags.GodmodeFlag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EssentialsListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGodStatusChangeEvent(GodStatusChangeEvent event) {
        SessionManager sessionManager = WorldGuardExtraFlagsPlugin.getWorldGuard().getSessionManager();
        Player player = event.getController().getBase();

        if (!sessionManager.hasBypass(player, player.getWorld())) {
            if (sessionManager.get(player).getHandler(GodmodeFlag.class).getIsGodModeEnabled() != null) {
                event.setCancelled(true);
            }
        }
    }
}
