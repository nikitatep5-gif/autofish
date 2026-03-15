package com.autofish;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class AutoFishMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("autofish");
    public static boolean enabled = false;

    private static KeyMapping toggleKey;

    private enum State { IDLE, WAITING, FISHING, REELING }
    private State state = State.IDLE;
    private int ticks = 0;
    private int recastDelay = 0;

    private static final int SETTLE_TICKS = 30;
    private static final int RECAST_MIN   = 8;
    private static final int RECAST_MAX   = 20;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.autofish.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "key.categories.misc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        LOGGER.info("[AutoFish] Loaded! Press F to toggle.");
    }

    private void tick(Minecraft client) {
        if (client.player == null || client.level == null) return;

        while (toggleKey.consumeClick()) {
            enabled = !enabled;
            state = State.IDLE;
            ticks = 0;
            if (!enabled && client.player.fishing != null) {
                useRod(client);
            }
            client.player.displayClientMessage(
                Component.literal(enabled ? "§a[AutoFish] Enabled" : "§c[AutoFish] Disabled"),
                true
            );
        }

        if (!enabled) return;
        ticks++;

        switch (state) {
            case IDLE    -> handleIdle(client);
            case WAITING -> handleWaiting(client);
            case FISHING -> handleFishing(client);
            case REELING -> handleReeling(client);
        }
    }

    private void handleIdle(Minecraft client) {
        if (!holdingRod(client)) return;
        useRod(client);
        setState(State.WAITING);
    }

    private void handleWaiting(Minecraft client) {
        if (ticks < SETTLE_TICKS) return;
        setState(client.player.fishing != null ? State.FISHING : State.IDLE);
    }

    private void handleFishing(Minecraft client) {
        if (client.player.fishing == null) { setState(State.IDLE); return; }
        if (AutoFishHook.hasBite) {
            AutoFishHook.hasBite = false;
            useRod(client);
            recastDelay = RECAST_MIN + (int)(Math.random() * (RECAST_MAX - RECAST_MIN));
            setState(State.REELING);
        }
    }

    private void handleReeling(Minecraft client) {
        if (ticks >= recastDelay) setState(State.IDLE);
    }

    private void setState(State s) { state = s; ticks = 0; }

    private boolean holdingRod(Minecraft client) {
        return client.player != null &&
               client.player.getMainHandItem().getItem() instanceof FishingRodItem;
    }

    private void useRod(Minecraft client) {
        if (client.gameMode != null && client.player != null) {
            client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        }
    }
}
