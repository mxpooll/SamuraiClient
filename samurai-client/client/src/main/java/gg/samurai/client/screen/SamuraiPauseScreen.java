package gg.samurai.client.screen;

import gg.samurai.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

/**
 * Keeps all vanilla pause behaviour (disconnect, advancements, etc.) and adds Samurai
 * branding plus a button to the module menu. Full restyle of the button column is the next step.
 */
public class SamuraiPauseScreen extends PauseScreen {
    public SamuraiPauseScreen(boolean showPauseMenu) {
        super(showPauseMenu);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new SamuraiButton(width - 126, 8, 118, 20,
                Component.literal("SAMURAI MODULES"), b -> minecraft.setScreen(new ModMenuScreen(this))));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        // top banner
        g.fill(0, 0, width, 3, Theme.RED);
        g.drawString(font, "SAMURAI", 8, 9, Theme.WHITE, false);
        g.drawString(font, "CLIENT", 8 + font.width("SAMURAI ") , 9, Theme.RED, false);
    }
}
