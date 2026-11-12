package gg.samurai.client.mod;

import gg.samurai.client.config.SamuraiConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Registry for all modules. Add new modules in {@link BuiltInMods#registerAll}. */
public final class ModManager {
    private static final List<HudMod> MODS = new ArrayList<>();

    private ModManager() {}

    public static void init() {
        BuiltInMods.registerAll(MODS);
        SamuraiConfig.load(MODS);
    }

    public static List<HudMod> all() {
        return Collections.unmodifiableList(MODS);
    }

    public static void save() {
        SamuraiConfig.save(MODS);
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null) return;
        for (HudMod m : MODS) {
            if (m.enabled) m.onTick(mc);
        }
    }

    public static void renderHud(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.getDebugOverlay().showDebugScreen()) return;
        for (HudMod m : MODS) {
            if (!m.enabled || !m.hasHud()) continue;
            g.pose().pushPose();
            g.pose().translate(m.x, m.y, 0);
            g.pose().scale(m.scale, m.scale, 1f);
            m.render(g, mc);
            g.pose().popPose();
        }
    }
}
