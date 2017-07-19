package net.malisis.core.asm.mixin.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(GameConfiguration gameConfig, CallbackInfo info) {
        System.err.println("I'm gonna mix them up on the client!");
        Thread.dumpStack();
    }
}
