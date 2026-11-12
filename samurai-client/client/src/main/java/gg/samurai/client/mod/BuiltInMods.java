package gg.samurai.client.mod;

import com.mojang.blaze3d.platform.InputConstants;
import gg.samurai.client.mixin.OptionInstanceAccessor;
import gg.samurai.client.util.Theme;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * First batch of modules. Each is small on purpose: copy the pattern to add more.
 * Positions are default screen-space coordinates (top-left origin).
 */
public final class BuiltInMods {
    public static final String HUD = "HUD";
    public static final String PVP = "PvP";
    public static final String VISUAL = "Visual";

    public static final KeyMapping OPEN_MENU = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.samurai.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.samurai"));
    public static final KeyMapping ZOOM_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.samurai.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.samurai"));

    public static final CpsMod CPS = new CpsMod();
    public static final ZoomMod ZOOM = new ZoomMod();

    private BuiltInMods() {}

    public static void registerAll(List<HudMod> out) {
        out.add(new FpsMod());
        out.add(new CoordinatesMod());
        out.add(CPS);
        out.add(new KeystrokesMod());
        out.add(new ArmorStatusMod());
        out.add(new PingMod());
        out.add(new MemoryMod());
        out.add(new DirectionMod());
        out.add(new ToggleSprintMod());
        out.add(new ToggleSneakMod());
        out.add(ZOOM);
        out.add(new FullbrightMod());
    }

    // ---------------------------------------------------------------- HUD

    /** Counts frames itself so it does not depend on a version-specific FPS getter. */
    static final class FpsMod extends HudMod {
        private int frames;
        private long windowStart = System.currentTimeMillis();
        private int shown;

