/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.InGameHudAccessor;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class MetalFinder extends Module {

    static final String START = "DISTANCE:";
    static final String END = "m";

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> historyLength = sgGeneral.add(new IntSetting.Builder()
        .name("history-length")
        .description("Maximum measurements to store.")
        .min(1)
        .max(4096)
        .defaultValue(256)
        .build()
    );


    int historyIndex = 0;
    int historyCount = 0;
    Vec3d[] posHistory;
    double[] distHistory;


    public MetalFinder() {
        super(Categories.Render, "metal-finder", "Finds metal in the Mines of Divan.");
        createHistory(historyLength.get());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        info(Integer.toString(((InGameHudAccessor) mc.inGameHud).getOverlayRemaining()));

        Text actionBarText = ((InGameHudAccessor) mc.inGameHud).getOverlayMessage();
        if (actionBarText != null) {
            String content = actionBarText.getString();
            int startI = content.indexOf(START);
            int endI = content.indexOf(END);
            if (startI >= 0 && endI >= 0 && startI + START.length() <= endI) {
                String numberStr = content.substring(startI + START.length(), endI);
                double dist = Double.parseDouble(numberStr);
                addToHistory(mc.player.getEntityPos(), dist);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        for(int i = 0; i < historyCount; i++) {
            int index = (historyIndex + 1 + i) % posHistory.length;
            Vec3d pos = posHistory[index];
            Vec3d radius = new Vec3d(0.25, 0.25, 0.25);
            Box box = new Box(pos.subtract(radius), pos.add(radius));
            event.renderer.box(box, new Color(255, 0, 255), new Color(192, 0, 192), ShapeMode.Both, 0);
        }
    }

    private void createHistory(int length) {
        historyIndex = length - 1;
        posHistory = new Vec3d[length];
        distHistory = new double[length];
    }

    private void addToHistory(Vec3d pos, double dist) {
        if (historyLength.get() != posHistory.length) {
            createHistory(historyLength.get());
        }

        posHistory[historyIndex] = pos;
        distHistory[historyIndex] = dist;
        historyIndex--;
        if (historyIndex < 0) {
            historyIndex = posHistory.length - 1;
        }
        if (historyCount < posHistory.length) {
            historyCount++;
        }
    }

}
