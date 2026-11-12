package gg.samurai.client.screen;

import gg.samurai.client.util.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Flat dark button with a red accent that slides in on hover. */
public class SamuraiButton extends Button {
    private float hover; // 0..1 animated

    public SamuraiButton(int x, int y, int w, int h, Component label, OnPress onPress) {
        super(x, y, w, h, label, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float target = isHoveredOrFocused() && active ? 1f : 0f;
        hover += (target - hover) * Math.min(1f, partialTick * 0.5f + 0.2f);

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        int bg = active ? Theme.withAlpha(Theme.GREY_DARK, 0.88f) : Theme.withAlpha(Theme.BLACK, 0.6f);
        g.fill(x, y, x + w, y + h, bg);
        // red accent grows across the button on hover
        g.fill(x, y, x + 2 + (int) (hover * (w - 2)), y + h, Theme.withAlpha(Theme.RED, 0.15f + 0.65f * hover));
        g.fill(x, y, x + 2, y + h, Theme.RED);

        Minecraft mc = Minecraft.getInstance();
        int color = active ? Theme.WHITE : Theme.MUTED;
        g.drawCenteredString(mc.font, getMessage(), x + w / 2, y + (h - 8) / 2, color);
    }
}
