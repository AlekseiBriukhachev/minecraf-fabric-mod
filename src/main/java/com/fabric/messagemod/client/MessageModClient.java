package com.fabric.messagemod.client;

import com.fabric.messagemod.client.gui.MessageScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class MessageModClient implements ClientModInitializer {

    private static KeyMapping openScreenKey;

    @Override
    public void onInitializeClient() {
        // Register key binding
        openScreenKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.messagemod.open_screen",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_M,
                KeyMapping.Category.MISC
        ));

        // Register key press handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openScreenKey != null) {
                while (openScreenKey.consumeClick()) {
                    if (client.player != null) {
                        client.setScreen(new MessageScreen());
                    }
                }
            }
        });
    }
}
