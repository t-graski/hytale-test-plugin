package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.math.vector.Vector3f;

public final class Arena {

    private final int id;
    private final Vector3f spawnA;
    private final Vector3f spawnB;

    public Arena(int id, Vector3f spawnA, Vector3f spawnB) {
        this.id = id;
        this.spawnA = spawnA;
        this.spawnB = spawnB;
    }

    public int getId() {
        return id;
    }

    public Vector3f getSpawnA() {
        return spawnA;
    }

    public Vector3f getSpawnB() {
        return spawnB;
    }

    /**
     * Convenience helpers if you want to pick the spawn by "slot"
     * slot 0 = A spawn, slot 1 = B spawn
     */
    public Vector3f getSpawnForSlot(int slot) {
        return (slot == 0) ? spawnA : spawnB;
    }

    /**
     * Returns the opponent spawn for the given slot.
     * slot 0 -> opponent is spawnB
     * slot 1 -> opponent is spawnA
     */
    public Vector3f getOpponentSpawnForSlot(int slot) {
        return (slot == 0) ? spawnB : spawnA;
    }

    @Override
    public String toString() {
        return "Arena{id=" + id + ", spawnA=" + spawnA + ", spawnB=" + spawnB + "}";
    }
}
