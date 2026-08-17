package com.leore.dungeoncrawler.world;

/** Cada tipo de piso/parede da dungeon, e se é possível andar sobre ele. */
public enum Tile {
    WALL(false),
    FLOOR(true),
    CORRIDOR(true),
    STAIRS_DOWN(true);

    private final boolean walkable;

    Tile(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isWalkable() {
        return walkable;
    }
}
