package com.tobiasgraski.testplugin.utils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds pending duel requests in memory.
 * Keyed by targetUuid -> request info.
 */
public final class DuelRequests {

    private DuelRequests() {}

    public static final class PendingDuel {
        public final UUID senderUuid;
        public final String senderName;
        public final UUID targetUuid;

        public PendingDuel(UUID senderUuid, String senderName, UUID targetUuid) {
            this.senderUuid = senderUuid;
            this.senderName = senderName;
            this.targetUuid = targetUuid;
        }
    }

    private static final ConcurrentHashMap<UUID, PendingDuel> pendingByTarget = new ConcurrentHashMap<>();

    /** Create/replace the pending request for this target. */
    public static void put(UUID targetUuid, UUID senderUuid, String senderName) {
        pendingByTarget.put(targetUuid, new PendingDuel(senderUuid, senderName, targetUuid));
    }

    /** Get pending request for target (without removing). */
    public static PendingDuel get(UUID targetUuid) {
        return pendingByTarget.get(targetUuid);
    }

    /** Remove and return pending request for target (consume). */
    public static PendingDuel consume(UUID targetUuid) {
        return pendingByTarget.remove(targetUuid);
    }

    /** Remove without returning. */
    public static void clear(UUID targetUuid) {
        pendingByTarget.remove(targetUuid);
    }
}
