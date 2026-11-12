package gg.samurai.client.screen;

import gg.samurai.client.mod.HudMod;
import gg.samurai.client.mod.ModManager;
import gg.samurai.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Paged grid of module toggles. Opened with Right Shift or from the title/pause screen. */
public class ModMenuScreen extends Screen {
    private static final int COLS = 3;
    private static final int ROWS = 5;
    private static final int PER_PAGE = COLS * ROWS;

    private final Screen parent;
    private int page;

    public ModMenuScreen(Screen parent) {
        super(Component.literal("Samurai Modules"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        List<HudMod> mods = ModManager.all();
        int cellW = 130, cellH = 24, gap = 6;
        int gridW = COLS * cellW + (COLS - 1) * gap;
        int x0 = width / 2 - gridW / 2;
        int y0 = 54;

        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < mods.size(); i++) {
            HudMod m = mods.get(start + i);
            int cx = x0 + (i % COLS) * (cellW + gap);
            int cy = y0 + (i / COLS) * (cellH + gap);
            addRenderableWidget(new SamuraiButton(cx, cy, cellW, cellH, label(m), b -> {
                m.setEnabled(minecraft, !m.enabled);
                b.setMessage(label(m));
                ModManager.save();
            }));
        }

        int by = y0 + ROWS * (cellH + gap) + 8;
        int pages = Math.max(1, (mods.size() + PER_PAGE - 1) / PER_PAGE);
        SamuraiButton prev = new SamuraiButton(x0, by, 60, 20, Component.literal("<"), b -> { page--; init(); });
        SamuraiButton next = new SamuraiButton(x0 + gridW - 60, by, 60, 20, Component.literal(">"), b -> { page++; init(); });
        prev.active = page > 0;
        next.active = page < pages - 1;
        addRenderableWidget(prev);
        addRenderableWidget(next);
        addRenderableWidget(new SamuraiButton(width / 2 - 50, by, 100, 20, Component.literal("DONE"),
                b -> onClose()));
    }

    private static Component label(HudMod m) {
        return Component.literal(m.name + ": " + (m.enabled ? "ON" : "OFF"));
    }

    @Override
    public void onClose() {
        ModManager.save();
        minecraft.setScreen(parent);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, width, height, 0xE60B0B0D, 0xF016161C);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.fill(0, 0, width, 3, Theme.RED);
        g.pose().pushPose();
        g.pose().translate(width / 2f, 16, 0);
        g.pose().scale(2f, 2f, 1f);
        g.drawCenteredString(font, "MODULES", 0, 0, Theme.WHITE);
        g.pose().popPose();
        g.drawCenteredString(font, "Page " + (page + 1), width / 2, 40, Theme.MUTED);
    }
}
