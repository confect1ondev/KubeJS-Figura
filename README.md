A lightweight bridge that exposes core Figura avatar functionality directly to KubeJS scripts. This is designed to add minimal strain to environments and simply provide a safe way to (up)load avatars from server scripts.

For safety, it can only load avatars that already exist in the players avatar folder. This is primarily designed for automatic character systems scripted on the server-side, and allows developers to integrate Figura.


```
// Example usage of KubeJS Figura in a server script.

const StringArgumentType = Java.loadClass("com.mojang.brigadier.arguments.StringArgumentType");
const ServerPlayer = Java.loadClass("net.minecraft.server.level.ServerPlayer");
const KJSFigura = Java.loadClass("com.confect1on.kubejs_figura.KubeJSFiguraMod");

ServerEvents.commandRegistry(event => {
  const { commands: Commands } = event;

  event.register(
    Commands.literal("figuraLoad")
      .then(
        Commands.argument("model", StringArgumentType.greedyString())
          .executes(ctx => {
            const player = ctx.source.entity;
            const model = StringArgumentType.getString(ctx, "model");

            if (player instanceof ServerPlayer) {
              KJSFigura.load(player, model);
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
          KJSFigura.upload(player);
        }

        return 1;
      })
  );

  event.register(
    Commands.literal("figuraHandle")
      .then(
        Commands.argument("charName", StringArgumentType.greedyString())
          .executes(ctx => {
            const player = ctx.source.entity;
            const charName = StringArgumentType.getString(ctx, "charName");

            if (player instanceof ServerPlayer) {
              KJSFigura.handle(player, charName, false);
            }

            return 1;
          })
      )
  );
});

```
