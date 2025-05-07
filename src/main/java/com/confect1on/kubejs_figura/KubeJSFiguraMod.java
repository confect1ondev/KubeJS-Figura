package com.confect1on.kubejs_figura;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    @SuppressWarnings("removal") // get deprecated as of 1.21.1
    public KubeJSFiguraMod() {
        LOGGER.info("KubeJS Figura initialized.");
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        CHANNEL.registerMessage(
                0,
                AvatarActionPacket.class,
                AvatarActionPacket::encode,
                AvatarActionPacket::decode,
                AvatarActionPacket::handle
        );
        LOGGER.info("KubeJS Figura channel registered.");
    }
}