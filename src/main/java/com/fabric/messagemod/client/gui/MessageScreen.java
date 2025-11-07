package com.fabric.messagemod.client.gui;

import com.fabric.messagemod.network.NetworkHandler;
import com.fabric.messagemod.network.proto.MessageProto;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MessageScreen extends Screen {

    private EditBox textField;
    private Button sendButton;
    private static final int TEXT_FIELD_WIDTH = 300;
    private static final int TEXT_FIELD_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;

    public MessageScreen() {
        super(Component.literal("Send Message"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Text field
        textField = new EditBox(
                this.font,
                centerX - TEXT_FIELD_WIDTH / 2,
                centerY - 30,
                TEXT_FIELD_WIDTH,
                TEXT_FIELD_HEIGHT,
                Component.literal("Message")
        );
        textField.setMaxLength(256);
        textField.setHint(Component.literal("Enter your message..."));
        this.addRenderableWidget(textField);
        this.setInitialFocus(textField);

        // Send button
        sendButton = Button.builder(
                        Component.literal("Send"),
                        button -> onSendButtonPressed()
                )
                .bounds(centerX - BUTTON_WIDTH / 2, centerY + 10, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(sendButton);
    }

    private void onSendButtonPressed() {
        String text = textField.getValue().trim();

        if (text.isEmpty()) {
            return;
        }

        // Create protobuf message
        MessageProto.Message message = MessageProto.Message.newBuilder()
                .setText(text)
                .build();

        // Send to server
        NetworkHandler.MessagePacket packet = new NetworkHandler.MessagePacket(
                message.toByteArray()
        );
        ClientPlayNetworking.send(packet);

        // Clear field and close screen
        textField.setValue("");
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw title
        graphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Send on Enter key
        if (keyCode == 257 && !textField.getValue().trim().isEmpty()) { // 257 = ENTER
            onSendButtonPressed();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
