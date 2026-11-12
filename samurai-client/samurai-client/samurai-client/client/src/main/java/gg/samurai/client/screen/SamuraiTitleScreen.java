package gg.samurai.client.screen;

import gg.samurai.client.util.Theme;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

public class SamuraiTitleScreen extends Screen {
    private long openedAt;

    public SamuraiTitleScreen() {
        super(Component.literal("Samurai Client"));
    }

    @Override
    protected void init() {
        openedAt = Util.getMillis();
        int w = 200, h = 24, x = this.width / 2 - w / 2;
        int y = this.height / 2 - 6;
        addRenderableWidget(new SamuraiButton(x, y, w, h, Component.literal("SINGLEPLAYER"),
                b -> minecraft.setScreen(new SelectWorldScreen(this))));
        addRenderableWidget(new SamuraiButton(x, y + 30, w, h, Component.literal("MULTIPLAYER"),
                b -> minecraft.setScreen(new JoinMultiplayerScreen(this))));
        addRenderableWidget(new SamuraiButton(x, y + 60, w, h, Component.literal("MODULES"),
                b -> minecraft.setScreen(new ModMenuScreen(this))));
        addRenderableWidget(new SamuraiButton(x, y + 90, 96, h, Component.literal("OPTIONS"),
                b -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))));
        addRenderableWidget(new SamuraiButton(x + 104, y + 90, 96, h, Component.literal("QUIT"),
                b -> minecraft.stop()));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float t = (Util.getMillis() - openedAt) / 1000f;
        float fade = Theme.easeOutCubic(t / 0.8f);

        // background: black -> dark grey gradient
        g.fillGradient(0, 0, width, height, Theme.BLACK, 0xFF16161C);

        // rising red sun with slow pulse
        int cx = width / 2, cy = (int) (height * 0.26f);
        float pulse = (float) (Math.sin(t * 1.4) * 0.5 + 0.5);
        int r = (int) (Math.min(width, height) * 0.16f * (0.4f + 0.6f * fade));
        Theme.fillCircle(g, cx, cy, r + 8, Theme.withAlpha(Theme.RED, 0.06f + 0.05f * pulse));
        Theme.fillCircle(g, cx, cy, r, Theme.withAlpha(Theme.RED, 0.85f * fade));

        // horizon lines
        int hy = cy + r + 14;
        for (int i = 0; i < 4; i++) {
            int inset = (int) (width * (0.30f - i * 0.05f));
            g.fill(inset, hy + i * 5, width - inset, hy + i * 5 + 1, Theme.withAlpha(Theme.WHITE, 0.07f * fade));
        }

        // wordmark
        g.pose().pushPose();
        g.pose().translate(cx, cy - 8, 0);
        g.pose().scale(3.5f, 3.5f, 1f);
        g.drawCenteredString(font, "SAMURAI", 0, 0, Theme.withAlpha(Theme.WHITE, fade));
        g.pose().popPose();
        g.pose().pushPose();
        g.pose().translate(cx, cy + 24, 0);
        g.pose().scale(1.6f, 1.6f, 1f);
        g.drawCenteredString(font, "C L I E N T", 0, 0, Theme.withAlpha(Theme.RED, 0.4f + 0.6f * fade));
        g.pose().popPose();

        super.render(g, mouseX, mouseY, partialTick);

        g.drawString(font, "Samurai Client 0.1.0", 6, height - 12, Theme.MUTED, false);
    }

    // We draw our own background; skip vanilla blur/dirt.
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {}
}
