package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActiveDuels {

    public static final long DUEL_TIME_LIMIT_MS = 5 * 60_000; // 5 minutes

    // key: any player in duel -> session
    private static final ConcurrentHashMap<UUID, DuelSession> byPlayer = new ConcurrentHashMap<>();

    public enum EndReason { DEATH, TIMEOUT, FORFEIT }

    public static final class DuelSession {
        public final UUID a;
        public final UUID b;
        public final long startMs;

        public DuelSession(UUID a, UUID b) {
            this.a = a;
            this.b = b;
            this.startMs = System.currentTimeMillis();
        }

        public UUID other(UUID me) {
            return me.equals(a) ? b : a;
        }

        public boolean involves(UUID p) {
            return a.equals(p) || b.equals(p);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - startMs > DUEL_TIME_LIMIT_MS;
        }
    }

    private ActiveDuels() {}

    public static boolean isInDuel(UUID player) {
        return byPlayer.containsKey(player);
    }

    public static DuelSession get(UUID player) {
        return byPlayer.get(player);
    }

    /** Start duel; overwrites any existing mapping for these players. */
    public static void start(UUID a, UUID b) {
        DuelSession session = new DuelSession(a, b);
        byPlayer.put(a, session);
        byPlayer.put(b, session);
    }

    /**
     * End duel by any participant. Returns the ended session (or null if not in duel).
     * Removes both players from registry exactly once.
     */
    public static DuelSession end(UUID participant, EndReason reason) {
        DuelSession session = byPlayer.remove(participant);
        if (session == null) return null;

        byPlayer.remove(session.other(participant), session);

        // Teleport both players out to lobby with fixed orientation (0, -90, 0 degrees)
        teleportOut(session);

        notifyEnded(session, participant, reason);
        
        healOut(session);

        return session;
    }

    private static void healOut(DuelSession s) {
        PlayerRef aRef = Universe.get().getPlayer(s.a);
        PlayerRef bRef = Universe.get().getPlayer(s.b);

        Player a = resolveOnlinePlayer(aRef);
        Player b = resolveOnlinePlayer(bRef);

        healOneOut(a);
        healOneOut(b);
    }

    private static void healOneOut(Player p) {
        if (p == null) return;

        Ref<EntityStore> ref = p.getReference();
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return;

        world.execute(() -> {
            p.invalidateEquipmentNetwork();
            p.getStatModifiersManager().setRecalculate(true);

            world.execute(() -> {
                DuelStatsUtil.setHealth(p, 9999);
                DuelStatsUtil.setStamina(p, 9999);
            });
        });
    }


    private static void teleportOut(DuelSession s) {
        PlayerRef aRef = Universe.get().getPlayer(s.a);
        PlayerRef bRef = Universe.get().getPlayer(s.b);

        float pitch = 0.0f;
        float yawRad = (float) Math.toRadians(-90.0);
        float roll = 0.0f;

        Vector3f rot = new Vector3f(pitch, yawRad, roll);

        if (aRef != null) { 
        	TeleportUtil.teleport(aRef, 182.0, 122.0, 70.0, rot);
        	DuelLoadouts.clearInventory(resolveOnlinePlayer(aRef));
        }
        if (bRef != null) { 
        	TeleportUtil.teleport(bRef, 182.0, 122.0, 70.0, rot);
        	DuelLoadouts.clearInventory(resolveOnlinePlayer(bRef));
        
        }
    }

    private static Player resolveOnlinePlayer(PlayerRef ref) {
        if (ref == null || ref.getWorldUuid() == null) return null;
        return (Player) Universe.get().getWorld(ref.getWorldUuid()).getEntity(ref.getUuid());
    }


    /** Periodic cleanup for time limit. Call from a tick system or scheduler. */
    public static void checkExpiredDuels() {
        byPlayer.forEach((player, session) -> {
            if (session.isExpired()) {
                // End once (for whichever key hits first)
                end(player, EndReason.TIMEOUT);
            }
        });
    }

    private static void notifyEnded(DuelSession s, UUID endedBy, EndReason reason) {
        PlayerRef aRef = Universe.get().getPlayer(s.a);
        PlayerRef bRef = Universe.get().getPlayer(s.b);

        String aName = (aRef != null) ? aRef.getUsername() : "Player A";
        String bName = (bRef != null) ? bRef.getUsername() : "Player B";

        UUID winner = null;
        UUID loser = null;

        // Determine winner/loser (when possible)
        if (reason == EndReason.DEATH || reason == EndReason.FORFEIT) {
            // endedBy is the player who died / forfeited
            loser = endedBy;
            winner = endedBy.equals(s.a) ? s.b : s.a;
        }

        String baseMsg;
        switch (reason) {
            case TIMEOUT -> baseMsg = "The duel has ended (time limit reached).";
            case DEATH   -> baseMsg = "The duel has ended (a player died).";
            case FORFEIT -> baseMsg = "The duel has ended (forfeit).";
            default      -> baseMsg = "The duel has ended.";
        }

        String winnerMsg = "";
        if (winner != null && loser != null) {
            String winnerName = winner.equals(s.a) ? aName : bName;
            String loserName  = loser.equals(s.a)  ? aName : bName;

            winnerMsg = " Winner: " + winnerName + ". Loser: " + loserName;
        } else if (reason == EndReason.TIMEOUT) {
            winnerMsg = " Result: Draw";
        }

        String msg = baseMsg + winnerMsg;

        if (aRef != null) aRef.sendMessage(Message.raw(msg).bold(true).color(Color.RED));
        if (bRef != null) bRef.sendMessage(Message.raw(msg).bold(true).color(Color.RED));
    }

}
