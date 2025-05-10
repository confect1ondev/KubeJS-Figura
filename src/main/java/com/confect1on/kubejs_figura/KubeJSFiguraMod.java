package com.confect1on.kubejs_figura;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(KubeJSFiguraMod.MODID)
public class KubeJSFiguraMod {
    public static final String MODID = "kubejs_figura";
    public static final String PROTOCOL_VERSION = "1.0";

    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal") // ResourceLocation deprecated as of 1.20.6
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public KubeJSFiguraMod() {
        CHANNEL.registerMessage(
                0,
                AvatarActionPacket.class,
                AvatarActionPacket::encode,
                AvatarActionPacket::decode,
                AvatarActionPacket::handle
        );
        LOGGER.info("KubeJS Figura channel registered.");

        LOGGER.info("KubeJS Figura initialized.");
    }

    public static void sendToPlayer(ServerPlayer player, AvatarActionPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void load(ServerPlayer player, String modelName) {
        sendToPlayer(player, new AvatarActionPacket("load", modelName, false));
    }

    public static void upload(ServerPlayer player) {
        sendToPlayer(player, new AvatarActionPacket("upload", "", false));
    }

    public static void handle(ServerPlayer player, String modelName, boolean silent) {
        sendToPlayer(player, new AvatarActionPacket("handle", modelName, silent));
    }
}