        FpsMod() { super("fps", "FPS", HUD, true, 6, 6); }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            frames++;
            long now = System.currentTimeMillis();
            if (now - windowStart >= 1000) {
                shown = frames;
                frames = 0;
                windowStart = now;
            }
            drawTextBox(g, mc, shown + " FPS");
        }
    }

    static final class CoordinatesMod extends HudMod {
        CoordinatesMod() { super("coordinates", "Coordinates", HUD, true, 6, 22); }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            var p = mc.player;
            drawTextBox(g, mc, String.format(Locale.ROOT, "XYZ: %.0f / %.0f / %.0f", p.getX(), p.getY(), p.getZ()));
        }
    }

    static final class DirectionMod extends HudMod {
        DirectionMod() { super("direction", "Direction HUD", HUD, true, 6, 38); }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            var p = mc.player;
            String dir = p.getDirection().getName();
            dir = dir.substring(0, 1).toUpperCase(Locale.ROOT) + dir.substring(1);
            float yaw = ((p.getYRot() % 360) + 360) % 360;
            drawTextBox(g, mc, String.format(Locale.ROOT, "%s (%.0f°)", dir, yaw));
        }
    }

    public static final class CpsMod extends HudMod {
        private final Deque<Long> left = new ArrayDeque<>();
        private final Deque<Long> right = new ArrayDeque<>();

        CpsMod() { super("cps", "CPS", HUD, true, 6, 54); }

        /** Called from MouseHandlerMixin on every press. */
        public void click(int button) {
            long now = System.currentTimeMillis();
            if (button == 0) left.addLast(now);
            else if (button == 1) right.addLast(now);
        }

        public int leftCps() { return count(left); }
        public int rightCps() { return count(right); }

        private static int count(Deque<Long> q) {
            long cutoff = System.currentTimeMillis() - 1000;
            while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
            return q.size();
        }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            drawTextBox(g, mc, leftCps() + " | " + rightCps() + " CPS");
        }
    }

    static final class PingMod extends HudMod {
        PingMod() { super("ping", "Ping", HUD, true, 6, 70); }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            String text = "-- ms";
            if (mc.getConnection() != null) {
                PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
                if (info != null && !mc.isLocalServer()) text = info.getLatency() + " ms";
            }
            drawTextBox(g, mc, text);
        }
    }

    static final class MemoryMod extends HudMod {
        MemoryMod() { super("memory", "Memory Usage", HUD, false, 6, 86); }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
            long max = rt.maxMemory() / 1048576L;
            drawTextBox(g, mc, "Mem: " + used + " / " + max + " MB");
        }
    }

    static final class ArmorStatusMod extends HudMod {
        private static final EquipmentSlot[] SLOTS = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND
        };

        ArmorStatusMod() { super("armor_status", "Armor Status", HUD, true, 6, 110); }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            int row = 0;
            for (EquipmentSlot slot : SLOTS) {
                ItemStack stack = mc.player.getItemBySlot(slot);
                if (stack.isEmpty()) continue;
                int y = row++ * 18;
                g.fill(0, y, 64, y + 17, Theme.PANEL);
                g.fill(0, y, 2, y + 17, Theme.RED);
                g.renderItem(stack, 4, y);
                g.renderItemDecorations(mc.font, stack, 4, y);
                if (stack.isDamageableItem()) {
                    int left = stack.getMaxDamage() - stack.getDamageValue();
                    g.drawString(mc.font, String.valueOf(left), 24, y + 5, Theme.WHITE, false);
                }
            }
        }
    }

    static final class KeystrokesMod extends HudMod {
        KeystrokesMod() { super("keystrokes", "Keystrokes", PVP, true, 6, 190); }

        private void key(GuiGraphics g, Minecraft mc, String label, int x, int y, int w, int h, boolean down) {
            g.fill(x, y, x + w, y + h, down ? Theme.withAlpha(Theme.RED, 0.85f) : Theme.PANEL);
            g.drawString(mc.font, label, x + (w - mc.font.width(label)) / 2, y + (h - 8) / 2,
                    down ? Theme.WHITE : Theme.MUTED, false);
        }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            var o = mc.options;
            key(g, mc, "W", 22, 0, 20, 20, o.keyUp.isDown());
            key(g, mc, "A", 0, 22, 20, 20, o.keyLeft.isDown());
            key(g, mc, "S", 22, 22, 20, 20, o.keyDown.isDown());
            key(g, mc, "D", 44, 22, 20, 20, o.keyRight.isDown());
            key(g, mc, "LMB " + CPS.leftCps(), 0, 44, 31, 18, o.keyAttack.isDown());
            key(g, mc, "RMB " + CPS.rightCps(), 33, 44, 31, 18, o.keyUse.isDown());
            key(g, mc, "SPACE", 0, 64, 64, 14, o.keyJump.isDown());
        }
    }

    // ---------------------------------------------------------------- PvP

    /** Uses the vanilla "Toggle Sprint" option and shows its state on the HUD. */
    static final class ToggleSprintMod extends HudMod {
        ToggleSprintMod() { super("toggle_sprint", "Toggle Sprint", PVP, true, 6, 270); }

        @Override
        public void onToggle(Minecraft mc, boolean on) { mc.options.toggleSprint().set(on); }

        @Override
        public void onTick(Minecraft mc) {
            if (!mc.options.toggleSprint().get()) mc.options.toggleSprint().set(true);
        }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            drawTextBox(g, mc, mc.player.isSprinting() ? "[Sprinting (Toggled)]" : "[Sprint: Toggle]");
        }
    }

    static final class ToggleSneakMod extends HudMod {
        ToggleSneakMod() { super("toggle_sneak", "Toggle Sneak", PVP, false, 6, 286); }

        @Override
        public void onToggle(Minecraft mc, boolean on) { mc.options.toggleCrouch().set(on); }

        @Override
        public void onTick(Minecraft mc) {
            if (!mc.options.toggleCrouch().get()) mc.options.toggleCrouch().set(true);
        }

        @Override
        public void render(GuiGraphics g, Minecraft mc) {
            drawTextBox(g, mc, mc.player.isShiftKeyDown() ? "[Sneaking (Toggled)]" : "[Sneak: Toggle]");
        }
    }

    // ------------------------------------------------------------- Visual

    /** Hold the zoom key to narrow FOV. GameRendererMixin reads {@link #factor()}. */
    public static final class ZoomMod extends HudMod {
        private float current = 1f;

        ZoomMod() { super("zoom", "Zoom", VISUAL, true, 0, 0); }

        @Override
        public boolean hasHud() { return false; }

        @Override
        public void onTick(Minecraft mc) {
            float target = ZOOM_KEY.isDown() ? 0.25f : 1f;
            current += (target - current) * 0.35f; // smooth in/out
            if (Math.abs(target - current) < 0.002f) current = target;
        }

        public float factor() { return enabled ? current : 1f; }
    }

    /** Raises the gamma option past its normal range for full brightness. */
    static final class FullbrightMod extends HudMod {
        private double previous = 1.0;

        FullbrightMod() { super("fullbright", "Fullbright", VISUAL, false, 0, 0); }

        @Override
        public boolean hasHud() { return false; }

        @Override
        @SuppressWarnings("unchecked")
        public void onToggle(Minecraft mc, boolean on) {
            var gamma = mc.options.gamma();
            if (on) {
                previous = gamma.get();
                ((OptionInstanceAccessor<Double>) (Object) gamma).samurai$setValue(16.0);
            } else {
                ((OptionInstanceAccessor<Double>) (Object) gamma).samurai$setValue(previous);
            }
        }
    }
}
