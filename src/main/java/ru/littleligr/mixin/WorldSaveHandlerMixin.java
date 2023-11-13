package ru.littleligr.mixin;

import ru.littleligr.NbtRedis;
import com.mojang.datafixers.DataFixer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(WorldSaveHandler.class)
public class WorldSaveHandlerMixin {

    @Final
    @Shadow
    private File playerDataDir;
    @Final
    @Shadow
    protected DataFixer dataFixer;


    @Inject(method = "savePlayerData", at = @At("HEAD"), cancellable = true)
    public void savePlayerData(PlayerEntity player, CallbackInfo ci) {
        try {
            NbtCompound nbtCompound = player.writeNbt(new NbtCompound());
            String hash = player.getUuidAsString();
            NbtRedis.saveNbt(hash, nbtCompound);
            ci.cancel();
        } catch (Exception var6) {
            NbtRedis.LOGGER.warn("Failed to save player data for {}", player.getName().getString());
        }
    }

    @Inject(method = "loadPlayerData", at = @At("HEAD"), cancellable = true)
    public void loadPlayerData(PlayerEntity player, CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound nbtCompound = null;

        try {
            String hash = player.getUuidAsString();
            NbtCompound nbt = NbtRedis.readNbt(hash);
        } catch (Exception var4) {
            NbtRedis.LOGGER.warn("Failed to load player data for {}", player.getName().getString());
        }

        if (nbtCompound != null) {
            int i = NbtHelper.getDataVersion(nbtCompound, -1);
            player.readNbt(DataFixTypes.PLAYER.update(this.dataFixer, nbtCompound, i));
        }

        cir.setReturnValue(nbtCompound);
    }

}
