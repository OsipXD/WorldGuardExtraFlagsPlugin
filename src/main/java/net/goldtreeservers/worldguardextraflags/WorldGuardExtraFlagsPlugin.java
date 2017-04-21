package net.goldtreeservers.worldguardextraflags;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.SessionManager;
import net.goldtreeservers.worldguardextraflags.flags.*;
import net.goldtreeservers.worldguardextraflags.listeners.*;
import net.goldtreeservers.worldguardextraflags.utils.PluginUtils;
import net.goldtreeservers.worldguardextraflags.utils.SoundData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WorldGuardExtraFlagsPlugin extends JavaPlugin {
    public final static LocationFlag teleportOnEntry = new LocationFlag("teleport-on-entry");
    public final static LocationFlag teleportOnExit = new LocationFlag("teleport-on-exit");
    public final static CustomSetFlag<String> commandOnEntry = new CustomSetFlag<String>("command-on-entry", new CommandStringFlag(null));
    public final static CustomSetFlag<String> commandOnExit = new CustomSetFlag<String>("command-on-exit", new CommandStringFlag(null));
    public final static CustomSetFlag<String> consoleCommandOnEntry = new CustomSetFlag<String>("console-command-on-entry", new CommandStringFlag(null));
    public final static CustomSetFlag<String> consoleCommandOnExit = new CustomSetFlag<String>("console-command-on-exit", new CommandStringFlag(null));
    public final static DoubleFlag walkSpeed = new DoubleFlag("walk-speed");
    public final static BooleanFlag keepInventory = new BooleanFlag("keep-inventory");
    public final static BooleanFlag keepExp = new BooleanFlag("keep-exp");
    public final static StringFlag chatPrefix = new StringFlag("chat-prefix");
    public final static StringFlag chatSuffix = new StringFlag("chat-suffix");
    public final static SetFlag<PotionEffectType> blockedEffects = new SetFlag<PotionEffectType>("blocked-effects", new PotionEffectTypeFlag(null));
    public final static StateFlag godMode = new StateFlag("godmode", false);
    public final static LocationFlag respawnLocation = new LocationFlag("respawn-location");
    public final static StateFlag worldEdit = new StateFlag("worldedit", true);
    public final static SetFlag<PotionEffect> giveEffects = new SetFlag<PotionEffect>("give-effects", new PotionEffectFlag(null));
    public final static StateFlag fly = new StateFlag("fly", false);
    public final static SetFlag<SoundData> playSounds = new SetFlag<SoundData>("play-sounds", new SoundDataFlag(null));
    public final static StateFlag mythicMobsEggs = new StateFlag("mythicmobs-eggs", true);
    public final static StateFlag frostwalker = new StateFlag("frostwalker", true);
    public final static StateFlag netherPortals = new StateFlag("nether-portals", true);
    public final static SetFlag<Material> allowBlockPlace = new SetFlag<Material>("allow-block-place", new MaterialFlag(null));
    public final static SetFlag<Material> denyBlockPlace = new SetFlag<Material>("deny-block-place", new MaterialFlag(null));
    public final static SetFlag<Material> allowBlockBreak = new SetFlag<Material>("allow-block-break", new MaterialFlag(null));
    public final static SetFlag<Material> denyBlockBreak = new SetFlag<Material>("deny-block-break", new MaterialFlag(null));
    public final static StateFlag glide = new StateFlag("glide", false);
    public final static StateFlag chunkUnload = new StateFlag("chunk-unload", true);
    public final static StateFlag itemDurability = new StateFlag("item-durability", true);

    private static WorldGuardExtraFlagsPlugin plugin;
    private static WorldGuardPlugin worldGuardPlugin;
    private static WorldEditPlugin worldEditPlugin;
    private static Essentials essentialsPlugin;
    private static boolean mythicMobsEnabled;
    private static boolean supportFrostwalker;
    private static boolean fastAsyncWorldEditEnabled;
    private static boolean essentialsEnabled;

    public WorldGuardExtraFlagsPlugin() {
        WorldGuardExtraFlagsPlugin.plugin = this;

        try {
            if (Material.FROSTED_ICE != null) {
                WorldGuardExtraFlagsPlugin.supportFrostwalker = true;
            }
        } catch (NoSuchFieldError ignored) {
        }
    }

    public static WorldGuardExtraFlagsPlugin getPlugin() {
        return WorldGuardExtraFlagsPlugin.plugin;
    }

    public static WorldGuardPlugin getWorldGuard() {
        return WorldGuardExtraFlagsPlugin.worldGuardPlugin;
    }

    public static WorldEditPlugin getWorldEditPlugin() {
        return WorldGuardExtraFlagsPlugin.worldEditPlugin;
    }

    public static boolean isMythicMobsEnabled() {
        return WorldGuardExtraFlagsPlugin.mythicMobsEnabled;
    }

    public static boolean isSupportingFrostwalker() {
        return WorldGuardExtraFlagsPlugin.supportFrostwalker;
    }

    public static boolean isFastAsyncWorldEditEnabled() {
        return WorldGuardExtraFlagsPlugin.fastAsyncWorldEditEnabled;
    }

    public static boolean isEssentialsEnabled() {
        return WorldGuardExtraFlagsPlugin.essentialsEnabled;
    }

    public static Essentials getEssentialsPlugin() {
        return WorldGuardExtraFlagsPlugin.essentialsPlugin;
    }

    public static void doUnloadChunkFlagWorldCheck(World world) {
        for (ProtectedRegion region : WorldGuardExtraFlagsPlugin.getWorldGuard().getRegionManager(world).getRegions().values()) {
            if (region.getFlag(WorldGuardExtraFlagsPlugin.chunkUnload) == State.DENY) {
                System.out.println("Loading chunks for region " + region.getId() + " located in " + world.getName());

                Location min = BukkitUtil.toLocation(world, region.getMinimumPoint());
                Location max = BukkitUtil.toLocation(world, region.getMaximumPoint());

                for (int x = min.getChunk().getX(); x <= max.getChunk().getX(); x++) {
                    for (int z = min.getChunk().getZ(); z <= max.getChunk().getZ(); z++) {
                        world.getChunkAt(x, z).load(true);
                    }
                }
            }
        }
    }

    @Override
    public void onLoad() {
        WorldGuardExtraFlagsPlugin.worldEditPlugin = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");

        WorldGuardExtraFlagsPlugin.worldGuardPlugin = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
        FlagRegistry flagRegistry = WorldGuardExtraFlagsPlugin.worldGuardPlugin.getFlagRegistry();
        flagRegistry.register(WorldGuardExtraFlagsPlugin.teleportOnEntry);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.teleportOnExit);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.commandOnEntry);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.commandOnExit);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.consoleCommandOnEntry);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.consoleCommandOnExit);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.walkSpeed);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.keepInventory);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.keepExp);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.chatPrefix);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.chatSuffix);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.blockedEffects);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.godMode);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.respawnLocation);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.worldEdit);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.giveEffects);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.fly);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.playSounds);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.mythicMobsEggs);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.frostwalker);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.netherPortals);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.allowBlockPlace);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.denyBlockPlace);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.allowBlockBreak);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.denyBlockBreak);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.glide);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.chunkUnload);
        flagRegistry.register(WorldGuardExtraFlagsPlugin.itemDurability);
    }

    @Override
    public void onEnable() {
        SessionManager sessionManager = WorldGuardExtraFlagsPlugin.worldGuardPlugin.getSessionManager();
        sessionManager.registerHandler(TeleportOnEntryFlag.FACTORY, null);
        sessionManager.registerHandler(TeleportOnExitFlag.FACTORY, null);
        sessionManager.registerHandler(CommandOnEntryFlag.FACTORY, null);
        sessionManager.registerHandler(CommandOnExitFlag.FACTORY, null);
        sessionManager.registerHandler(ConsoleCommandOnEntryFlag.FACTORY, null);
        sessionManager.registerHandler(ConsoleCommandOnExitFlag.FACTORY, null);
        sessionManager.registerHandler(WalkSpeedFlag.FACTORY, null);
        sessionManager.registerHandler(BlockedEffectsFlag.FACTORY, null);
        sessionManager.registerHandler(GodmodeFlag.FACTORY, null);
        sessionManager.registerHandler(GiveEffectsFlag.FACTORY, null);
        sessionManager.registerHandler(FlyFlag.FACTORY, null);
        sessionManager.registerHandler(PlaySoundsFlag.FACTORY, null);
        sessionManager.registerHandler(GlideFlag.FACTORY, null);

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new BlockListener(), this);
        pluginManager.registerEvents(new EntityListener(), this);
        pluginManager.registerEvents(new WorldListener(), this);

        try {
            if (EntityToggleGlideEvent.class != null) {
                pluginManager.registerEvents(new EntityListenerOnePointNine(), this);
            }
        } catch (NoClassDefFoundError ignored) {
        }

        Plugin essentialsPlugin = pluginManager.getPlugin("Essentials");
        if (essentialsPlugin != null) {
            WorldGuardExtraFlagsPlugin.essentialsPlugin = (Essentials) essentialsPlugin;
        }
        WorldGuardExtraFlagsPlugin.mythicMobsEnabled = pluginManager.isPluginEnabled("MythicMobs");
        WorldGuardExtraFlagsPlugin.fastAsyncWorldEditEnabled = pluginManager.isPluginEnabled("FastAsyncWorldEdit");
        WorldGuardExtraFlagsPlugin.essentialsEnabled = pluginManager.isPluginEnabled("Essentials");

        if (WorldGuardExtraFlagsPlugin.fastAsyncWorldEditEnabled) {
            PluginUtils.registerFAWE();
        } else {
            WorldGuardExtraFlagsPlugin.worldEditPlugin.getWorldEdit().getEventBus().register(new WorldEditListener());
        }

        if (WorldGuardExtraFlagsPlugin.essentialsEnabled) {
            pluginManager.registerEvents(new EssentialsListener(), this);
        }

        for (World world : this.getServer().getWorlds()) {
            WorldGuardExtraFlagsPlugin.doUnloadChunkFlagWorldCheck(world);
        }
    }
}
