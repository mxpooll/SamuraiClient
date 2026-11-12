package gg.samurai.client.mixin;

import gg.samurai.client.screen.SamuraiPauseScreen;
import gg.samurai.client.screen.SamuraiTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    /** Swap vanilla title/pause screens for Samurai ones. */
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen samurai$replaceScreen(Screen screen) {
        if (screen instanceof TitleScreen) {
            return new SamuraiTitleScreen();
        }
        if (screen instanceof PauseScreen && !(screen instanceof SamuraiPauseScreen)) {
            return new SamuraiPauseScreen(true);
        }
        return screen;
    }

    /** Window title branding. */
    @Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
    private void samurai$title(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("Samurai Client - " + cir.getReturnValue());
    }
}
