package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActiveDuels {

    public static final long DUEL_TIME_LIMIT_MS = 5 * 60_000; // 5 minutes

    // key: any player in duel -> session
    private static final ConcurrentHashMap<UUID, DuelSession> byPlayer = new ConcurrentHashMap<>();

    // key: arenaId -> session (occupied)
    private static final ConcurrentHashMap<Integer, DuelSession> arenaOccupancy = new ConcurrentHashMap<>();
    
    private static float yawToFace(double fromX, double fromZ, double toX, double toZ) {
        double dx = toX - fromX;
        double dz = toZ - fromZ;

        return (float) Math.atan2(-dx, -dz); // radians
    }

    private static final Arena[] ARENAS = new Arena[] {
        new Arena(0, new Vector3f(14, 99, 0),     new Vector3f(-14, 100, 0)),
        new Arena(1, new Vector3f(-10, 100, -81), new Vector3f(18, 101, -81)),
        new Arena(2, new Vector3f(163, 97, -30),  new Vector3f(135, 98, -30)),
        new Arena(3, new Vector3f(122, 99, 91),   new Vector3f(94, 100, 91)),
    };

    public enum EndReason { DEATH, TIMEOUT, FORFEIT }

    public static final class DuelSession {
        public final UUID a;
        public final UUID b;
        public final long startMs;
        public final int arenaId;

        public DuelSession(UUID a, UUID b, int arenaId) {
            this.a = a;
            this.b = b;
            this.arenaId = arenaId;
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
    
    public static boolean areOpponents(UUID attacker, UUID victim) {
        DuelSession s = byPlayer.get(attacker);
        return s != null && s.involves(victim);
    }


    public static DuelSession get(UUID player) {
        return byPlayer.get(player);
    }

    /** Returns null if no arenas are free. */
    private static Arena tryClaimFreeArena(DuelSession s) {
        for (Arena a : ARENAS) {
            if (arenaOccupancy.putIfAbsent(a.getId(), s) == null) {
                return a; // successfully claimed
            }
        }
        return null;
    }

    private static void releaseArena(DuelSession s) {
        if (s == null) return;
        arenaOccupancy.remove(s.arenaId, s);
    }

    private static Arena arenaFor(int arenaId) {
        for (Arena a : ARENAS) {
            if (a.getId() == arenaId) return a;
        }
        return null;
    }

    /**
     * Start duel. Returns true if started, false if:
     * - either player is already in a duel, OR
     * - no arena is currently free.
     */
    public static boolean start(UUID a, UUID b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return false;

        // Don’t allow starting if either is already in a duel
        if (isInDuel(a) || isInDuel(b)) {
        	return false;
        }

        
        // Claim an arena atomically
        DuelSession placeholder = new DuelSession(a, b, -1);
        Arena arena = tryClaimFreeArena(placeholder);
        if (arena == null) {
            PlayerRef aRef = Universe.get().getPlayer(a);
            PlayerRef bRef = Universe.get().getPlayer(b);

            String msg = "No arenas are currently available. Please try again in a moment.";

            if (aRef != null) aRef.sendMessage(Message.raw(msg).bold(true).color(Color.RED));
            if (bRef != null) bRef.sendMessage(Message.raw(msg).bold(true).color(Color.RED));
            return false;
        }

        DuelSession session = new DuelSession(a, b, arena.getId());

        // Swap occupancy mapping placeholder -> real session (best-effort)
        arenaOccupancy.replace(arena.getId(), placeholder, session);

        byPlayer.put(a, session);
        byPlayer.put(b, session);

        // Teleport players into the arena (you can also apply kit/loadout here)
        teleportIntoArena(session);

        return true;
    }

    /**
     * End duel by any participant. Returns the ended session (or null if not in duel).
     * Removes both players from registry exactly once, and frees arena.
     */
    public static DuelSession end(UUID participant, EndReason reason) {
        DuelSession session = byPlayer.remove(participant);
        if (session == null) return null;

        // Remove the other player only if it still maps to the same session
        byPlayer.remove(session.other(participant), session);

        // Free the arena first so new duels can start immediately
        releaseArena(session);

        // Teleport both players out to lobby with fixed orientation (0, -90, 0 degrees)
        teleportOut(session);

        notifyEnded(session, participant, reason);

        healOut(session);

        return session;
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

    private static void teleportIntoArena(DuelSession s) {
        Arena arena = arenaFor(s.arenaId);
        if (arena == null) return;

        PlayerRef aRef = Universe.get().getPlayer(s.a);
        PlayerRef bRef = Universe.get().getPlayer(s.b);

        Vector3f aSpawn = arena.getSpawnA();
        Vector3f bSpawn = arena.getSpawnB();

        // yaw for each to look at the other (same as your previous behavior)
        float aYaw = yawToFace(aSpawn.x, aSpawn.z, bSpawn.x, bSpawn.z);
        float bYaw = yawToFace(bSpawn.x, bSpawn.z, aSpawn.x, aSpawn.z);

        Vector3f rotA = new Vector3f(0.0f, aYaw, 0.0f);
        Vector3f rotB = new Vector3f(0.0f, bYaw, 0.0f);

        if (aRef != null) {
            TeleportUtil.teleport(aRef, aSpawn.x, aSpawn.y, aSpawn.z, rotA);
        }
        if (bRef != null) {
            TeleportUtil.teleport(bRef, bSpawn.x, bSpawn.y, bSpawn.z, rotB);
        }
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
            TeleportUtil.teleport(aRef, 108, 100, 91, rot);
            DuelLoadouts.clearInventory(resolveOnlinePlayer(aRef));
        }
        if (bRef != null) {
            TeleportUtil.teleport(bRef, 108, 100, 91, rot);
            DuelLoadouts.clearInventory(resolveOnlinePlayer(bRef));
        }
    }

    private static Player resolveOnlinePlayer(PlayerRef ref) {
        if (ref == null || ref.getWorldUuid() == null) return null;
        return (Player) Universe.get().getWorld(ref.getWorldUuid()).getEntity(ref.getUuid());
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
