package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class TeleportUtil {
    private TeleportUtil() {}

    public static void teleport(Player player, double x, double y, double z) {
        Ref<EntityStore> ref = player.getReference();
        World world = player.getWorld();

        if (ref == null || !ref.isValid() || world == null) return;

        world.execute(() -> {
            Store<EntityStore> store = ref.getStore();

            TransformComponent transform =
                (TransformComponent) store.getComponent(ref, TransformComponent.getComponentType());
            HeadRotation head =
                (HeadRotation) store.getComponent(ref, HeadRotation.getComponentType());

            Vector3f bodyRot = (transform != null)
                ? transform.getRotation().clone()
                : new Vector3f(Float.NaN, Float.NaN, Float.NaN);

            float yaw = (head != null) ? head.getRotation().getYaw() : Float.NaN;

            Teleport tp = new Teleport(
                new Vector3d(x, y, z),
                new Vector3f(bodyRot.getPitch(), yaw, bodyRot.getRoll())
            );

            store.addComponent(ref, Teleport.getComponentType(), tp);
        });
    }
}
