package net.malisis.core.asm.mixin.core.server.dedicated;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.datafix.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(File anvilFileIn, DataFixer dataFixerIn, YggdrasilAuthenticationService authServiceIn,
            MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn, CallbackInfo ci) {
        System.err.println("I'm gonna mix them up on the server!");
        Thread.dumpStack();
    }

}
