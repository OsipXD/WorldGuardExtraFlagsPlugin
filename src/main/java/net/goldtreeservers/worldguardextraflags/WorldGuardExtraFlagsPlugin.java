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
    public final static LocationFlag TELEPORT_ON_ENTRY = new LocationFlag("teleport-on-entry");
    public final static LocationFlag TELEPORT_ON_EXIT = new LocationFlag("teleport-on-exit");
    public final static LocationFlag RESPAWN_LOCATION = new LocationFlag("respawn-location");

    public final static CustomSetFlag<String> COMMAND_ON_ENTRY = new CustomSetFlag<String>("command-on-entry", new CommandStringFlag(null));
    public final static CustomSetFlag<String> COMMAND_ON_EXIT = new CustomSetFlag<String>("command-on-exit", new CommandStringFlag(null));
    public final static CustomSetFlag<String> CONSOLE_COMMAND_ON_ENTRY = new CustomSetFlag<String>("console-command-on-entry", new CommandStringFlag(null));
    public final static CustomSetFlag<String> CONSOLE_COMMAND_ON_EXIT = new CustomSetFlag<String>("console-command-on-exit", new CommandStringFlag(null));
    public final static SetFlag<PotionEffectType> BLOCKED_EFFECTS = new SetFlag<PotionEffectType>("blocked-effects", new PotionEffectTypeFlag(null));
    public final static SetFlag<PotionEffect> GIVE_EFFECTS = new SetFlag<PotionEffect>("give-effects", new PotionEffectFlag(null));
    public final static SetFlag<SoundData> PLAY_SOUNDS = new SetFlag<SoundData>("play-sounds", new SoundDataFlag(null));
    public final static SetFlag<Material> ALLOW_BLOCK_PLACE = new SetFlag<Material>("allow-block-place", new MaterialFlag(null));
    public final static SetFlag<Material> DENY_BLOCK_PLACE = new SetFlag<Material>("deny-block-place", new MaterialFlag(null));
    public final static SetFlag<Material> ALLOW_BLOCK_BREAK = new SetFlag<Material>("allow-block-break", new MaterialFlag(null));
    public final static SetFlag<Material> DENY_BLOCK_BREAK = new SetFlag<Material>("deny-block-break", new MaterialFlag(null));

    public final static DoubleFlag WALK_SPEED = new DoubleFlag("walk-speed");
    public final static BooleanFlag KEEP_INVENTORY = new BooleanFlag("keep-inventory");
    public final static BooleanFlag KEEP_EXP = new BooleanFlag("keep-exp");
    public final static StringFlag CHAT_PREFIX = new StringFlag("chat-prefix");
    public final static StringFlag CHAT_SUFFIX = new StringFlag("chat-suffix");
    public final static StateFlag GODMODE = new StateFlag("godmode", false);
    public final static StateFlag WORLD_EDIT = new StateFlag("worldedit", true);
    public final static StateFlag FLY = new StateFlag("fly", false);
    public final static StateFlag MYTHIC_MOBS_EGGS = new StateFlag("mythicmobs-eggs", true);
    public final static StateFlag FROSTWALKER = new StateFlag("frostwalker", true);
    public final static StateFlag NETHER_PORTALS = new StateFlag("nether-portals", true);
    public final static StateFlag GLIDE = new StateFlag("glide", false);
    public final static StateFlag CHUNK_UNLOAD = new StateFlag("chunk-unload", true);
    public final static StateFlag ITEM_DURABILITY = new StateFlag("item-durability", true);

    private static WorldGuardExtraFlagsPlugin instance;
    private static WorldGuardPlugin worldGuardPlugin;
    private static WorldEditPlugin worldEditPlugin;
    private static Essentials essentialsPlugin;

    private static boolean mythicMobsEnabled;
    private static boolean supportFrostwalker;
    private static boolean fastAsyncWorldEditEnabled;
    private static boolean essentialsEnabled;

    public WorldGuardExtraFlagsPlugin() {
        instance = this;

        try {
            if (Material.FROSTED_ICE != null) {
                supportFrostwalker = true;
            }
        } catch (NoSuchFieldError ignored) {
        }
    }

    public static WorldGuardExtraFlagsPlugin getInstance() {
        return instance;
    }

    public static WorldGuardPlugin getWorldGuard() {
        return worldGuardPlugin;
    }

    public static WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public static boolean isMythicMobsEnabled() {
        return mythicMobsEnabled;
    }

    public static boolean isSupportingFrostwalker() {
        return supportFrostwalker;
    }

    public static boolean isFastAsyncWorldEditEnabled() {
        return fastAsyncWorldEditEnabled;
    }

    public static boolean isEssentialsEnabled() {
        return essentialsEnabled;
    }

    public static Essentials getEssentialsPlugin() {
        return essentialsPlugin;
    }

    public static void doUnloadChunkFlagWorldCheck(World world) {
        for (ProtectedRegion region : getWorldGuard().getRegionManager(world).getRegions().values()) {
            if (region.getFlag(CHUNK_UNLOAD) == State.DENY) {
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
        worldEditPlugin = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");

        worldGuardPlugin = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
        FlagRegistry flagRegistry = worldGuardPlugin.getFlagRegistry();
        flagRegistry.register(TELEPORT_ON_ENTRY);
        flagRegistry.register(TELEPORT_ON_EXIT);
        flagRegistry.register(COMMAND_ON_ENTRY);
        flagRegistry.register(COMMAND_ON_EXIT);
        flagRegistry.register(CONSOLE_COMMAND_ON_ENTRY);
        flagRegistry.register(CONSOLE_COMMAND_ON_EXIT);
        flagRegistry.register(WALK_SPEED);
        flagRegistry.register(KEEP_INVENTORY);
        flagRegistry.register(KEEP_EXP);
        flagRegistry.register(CHAT_PREFIX);
        flagRegistry.register(CHAT_SUFFIX);
        flagRegistry.register(BLOCKED_EFFECTS);
        flagRegistry.register(GODMODE);
        flagRegistry.register(RESPAWN_LOCATION);
        flagRegistry.register(WORLD_EDIT);
        flagRegistry.register(GIVE_EFFECTS);
        flagRegistry.register(FLY);
        flagRegistry.register(PLAY_SOUNDS);
        flagRegistry.register(MYTHIC_MOBS_EGGS);
        flagRegistry.register(FROSTWALKER);
        flagRegistry.register(NETHER_PORTALS);
        flagRegistry.register(ALLOW_BLOCK_PLACE);
        flagRegistry.register(DENY_BLOCK_PLACE);
        flagRegistry.register(ALLOW_BLOCK_BREAK);
        flagRegistry.register(DENY_BLOCK_BREAK);
        flagRegistry.register(GLIDE);
        flagRegistry.register(CHUNK_UNLOAD);
        flagRegistry.register(ITEM_DURABILITY);
    }

    @Override
    public void onEnable() {
        SessionManager sessionManager = worldGuardPlugin.getSessionManager();
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

        mythicMobsEnabled = pluginManager.isPluginEnabled("MythicMobs");
        fastAsyncWorldEditEnabled = pluginManager.isPluginEnabled("FastAsyncWorldEdit");
        essentialsEnabled = pluginManager.isPluginEnabled("Essentials");

        if (fastAsyncWorldEditEnabled) {
            PluginUtils.registerFAWE();
        } else {
            worldEditPlugin.getWorldEdit().getEventBus().register(new WorldEditListener());
        }

        if (essentialsEnabled) {
            pluginManager.registerEvents(new EssentialsListener(), this);
        }

        for (World world : this.getServer().getWorlds()) {
            doUnloadChunkFlagWorldCheck(world);
        }
    }
}
