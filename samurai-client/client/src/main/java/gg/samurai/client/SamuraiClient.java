package gg.samurai.client;

import gg.samurai.client.mod.BuiltInMods;
import gg.samurai.client.mod.ModManager;
import gg.samurai.client.screen.ModMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class SamuraiClient implements ClientModInitializer {
    public static final String VERSION = "0.1.0";

    @Override
    public void onInitializeClient() {
        ModManager.init();

        HudRenderCallback.EVENT.register(ModManager::renderHud);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ModManager.tick(mc);
            while (BuiltInMods.OPEN_MENU.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new ModMenuScreen(null));
            }
        });
    }
}
