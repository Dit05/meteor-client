/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ClonedPlayerInput;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.input.Input;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class Premeditate extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> renderOriginal = sgGeneral.add(new BoolSetting.Builder()
        .name("render-original")
        .description("Renders your player model at the original position.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("pulse-delay")
        .description("After the duration in ticks has elapsed, send all packets and start blinking again. 0 to disable.")
        .defaultValue(0)
        .min(0)
        .sliderMax(60)
        .build()
    );

    private FakePlayerEntity model;
    private Input original;
    private final Vector3d start = new Vector3d();
    private final List<Input> inputs = new ArrayList<>();
    private boolean recording = false;
    private int performing = -1;

    @SuppressWarnings("unused")
    private final Setting<Keybind> recordMovement = sgGeneral.add(new KeybindSetting.Builder()
        .name("record-movement")
        .description("Press to start/stop recording movement.")
        .defaultValue(Keybind.none())
        .action(() -> {
            recording = !recording;
            if (performing >= 0) {
               performing = -1;
            }
            if (recording) {
                inputs.clear();
                Utils.set(start, mc.player.getEntityPos());
                if (renderOriginal.get()) {
                    // TODO despawn it consistently
                    // TODO just make everything work properly
                    model = new FakePlayerEntity(mc.player, mc.player.getGameProfile().name(), 20, true);
                    model.doNotPush = true;
                    model.hideWhenInsideCamera = true;
                    model.noHit = true;
                    model.spawn();
                }
                this.info("Recording started");
            } else {
                mc.player.setPos(start.x, start.y, start.z);
                mc.player.setVelocity(Vec3d.ZERO);
                // TODO also despawn when disabling
                if (model != null) {
                    model.despawn();
                    model = null;
                }
                this.info("Recording stopped");
            }
        })
        .build()
    );

    @SuppressWarnings("unused")
    private final Setting<Keybind> replayMovement = sgGeneral.add(new KeybindSetting.Builder()
        .name("replay-movement")
        .description("Replays the last recorded movements.")
        .defaultValue(Keybind.none())
        .action(() -> {
            if (performing == -1) {
                original = mc.player.input; // TODO this might be one of our cloned inputs, in which case it will get stuck
            }
            performing = 0;
            recording = false;
        })
        .build()
    );


    public Premeditate() {
        super(Categories.Movement, "premeditate", "Allows you to essentially teleport while suspending motion updates.");
    }

    @Override
    public void onActivate() {
        if (!Utils.canUpdate()) return;
    }

    @Override
    public void onDeactivate() {
        if (!Utils.canUpdate()) return;
        inputs.clear();
        performing = -1;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;

        if(performing >= 0) {
            if (performing >= inputs.size()) {
                performing = -1;
                mc.player.input = original;
            } else {
                mc.player.input = inputs.get(performing++);
            }
        } else if (recording) {
            inputs.add(new ClonedPlayerInput(mc.player.input));
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!Utils.canUpdate()) return;

        if (!recording) return;
        if (!(event.packet instanceof PlayerMoveC2SPacket p)) return;
        event.cancel();
    }

    @EventHandler
    private void onLeaveGame(GameLeftEvent event) {
        onDeactivate();
    }

    @Override
    public String getInfoString() {
        // TODO better (show progress?)
        return String.format("%s", inputs.size());
    }
}
