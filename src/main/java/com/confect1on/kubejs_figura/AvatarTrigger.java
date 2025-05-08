package com.confect1on.kubejs_figura;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

public class AvatarTrigger {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Deque<Long> uploadTimestamps = new ArrayDeque<>();
    private static final long RATE_LIMIT_WINDOW = 2 * 60 * 1000;
    private static final int MAX_UPLOADS = 10;

    private static boolean isSilent = false;

    public static void load(String avatarFolderName, boolean silent) {
        // silent isnt really used here, but it keeps it consistent!
        boolean prevSilent = isSilent;
        isSilent = silent;

        try {
            Class<?> fetcherClass = Class.forName("org.figuramc.figura.avatar.local.LocalAvatarFetcher");
            Class<?> managerClass = Class.forName("org.figuramc.figura.avatar.AvatarManager");

            Path baseDir = (Path) fetcherClass.getMethod("getLocalAvatarDirectory").invoke(null);
            Path fullPath = baseDir.resolve(avatarFolderName);

            if (Files.exists(fullPath)) {
                managerClass.getMethod("loadLocalAvatar", Path.class).invoke(null, fullPath);
                LOGGER.info("Loaded Figura avatar: {}", avatarFolderName);
            } else {
                LOGGER.warn("Figura avatar not found at: {}", fullPath);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load Figura avatar '{}'", avatarFolderName, e);
        } finally {
            isSilent = prevSilent;
        }
    }

    public static boolean isAvatarLoaded() {
        try {
            Class<?> figuraMod = Class.forName("org.figuramc.figura.FiguraMod");
            Class<?> avatarManager = Class.forName("org.figuramc.figura.avatar.AvatarManager");

            UUID uuid = (UUID) figuraMod.getMethod("getLocalPlayerUUID").invoke(null);
            Object avatar = avatarManager.getMethod("getLoadedAvatar", UUID.class).invoke(null, uuid);

            return avatar != null;
        } catch (Exception e) {
            LOGGER.error("Error while checking if avatar is loaded", e);
            return false;
        }
    }

    public static boolean canUpload() {
        long now = System.currentTimeMillis();
        while (!uploadTimestamps.isEmpty() && now - uploadTimestamps.peekFirst() > RATE_LIMIT_WINDOW) {
            uploadTimestamps.pollFirst();
        }
        return uploadTimestamps.size() < MAX_UPLOADS;
    }

    public static void upload(boolean silent) {
        boolean prevSilent = isSilent;
        isSilent = silent;

        try {
            Class<?> figuraMod = Class.forName("org.figuramc.figura.FiguraMod");
            UUID uuid = (UUID) figuraMod.getMethod("getLocalPlayerUUID").invoke(null);

            Class<?> avatarManager = Class.forName("org.figuramc.figura.avatar.AvatarManager");
            Object avatar = avatarManager.getMethod("getAvatarForPlayer", UUID.class).invoke(null, uuid);

            if (avatar == null) {
                notify("§c[KubeJS Figura] No avatar is currently loaded.");
                return;
            }

            Class<?> networkStuff = Class.forName("org.figuramc.figura.backend2.NetworkStuff");
            networkStuff.getMethod("uploadAvatar", avatar.getClass()).invoke(null, avatar);

            uploadTimestamps.addLast(System.currentTimeMillis());
            notify("§a[KubeJS Figura] Upload complete.");
            LOGGER.info("Uploaded Figura avatar for player UUID: {}", uuid);
        } catch (Exception e) {
            notify("§c[KubeJS Figura] Upload failed.");
            LOGGER.error("Failed to upload Figura avatar", e);
        } finally {
            isSilent = prevSilent;
        }
    }

    public static void waitForAvatarAndUpload(long timeoutMillis, boolean silent) {
        long startTime = System.currentTimeMillis();
        Runnable checkTask = new Runnable() {
            @Override
            public void run() {
                if (isAvatarLoaded()) {
                    upload(silent);
                    return;
                }
                if (System.currentTimeMillis() - startTime > timeoutMillis) {
                    AvatarTrigger.notify("§c[KubeJS Figura] Avatar load timed out.");
                    return;
                }
                // Try again next client tick
                Minecraft.getInstance().tell(this);
            }
        };
        Minecraft.getInstance().tell(checkTask);
    }

    public static void handle(String avatarFolderName, boolean silent) {
        boolean prevSilent = isSilent;
        isSilent = silent;

        try {
            // 1) Locate the local-avatars directory
            Class<?> fetcherClass = Class.forName("org.figuramc.figura.avatar.local.LocalAvatarFetcher");
            Path baseDir = (Path) fetcherClass
                    .getMethod("getLocalAvatarDirectory")
                    .invoke(null);

            // 2) Resolve the requested folder
            Path fullPath = baseDir.resolve(avatarFolderName);

            // 3) Folder-exists check (must be a directory!)
            if (!Files.isDirectory(fullPath)) {
                notify("§c[KubeJS Figura] Avatar folder not found: " + avatarFolderName);
                return;
            }

            // 4) Rate-limit check
            if (!canUpload()) {
                notify("§e[KubeJS Figura] Upload rate limit reached. Try again soon.");
                return;
            }

            // 5) Happy path: load then upload
            load(avatarFolderName, isSilent);
            // wait up to 10s for it to actually load
            waitForAvatarAndUpload(10_000, isSilent);

            if (!isAvatarLoaded()) {
                notify("§c[KubeJS Figura] Avatar load timed out.");
                return;
            }

            upload(isSilent);
        } catch (Exception e) {
            LOGGER.error("Error in AvatarTrigger.handle()", e);
            notify("§c[KubeJS Figura] Failed to load or upload avatar.");
        } finally {
            isSilent = prevSilent;
        }
    }

    public static void notify(String message) {
        if (isSilent) return;
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));
        }
    }
}
