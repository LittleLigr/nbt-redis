package ru.littleligr;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;

public class NbtRedis implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "nbt-redis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	private static JedisPooled REDIS_POOL;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		MidnightConfig.init(MODID, ConfigModel.class);
		REDIS_POOL = new JedisPooled(ConfigModel.REDIS_IP, ConfigModel.PORT);
		LOGGER.info(MODID + " is loaded");
	}

	public static void saveNbt(String hash, NbtCompound compound) {
		REDIS_POOL.set(hash, compound.asString());
	}

	@Nullable
	public static NbtCompound readNbt(String hash) {
		try {
			String nbt = REDIS_POOL.get(hash);
			if (nbt != null)
				return StringNbtReader.parse(nbt);
			return null;
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static void remove(String hash) {
		REDIS_POOL.del(hash);
	}
}