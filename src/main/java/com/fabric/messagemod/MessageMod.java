package com.fabric.messagemod;

import com.fabric.messagemod.database.DatabaseManager;
import com.fabric.messagemod.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MessageMod implements ModInitializer {
    public static final String MOD_ID = "messagemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing MessageMod");

        // Register network packets
        NetworkHandler.registerPackets();

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
            DatabaseManager.getInstance().initialize(configDir);
            NetworkHandler.registerServerHandlers();
            LOGGER.info("Server starting - database initialized");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DatabaseManager.getInstance().shutdown();
            LOGGER.info("Server stopping - database shut down");
        });
    }
}
