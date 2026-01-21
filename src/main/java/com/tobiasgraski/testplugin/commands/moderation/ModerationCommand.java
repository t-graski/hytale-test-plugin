package com.tobiasgraski.testplugin.commands.moderation;

import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

public abstract class ModerationCommand extends CommandBase {
    public ModerationCommand(String action, String description) {
        super(action, description);
        this.setPermissionGroups("op");
    }
}
