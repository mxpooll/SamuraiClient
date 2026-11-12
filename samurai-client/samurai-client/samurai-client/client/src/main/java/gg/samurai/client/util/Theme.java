package gg.samurai.client.util;

import net.minecraft.client.gui.GuiGraphics;

/** Samurai colour palette (ARGB) and small drawing helpers. */
public final class Theme {
    public static final int BLACK = 0xFF0B0B0D;
    public static final int GREY_DARK = 0xFF17171B;
    public static final int GREY = 0xFF23232A;
    public static final int RED = 0xFFC8102E;
    public static final int WHITE = 0xFFF4F4F5;
    public static final int MUTED = 0xFF8A8A94;
    public static final int PANEL = 0xB00B0B0D; // translucent panel for HUD

    private Theme() {}

    public static int withAlpha(int argb, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    public static float easeOutCubic(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return 1f - (float) Math.pow(1f - t, 3);
    }

    /** Filled circle drawn as horizontal scanlines (no textures needed). */
    public static void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt((double) r * r - (double) dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }
}
