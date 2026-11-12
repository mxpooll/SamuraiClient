package gg.samurai.client.mixin;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionInstance.class)
public interface OptionInstanceAccessor<T> {
    /** Sets the raw value, bypassing the slider's range validation. */
    @Accessor("value")
    void samurai$setValue(T value);
}
