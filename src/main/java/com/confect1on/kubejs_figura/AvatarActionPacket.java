package com.confect1on.kubejs_figura;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AvatarActionPacket {
    private final String action;
    private final String argument;
    private final boolean silent;

    public AvatarActionPacket(String action, String argument, boolean silent) {
        this.action = action;
        this.argument = argument;
        this.silent = silent;
    }

    public static void encode(AvatarActionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.action);
        buf.writeUtf(msg.argument != null ? msg.argument : "");
        buf.writeBoolean(msg.silent);
    }

    public static AvatarActionPacket decode(FriendlyByteBuf buf) {
        return new AvatarActionPacket(buf.readUtf(), buf.readUtf(), buf.readBoolean());
    }

    public static void handle(AvatarActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                switch (msg.action.toLowerCase()) {
                    case "load":
                        AvatarTrigger.load(msg.argument, msg.silent);
                        break;
                    case "upload":
                        AvatarTrigger.upload(msg.silent);
                        break;
                    case "handle":
                        AvatarTrigger.handle(msg.argument, msg.silent);
                        break;
                    default:
                        System.err.println("[KubeJS Figura] Unknown action: " + msg.action);
                        break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
