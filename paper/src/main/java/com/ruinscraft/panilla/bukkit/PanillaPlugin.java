package com.ruinscraft.panilla.bukkit;

import com.ruinscraft.panilla.api.*;
import com.ruinscraft.panilla.api.config.PConfig;
import com.ruinscraft.panilla.api.config.PStrictness;
import com.ruinscraft.panilla.api.config.PTranslations;
import com.ruinscraft.panilla.api.io.IPacketInspector;
import com.ruinscraft.panilla.api.io.IPacketSerializer;
import com.ruinscraft.panilla.api.io.IPlayerInjector;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class PanillaPlugin extends JavaPlugin implements IPanilla {

    private static final String SERVER_IMP = Bukkit.getServer().getClass().getSimpleName();
    private Class<? extends IPacketSerializer> packetSerializerClass;

    private PConfig pConfig;
    private PTranslations pTranslations;
    private IPanillaLogger panillaLogger;
    private IProtocolConstants protocolConstants;
    private IPlayerInjector playerInjector;
    private IPacketInspector packetInspector;
    private IInventoryCleaner containerCleaner;
    private IEnchantments enchantments;
    private Metrics metrics;

    @Override
    public PConfig getPConfig() {
        return pConfig;
    }

    @Override
    public PTranslations getPTranslations() {
        return pTranslations;
    }

    @Override
    public IPanillaLogger getPanillaLogger() {
        return panillaLogger;
    }

    @Override
    public IProtocolConstants getProtocolConstants() {
        return protocolConstants;
    }

    @Override
    public IPacketInspector getPacketInspector() {
        return packetInspector;
    }

    @Override
    public IPlayerInjector getPlayerInjector() {
        return playerInjector;
    }

    @Override
    public IInventoryCleaner getInventoryCleaner() {
        return containerCleaner;
    }

    @Override
    public IEnchantments getEnchantments() {
        return enchantments;
    }

    @Override
    public IPacketSerializer createPacketSerializer(Object byteBuf) {
        try {
            return (IPacketSerializer) packetSerializerClass.getConstructors()[0].newInstance(byteBuf);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void exec(Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    private synchronized void loadConfig() {
        saveDefaultConfig();

        pConfig = new BukkitPConfig();

        pConfig.language = getConfig().getString("language", pConfig.language);
        pConfig.consoleLogging = getConfig().getBoolean("logging.console", pConfig.consoleLogging);
        pConfig.chatLogging = getConfig().getBoolean("logging.chat", pConfig.chatLogging);
        pConfig.strictness = PStrictness.valueOf(getConfig().getString("strictness", pConfig.strictness.name()).toUpperCase());
        pConfig.preventMinecraftEducationSkulls = getConfig().getBoolean("prevent-minecraft-education-skulls", pConfig.preventMinecraftEducationSkulls);
        pConfig.preventFaweBrushNbt = getConfig().getBoolean("prevent-fawe-brush-nbt", pConfig.preventFaweBrushNbt);
        pConfig.ignoreNonPlayerInventories = getConfig().getBoolean("ignore-non-player-inventories", pConfig.ignoreNonPlayerInventories);
        pConfig.noBlockEntityTag = getConfig().getBoolean("no-block-entity-tag", pConfig.noBlockEntityTag);
        pConfig.nbtWhitelist = getConfig().getStringList("nbt-whitelist");
        pConfig.disabledWorlds = getConfig().getStringList("disabled-worlds");
        pConfig.maxNonMinecraftNbtKeys = getConfig().getInt("max-non-minecraft-nbt-keys", pConfig.maxNonMinecraftNbtKeys);
        pConfig.maxNbtDepth = getConfig().getInt("max-nbt-depth", pConfig.maxNbtDepth);
        pConfig.overrideMinecraftMaxEnchantmentLevels = getConfig().getBoolean("max-enchantment-levels.override-minecraft-max-enchantment-levels", pConfig.overrideMinecraftMaxEnchantmentLevels);

        Map<String, Integer> enchantmentOverrides = new HashMap<>();

        if (getConfig().getConfigurationSection("max-enchantment-levels.overrides") != null) {
            for (String enchantmentOverride : getConfig().getConfigurationSection("max-enchantment-levels.overrides").getKeys(false)) {
                int level = getConfig().getInt("max-enchantment-levels.overrides." + enchantmentOverride);
                enchantmentOverrides.put(enchantmentOverride, level);
            }
        }

        pConfig.minecraftMaxEnchantmentLevelOverrides = enchantmentOverrides;
    }

    private synchronized void loadTranslations(String languageKey) {
        try {
            pTranslations = PTranslations.get(languageKey);
        } catch (IOException e) {
            getPanillaLogger().warning("Could not load language translations for " + languageKey, false);
        }
    }

    @Override
    public void onEnable() {
        loadConfig();
        loadTranslations(pConfig.language);

        panillaLogger = new BukkitPanillaLogger(this, getLogger());
        enchantments = new BukkitEnchantments(pConfig);

        initVersion();

        /* Register listeners */
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this, this), this);
        getServer().getPluginManager().registerEvents(new TileLootTableListener(), this);

        /* Register command */
        PanillaCommand panillaCommand = new PanillaCommand(this);
        Bukkit.getCommandMap().register("panilla", new Command("panilla") {
            @Override
            public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String @NotNull [] strings) {
                return panillaCommand.onCommand(commandSender, this, s, strings);
            }
        });

        /* Inject already online players in case of reload */
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                playerInjector.register(this, new BukkitPanillaPlayer(player));
            } catch (IOException e) {
                // Ignore
            }
        }

        /* Initialize Metrics */
        metrics = new Metrics(this, 26380);
    }

    @Override
    public void onDisable() {
        /* Uninject any online players */
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                playerInjector.unregister(new BukkitPanillaPlayer(player));
            } catch (IOException e) {
                // Ignore
            }
        }
        if (metrics != null) {
            metrics.shutdown();
        }
    }

    // Version-group suffix used to load the matching NMS implementation bundle.
    // Each bundle ships its own package: v1_21_4, v1_21_5, v1_21_11.
    private enum VersionGroup {
        V1_21_4("v1_21_4"),
        V1_21_5("v1_21_5"),
        V1_21_11("v1_21_11");

        final String pkg;

        VersionGroup(String pkg) {
            this.pkg = pkg;
        }
    }

    @SuppressWarnings("deprecation")
    private VersionGroup resolveVersionGroup(int dataVersion) {
        // 1.21 / 1.21.1 / 1.21.2 / 1.21.3 / 1.21.4
        if (dataVersion <= 4189) {
            return VersionGroup.V1_21_4;
        }
        // 1.21.5 / 1.21.6 / 1.21.7 / 1.21.8 / 1.21.9 / 1.21.10
        if (dataVersion <= 4556) {
            return VersionGroup.V1_21_5;
        }
        // 1.21.11 / 26.x
        return VersionGroup.V1_21_11;
    }

    @SuppressWarnings("deprecation")
    private int currentDataVersion() {
        try {
            return Bukkit.getUnsafe().getDataVersion();
        } catch (Throwable t) {
            return Integer.MAX_VALUE;
        }
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private void initVersion() {
        int dataVersion = currentDataVersion();
        VersionGroup requested = resolveVersionGroup(dataVersion);

        // 1.21.x servers run Java 21 and ship the v1_21_4/v1_21_5 bundles;
        // 26.x servers run Java 25 and ship the v1_21_11 bundle.
        if (requested == VersionGroup.V1_21_11 && Runtime.version().feature() < 25) {
            getLogger().warning("Server data version " + dataVersion + " needs a Java 25 server and the PanillaX Java 25 build.");
        }

        // Try the requested bundle first, then degrade to the newest available one.
        VersionGroup[] order = VersionGroup.values();
        for (int i = order.length - 1; i >= 0; i--) {
            VersionGroup group = order[i];
            // Never try a bundle newer than the requested one.
            if (group.ordinal() > requested.ordinal()) {
                continue;
            }
            if (loadVersionGroup(group, dataVersion)) {
                if (group != requested) {
                    getLogger().warning("PanillaX bundle '" + group.pkg + "' is not present in this build, tried newest available. "
                            + "Download the correct PanillaX build for server data version " + dataVersion + ".");
                }
                return;
            }
        }

        getLogger().severe("Fatal: could not load any PanillaX implementation bundle for server data version " + dataVersion + ".");
        initLatestFallback();
    }

    @SuppressWarnings("unchecked")
    private boolean loadVersionGroup(VersionGroup group, int dataVersion) {
        String base = "com.ruinscraft.panilla.paper." + group.pkg;

        try {
            packetSerializerClass = (Class<? extends IPacketSerializer>) Class.forName(base + ".io.dplx.PacketSerializer");

            Class<?> playerInjectorClass = Class.forName(base + ".io.PlayerInjector");
            playerInjector = (IPlayerInjector) playerInjectorClass.getDeclaredConstructor().newInstance();

            Class<?> packetInspectorClass = Class.forName(base + ".io.PacketInspector");
            Constructor<?> packetInspectorCtor = packetInspectorClass.getDeclaredConstructor(IPanilla.class);
            packetInspector = (IPacketInspector) packetInspectorCtor.newInstance(this);

            Class<?> containerCleanerClass = Class.forName(base + ".InventoryCleaner");
            Constructor<?> containerCleanerCtor = containerCleanerClass.getDeclaredConstructor(IPanilla.class);
            containerCleaner = (IInventoryCleaner) containerCleanerCtor.newInstance(this);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return false;
        }

        final int maxBookPages = resolveVersionGroup(dataVersion) == VersionGroup.V1_21_11 ? 100 : 50;
        protocolConstants = new IProtocolConstants() {
            @Override
            public int maxBookPages() {
                return maxBookPages;
            }
        };

        getLogger().info("Detected server data version " + dataVersion + " -> using PanillaX implementation bundle '" + group.pkg + "'");
        return true;
    }

    // Handles the unlikely case the reflective bundle load fails. Logs and continues with a best-effort instance.
    @SuppressWarnings("unchecked")
    private void initLatestFallback() {
        try {
            String base = "com.ruinscraft.panilla.paper.v1_21_11";
            packetSerializerClass = (Class<? extends IPacketSerializer>) Class.forName(base + ".io.dplx.PacketSerializer");
            playerInjector = (IPlayerInjector) Class.forName(base + ".io.PlayerInjector").getDeclaredConstructor().newInstance();
            packetInspector = (IPacketInspector) Class.forName(base + ".io.PacketInspector").getDeclaredConstructor(IPanilla.class).newInstance(this);
            containerCleaner = (IInventoryCleaner) Class.forName(base + ".InventoryCleaner").getDeclaredConstructor(IPanilla.class).newInstance(this);
            protocolConstants = new IProtocolConstants() {
                @Override
                public int maxBookPages() {
                    return 100;
                }
            };
        } catch (Throwable t) {
            getLogger().severe("Fatal: could not load any PanillaX implementation.");
            t.printStackTrace();
        }
    }

}
