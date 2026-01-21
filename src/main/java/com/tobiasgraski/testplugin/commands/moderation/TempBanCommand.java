package com.tobiasgraski.testplugin.commands.moderation;

import java.awt.Color;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.TimedBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleBanProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class TempBanCommand extends ModerationCommand {

    private final HytaleBanProvider banProvider;
    private final RequiredArg<PlayerRef> playerArg;
    private final RequiredArg<String> durationArg;
    private final OptionalArg<List<String>> reasonArg;

    public TempBanCommand(HytaleBanProvider banProvider) {
        super("Tempban", "/tempban <target> <duration> [reason]");
        
        this.banProvider = banProvider;

        playerArg = withRequiredArg("target", "Player to temporarily ban", ArgTypes.PLAYER_REF);
        durationArg = withRequiredArg("duration", "Duration to ban the player (e.g. 2d, 48h)", ArgTypes.STRING);
        reasonArg = withListOptionalArg("reason", "Reason for ban", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        if (!ctx.isPlayer()) return;

        PlayerRef bannedPlayer = ctx.get(playerArg);
        String banDurationStr = ctx.get(durationArg);
        List<String> reasonsList = ctx.get(reasonArg);
        String banReason = String.join(" ", reasonsList);
        
        Instant now = Instant.now();
        Instant banExp;
        try {
            banExp = calculateBanExpirationTime(now, banDurationStr);
        } catch (IllegalArgumentException e) {
            ctx.sender().sendMessage(Message.raw(e.getMessage()).color(Color.RED));
            return;
        }

        TimedBan ban = new TimedBan(bannedPlayer.getUuid(), ctx.sender().getUuid(), now, banExp, banReason);

        banProvider.modify(bans -> {
            bans.put(bannedPlayer.getUuid(), ban);
            return true;
        });
        
        ctx.sender().sendMessage(formatBanMessage(bannedPlayer, banDurationStr, banReason));
    }

    private Instant calculateBanExpirationTime(Instant now, String banDurationStr) {
        char banDurationUnit = Character.toLowerCase(banDurationStr.charAt(banDurationStr.length() - 1));
        String durationNumStr = banDurationStr.substring(0, banDurationStr.length() - 1);
        long durationNum = Long.parseLong(durationNumStr);

        if (durationNum <= 0) {
            throw new IllegalArgumentException("Invalid ban duration!");
        }

        return switch (banDurationUnit) {
            case 'y' -> now.plus(durationNum, ChronoUnit.YEARS);
            case 'd' -> now.plus(durationNum, ChronoUnit.DAYS);
            case 'h' -> now.plus(durationNum, ChronoUnit.HOURS);
            case 'm' -> now.plus(durationNum, ChronoUnit.MINUTES);
            case 's' -> now.plus(durationNum, ChronoUnit.SECONDS);
            default -> throw new IllegalArgumentException("Invalid time unit!");
        };
    }
    
    private Message formatBanMessage(PlayerRef bannedPlayer, String banDurationStr, String reason) {
        String banMessage = String.format(
            "Successfully banned %s for %s (Reason: %s)", 
            bannedPlayer.getUsername(),
            banDurationStr,
            reason
            );
        return Message.raw(banMessage).color(Color.RED);
    }
}
