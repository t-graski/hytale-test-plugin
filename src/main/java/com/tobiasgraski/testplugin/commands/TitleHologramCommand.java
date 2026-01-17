package com.tobiasgraski.testplugin.commands;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TitleHologramCommand extends CommandBase {

    private final RequiredArg<String> textArg;

    public TitleHologramCommand() {
        super("TitleHologram", "Create a hologram.");

        textArg = withRequiredArg("text", "text", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        var playerUuid = ctx.sender().getUuid();
        var playerRef = Universe.get().getPlayer(playerUuid);
        var world = Universe.get().getWorld(playerRef.getWorldUuid());
        var playerTransform = playerRef.getTransform();

        world.execute(() -> {
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            var projectileComp = new ProjectileComponent("Projectile");
            holder.putComponent(ProjectileComponent.getComponentType(), projectileComp);
            holder.putComponent(TransformComponent.getComponentType(), new TransformComponent(playerTransform.getPosition().clone(), playerTransform.getRotation().clone()));
            holder.ensureComponent(UUIDComponent.getComponentType());

            if (projectileComp.getProjectile() == null) {
                projectileComp.initialize();

                if (projectileComp.getProjectile() == null)
                    return;
            }

            holder.addComponent(NetworkId.getComponentType(), new NetworkId(world.getEntityStore().getStore().getExternalData().takeNextNetworkId()));
            holder.addComponent(Nameplate.getComponentType(), new Nameplate(ctx.get(textArg)));

            world.getEntityStore().getStore().addEntity(holder, AddReason.SPAWN);
        });
    }
}
