package gg.samurai.client.mod;

import gg.samurai.client.util.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Base class for every Samurai module. HUD modules draw themselves at (0,0);
 * {@link ModManager} translates and scales them to the user's configured position.
 */
public abstract class HudMod {
    public final String id;
    public final String name;
    public final String category;
    public boolean enabled;
    public int x;
    public int y;
    public float scale = 1.0f;

    protected HudMod(String id, String name, String category, boolean enabledByDefault, int x, int y) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.enabled = enabledByDefault;
        this.x = x;
        this.y = y;
    }

    /** Called every client tick while in a world (enabled modules only). */
    public void onTick(Minecraft mc) {}

    /** Called when toggled on/off from the menu. */
    public void onToggle(Minecraft mc, boolean nowEnabled) {}

    /** Whether this module draws on the HUD. Logic-only modules return false. */
    public boolean hasHud() { return true; }

    /** Draw at origin. */
    public void render(GuiGraphics g, Minecraft mc) {}

    public final void setEnabled(Minecraft mc, boolean value) {
        if (enabled != value) {
            enabled = value;
            onToggle(mc, value);
        }
    }

    /** Standard Samurai text panel: dark translucent box with a red accent bar. */
    protected static void drawTextBox(GuiGraphics g, Minecraft mc, String... lines) {
        int w = 0;
        for (String l : lines) w = Math.max(w, mc.font.width(l));
        int h = lines.length * 10 + 4;
        g.fill(0, 0, w + 9, h, Theme.PANEL);
        g.fill(0, 0, 2, h, Theme.RED);
        for (int i = 0; i < lines.length; i++) {
            g.drawString(mc.font, lines[i], 6, 3 + i * 10, Theme.WHITE, false);
        }
    }
}
