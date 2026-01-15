package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds pending duel requests in memory. Keyed by targetUuid -> request info.
 */
public final class DuelRequests {

	private static final ConcurrentHashMap<UUID, PendingDuel> pendingByTarget = new ConcurrentHashMap<>();
	public static final long EXPIRATION_TIME_MS = 3_000;

	public static final class PendingDuel {
		public final UUID senderUuid;
		public final String senderName;
		public final UUID targetUuid;
		public final long timestamp;

		public PendingDuel(UUID senderUuid, String senderName, UUID targetUuid) {
			this.senderUuid = senderUuid;
			this.senderName = senderName;
			this.targetUuid = targetUuid;
			this.timestamp = System.currentTimeMillis();

		}

		public boolean isExpired() {
			return System.currentTimeMillis() - timestamp > EXPIRATION_TIME_MS;
		}
	}

	private DuelRequests() {
	}

	/**
	 * Create/replace the pending request for this target.
	 */
	public static void put(UUID targetUuid, UUID senderUuid, String senderName) {
		pendingByTarget.put(targetUuid, new PendingDuel(senderUuid, senderName, targetUuid));
	}

	/**
	 * Get pending request for target (without removing).
	 */
	public static PendingDuel get(UUID targetUuid) {
		return pendingByTarget.get(targetUuid);
	}

	/**
	 * Remove and return pending request for target (consume).
	 */
	public static PendingDuel consume(UUID targetUuid) {
		var pending = pendingByTarget.remove(targetUuid);

		if (pending != null && pending.isExpired()) {
			return null;
		}

		return pending;
	}

	/**
	 * Remove without returning.
	 */
	public static void clear(UUID targetUuid) {
		pendingByTarget.remove(targetUuid);
	}

	public static void checkExpiredRequests() {
		pendingByTarget.entrySet().removeIf(entry -> {
			var pending = entry.getValue();

			if (pending.isExpired()) {
				notifySenderExpired(pending);
				return true;
			}

			return false;
		});
	}

	private static void notifySenderExpired(PendingDuel pending) {
		var senderRef = Universe.get().getPlayer(pending.senderUuid);
		var targetRef = Universe.get().getPlayer(pending.targetUuid);

		if (senderRef != null) {
			senderRef.sendMessage(Message.raw("Your duel request to " + targetRef.getUsername() + " has expired.")
					.bold(true).color(Color.RED));

			targetRef.sendMessage(Message.raw("The duel request from " + senderRef.getUsername() + " has expired.")
					.bold(true).color(Color.RED));
		}
	}
}
