package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class TeleportUtil {
    private TeleportUtil() {}

    // -------------------------
    // Existing API (unchanged)
    // -------------------------
    public static void teleport(Player player, double x, double y, double z) {
        if (player == null) return;
        teleport(player.getReference(), x, y, z);
    }

    public static void teleport(PlayerRef playerRef, double x, double y, double z) {
        if (playerRef == null) return;
        teleport(playerRef.getReference(), x, y, z);
    }

    public static void teleport(Ref<EntityStore> ref, double x, double y, double z) {
        // Keep old behavior: preserve current head yaw (if available),
        // preserve body pitch/roll (if available).
        teleport(ref, x, y, z, null);
    }

    // -------------------------
    // New overloads (optional head rotation)
    // -------------------------

    /**
     * Teleport and (optionally) apply a new head rotation.
     * If headRotation is null, existing behavior is preserved.
     *
     * You can pass NaN for any component (pitch/yaw/roll) you want to leave unchanged.
     * HeadRotation.teleportRotation(...) already implements the "ignore NaN" behavior.
     */
    public static void teleport(Player player, double x, double y, double z, Vector3f headRotation) {
        if (player == null) return;
        teleport(player.getReference(), x, y, z, headRotation);
    }

    public static void teleport(PlayerRef playerRef, double x, double y, double z, Vector3f headRotation) {
        if (playerRef == null) return;
        teleport(playerRef.getReference(), x, y, z, headRotation);
    }

    public static void teleport(Ref<EntityStore> ref, double x, double y, double z, Vector3f headRotation) {
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return;

        world.execute(() -> {
            Store<EntityStore> s = ref.getStore();

            TransformComponent transform = (TransformComponent)
                    s.getComponent(ref, TransformComponent.getComponentType());
            HeadRotation head = (HeadRotation)
                    s.getComponent(ref, HeadRotation.getComponentType());

            // Ensure HeadRotation exists if caller wants to set it
            if (headRotation != null && head == null) {
                head = new HeadRotation();
                s.addComponent(ref, HeadRotation.getComponentType(), head);
            }

            // Optionally update head rotation in-place.
            // teleportRotation(...) only applies non-NaN pitch/yaw/roll.
            if (headRotation != null) {
                head.teleportRotation(headRotation);
            }

            // Body rotation: keep existing body pitch/roll if present; otherwise NaN (ignored by HeadRotation logic).
            Vector3f bodyRot = (transform != null)
                    ? transform.getRotation().clone()
                    : new Vector3f(Float.NaN, Float.NaN, Float.NaN);

            // Decide yaw for the Teleport rotation:
            // - If caller provided a yaw (non-NaN), use it.
            // - Else fall back to current head yaw (if available).
            float yaw = Float.NaN;
            if (headRotation != null && !Float.isNaN(headRotation.getYaw())) {
                yaw = headRotation.getYaw();
            } else if (head != null) {
                yaw = head.getRotation().getYaw();
            }

            // Decide pitch for the Teleport rotation:
            // - If caller provided pitch (non-NaN), use it.
            // - Else keep body pitch (if available).
            float pitch = bodyRot.getPitch();
            if (headRotation != null && !Float.isNaN(headRotation.getPitch())) {
                pitch = headRotation.getPitch();
            }

            Teleport tp = new Teleport(
                    new Vector3d(x, y, z),
                    new Vector3f(pitch, yaw, bodyRot.getRoll())
            );

            s.addComponent(ref, Teleport.getComponentType(), tp);
        });
    }
}
