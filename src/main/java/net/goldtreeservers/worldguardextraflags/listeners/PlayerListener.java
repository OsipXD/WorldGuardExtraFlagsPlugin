package net.goldtreeservers.worldguardextraflags.listeners;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import net.elseland.xikage.MythicMobs.Mobs.EggManager;
import net.elseland.xikage.MythicMobs.Mobs.MythicMob;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import net.goldtreeservers.worldguardextraflags.flags.FlyFlag;
import net.goldtreeservers.worldguardextraflags.flags.GiveEffectsFlag;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        event.getPlayer().removeMetadata("WorldGuardExtraFlagsWaitingForTeleportationToBeDone", WorldGuardExtraFlagsPlugin.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        WorldGuardPlugin wg = WorldGuardExtraFlagsPlugin.getWorldGuard();
        ApplicableRegionSet regions = wg.getRegionContainer().createQuery().getApplicableRegions(event.getEntity().getLocation());
        Boolean keepInventory = regions.queryValue(wg.wrapPlayer(event.getEntity()), WorldGuardExtraFlagsPlugin.KEEP_INVENTORY);
        if (keepInventory != null && keepInventory) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }

        Boolean keepExp = regions.queryValue(wg.wrapPlayer(event.getEntity()), WorldGuardExtraFlagsPlugin.KEEP_EXP);
        if (keepExp != null && keepExp) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        WorldGuardPlugin wg = WorldGuardExtraFlagsPlugin.getWorldGuard();
        ApplicableRegionSet regions = wg.getRegionContainer().createQuery().getApplicableRegions(event.getPlayer().getLocation());
        String prefix = regions.queryValue(wg.wrapPlayer(event.getPlayer()), WorldGuardExtraFlagsPlugin.CHAT_PREFIX);
        String suffix = regions.queryValue(wg.wrapPlayer(event.getPlayer()), WorldGuardExtraFlagsPlugin.CHAT_SUFFIX);

        if (prefix != null) {
            event.setFormat(prefix + event.getFormat());
        }

        if (suffix != null) {
            event.setFormat(event.getFormat() + suffix);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        ApplicableRegionSet regions = WorldGuardExtraFlagsPlugin.getWorldGuard().getRegionContainer().createQuery().getApplicableRegions(event.getPlayer().getLocation());
        Location respawnLocation = regions.queryValue(WorldGuardExtraFlagsPlugin.getWorldGuard().wrapPlayer(event.getPlayer()), WorldGuardExtraFlagsPlugin.RESPAWN_LOCATION);
        if (respawnLocation != null) {
            event.setRespawnLocation(BukkitUtil.toLocation(respawnLocation));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        WorldGuardPlugin wg = WorldGuardExtraFlagsPlugin.getWorldGuard();
        ItemMeta itemMeta = event.getItem().getItemMeta();
        if (itemMeta instanceof PotionMeta) {
            wg.getSessionManager().get(event.getPlayer())
                    .getHandler(GiveEffectsFlag.class)
                    .drinkPotion(Potion.fromItemStack(event.getItem()).getEffects());
            return;
        }

        Material material = event.getItem().getType();
        if (material == Material.MILK_BUCKET) {
            wg.getSessionManager().get(event.getPlayer()).getHandler(GiveEffectsFlag.class).drinkMilk();
            ApplicableRegionSet regions = wg.getRegionContainer().createQuery().getApplicableRegions(event.getPlayer().getLocation());

            List<PotionEffectType> effects = new ArrayList<>();
            for (Set<PotionEffect> potionEffects : regions.queryAllValues(wg.wrapPlayer(event.getPlayer()), WorldGuardExtraFlagsPlugin.GIVE_EFFECTS)) {
                if (potionEffects != null) {
                    for (PotionEffect potionEffect : potionEffects) {
                        if (potionEffect != null) {
                            effects.add(potionEffect.getType());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    event.getPlayer().setAllowFlight(WorldGuardExtraFlagsPlugin.getWorldGuard().getSessionManager().get(event.getPlayer()).getHandler(FlyFlag.class).getFlyStatus());
                } catch (Exception ignored) {
                }
            }
        }.runTask(WorldGuardExtraFlagsPlugin.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!WorldGuardExtraFlagsPlugin.isMythicMobsEnabled()
                || event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK
                || !event.hasItem()) {
            return;
        }

        ItemStack item = event.getItem();
        if (item.getType() != Material.MONSTER_EGG || !item.getItemMeta().hasLore()) {
            return;
        }

        List<String> lore = item.getItemMeta().getLore();
        if (lore.get(0).equals(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "A Mythical Egg that can")) {
            MythicMob mm = EggManager.getMythicMobFromEgg(lore.get(2));
            if (mm != null) {
                ApplicableRegionSet regions = WorldGuardExtraFlagsPlugin.getWorldGuard().getRegionContainer().createQuery().getApplicableRegions(event.getAction() == Action.RIGHT_CLICK_BLOCK ? event.getClickedBlock().getLocation() : event.getPlayer().getLocation());
                State state = regions.queryValue(WorldGuardExtraFlagsPlugin.getWorldGuard().wrapPlayer(event.getPlayer()), WorldGuardExtraFlagsPlugin.MYTHIC_MOBS_EGGS);
                if (state == State.DENY) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Result.DENY);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        //Essentials how dare u do this to me!?!
        if (WorldGuardExtraFlagsPlugin.isEssentialsEnabled()) {
            Player player = event.getPlayer();
            if (player.getGameMode() != GameMode.CREATIVE && !WorldGuardExtraFlagsPlugin.getEssentialsPlugin().getUser(player).isAuthorized("essentials.fly")) {
                //Essentials now turns off flight, fuck him
                try {
                    player.setAllowFlight(WorldGuardExtraFlagsPlugin.getWorldGuard().getSessionManager().get(player).getHandler(FlyFlag.class).getFlyStatus());
                } catch (Exception ignored) {
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    event.getPlayer().setAllowFlight(WorldGuardExtraFlagsPlugin.getWorldGuard().getSessionManager().get(event.getPlayer()).getHandler(FlyFlag.class).getFlyStatus());
                } catch (Exception ignored) {
                }
            }
        }.runTaskLater(WorldGuardExtraFlagsPlugin.getInstance(), 2);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent event) {
        WorldGuardPlugin wg = WorldGuardExtraFlagsPlugin.getWorldGuard();
        RegionContainer regionContainer = wg.getRegionContainer();
        ApplicableRegionSet regions = regionContainer.createQuery().getApplicableRegions(event.getPlayer().getLocation());
        State state = regions.queryState(wg.wrapPlayer(event.getPlayer()), WorldGuardExtraFlagsPlugin.ITEM_DURABILITY);
        if (state == State.DENY) {
            event.setCancelled(true);
        }
    }
}
