package com.leore.dungeoncrawler.world;

/** Retângulo de sala em coordenadas de tile, usado apenas durante a geração da dungeon. */
public class Room {
    public final int x, y, width, height;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int centerX() {
        return x + width / 2;
    }

    public int centerY() {
        return y + height / 2;
    }

    /** Verifica sobreposição incluindo uma margem, para as salas não ficarem coladas. */
    public boolean overlaps(Room other, int margin) {
        return x - margin < other.x + other.width
            && x + width + margin > other.x
            && y - margin < other.y + other.height
            && y + height + margin > other.y;
    }
}
