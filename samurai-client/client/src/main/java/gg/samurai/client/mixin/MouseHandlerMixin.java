package gg.samurai.client.mixin;

import gg.samurai.client.mod.BuiltInMods;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onPress", at = @At("HEAD"))
    private void samurai$countClicks(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 1) { // GLFW_PRESS
            BuiltInMods.CPS.click(button);
        }
    }
}
