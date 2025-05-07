A lightweight bridge that exposes core Figura avatar functionality directly to KubeJS scripts. This is designed to add minimal strain to environments and simply provide a safe way to (up)load avatars from server scripts.

For safety, it can only load avatars that already exist in the players avatar folder. This is primarily designed for automatic character systems scripted on the server-side, and allows developers to integrate Figura.


```
// Example usage of KubeJS Figura in a server script.

const StringArgumentType = Java.loadClass("com.mojang.brigadier.arguments.StringArgumentType");
const ServerPlayer = Java.loadClass("net.minecraft.server.level.ServerPlayer");
const AutoFiguraMod = Java.loadClass("com.confect1on.kubejs_figura.KubeJSFiguraMod");
const AvatarActionPacket = Java.loadClass("com.confect1on.kubejs_figura.AvatarActionPacket");
const PacketDistributor = Java.loadClass("net.minecraftforge.network.PacketDistributor");

ServerEvents.commandRegistry(event => {
  const { commands: Commands } = event;

  event.register(
    Commands.literal("figuraLoad")
      .then(
        Commands.argument("model", StringArgumentType.string())
          .executes(ctx => {
            const player = ctx.source.entity;
            const model = StringArgumentType.getString(ctx, "model");

            if (player instanceof ServerPlayer) {
              AutoFiguraMod.CHANNEL.send(
                PacketDistributor.PLAYER.with(() => player),
                new AvatarActionPacket("load", model, false)
              );
            }

            return 1;
          })
      )
  );

  event.register(
    Commands.literal("figuraUpload")
      .executes(ctx => {
        const player = ctx.source.entity;

        if (player instanceof ServerPlayer) {
          AutoFiguraMod.CHANNEL.send(
            PacketDistributor.PLAYER.with(() => player),
            new AvatarActionPacket("upload", "", false)
          );
        }

        return 1;
      })
  );

// The handle action combines load and upload with additional safety checks.
  event.register(
    Commands.literal("figuraHandle")
      .then(
        Commands.argument("charName", StringArgumentType.string())
          .executes(ctx => {
            const player = ctx.source.entity;
            const charName = StringArgumentType.getString(ctx, "charName");

            if (player instanceof ServerPlayer) {
              AutoFiguraMod.CHANNEL.send(
                PacketDistributor.PLAYER.with(() => player),
                new AvatarActionPacket("handle", charName, false)
              );
            }

            return 1;
          })
      )
  );
});

```
