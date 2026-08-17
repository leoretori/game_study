package com.leore.dungeoncrawler.world;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Gera uma dungeon simples: várias salas retangulares espalhadas aleatoriamente
 * e conectadas em sequência por corredores em "L", mais algumas conexões extras
 * aleatórias entre salas não-vizinhas pra criar atalhos/loops em vez de um único
 * corredor linear. Fácil de entender e uma boa base pra evoluir depois (BSP, etc.).
 */
public final class DungeonGenerator {

    private static final int MAX_ATTEMPTS = 200;
    private static final int ROOM_MARGIN = 1;
    private static final float EXTRA_CONNECTION_RATIO = 0.3f;

    private DungeonGenerator() {
    }

    public static class Result {
        public final DungeonMap map;
        public final List<Room> rooms;

        Result(DungeonMap map, List<Room> rooms) {
            this.map = map;
            this.rooms = rooms;
        }
    }

    public static Result generate(int mapWidth, int mapHeight, int roomCount,
                                   int minRoomSize, int maxRoomSize) {
        DungeonMap map = new DungeonMap(mapWidth, mapHeight);
        List<Room> rooms = new ArrayList<>();

        int attempts = 0;
        while (rooms.size() < roomCount && attempts < MAX_ATTEMPTS) {
            attempts++;
            int w = MathUtils.random(minRoomSize, maxRoomSize);
            int h = MathUtils.random(minRoomSize, maxRoomSize);
            int x = MathUtils.random(1, mapWidth - w - 1);
            int y = MathUtils.random(1, mapHeight - h - 1);
            Room candidate = new Room(x, y, w, h);

            if (overlapsAny(candidate, rooms)) {
                continue;
            }

            carveRoom(map, candidate);
            if (!rooms.isEmpty()) {
                Room previous = rooms.get(rooms.size() - 1);
                carveCorridor(map, previous.centerX(), previous.centerY(),
                    candidate.centerX(), candidate.centerY());
            }
            rooms.add(candidate);
        }

        addExtraConnections(map, rooms);
        placeStairs(map, rooms);

        return new Result(map, rooms);
    }

    /**
     * Além da cadeia linear (que já garante que dá pra chegar em qualquer sala),
     * cava algumas conexões extras entre pares de salas aleatórias. Isso cria
     * atalhos e loops, então a dungeon não fica só um corredor único serpenteando.
     */
    private static void addExtraConnections(DungeonMap map, List<Room> rooms) {
        if (rooms.size() < 3) {
            return;
        }
        int extraConnections = Math.round(rooms.size() * EXTRA_CONNECTION_RATIO);
        for (int i = 0; i < extraConnections; i++) {
            Room a = rooms.get(MathUtils.random(rooms.size() - 1));
            Room b = rooms.get(MathUtils.random(rooms.size() - 1));
            if (a == b) {
                continue;
            }
            carveCorridor(map, a.centerX(), a.centerY(), b.centerX(), b.centerY());
        }
    }

    /** Escada para o próximo andar: fica na última sala gerada, a mais "longe" do início. */
    private static void placeStairs(DungeonMap map, List<Room> rooms) {
        if (rooms.isEmpty()) {
            return;
        }
        Room stairsRoom = rooms.get(rooms.size() - 1);
        map.setTile(stairsRoom.centerX(), stairsRoom.centerY(), Tile.STAIRS_DOWN);
    }

    private static boolean overlapsAny(Room candidate, List<Room> rooms) {
        for (Room room : rooms) {
            if (candidate.overlaps(room, ROOM_MARGIN)) {
                return true;
            }
        }
        return false;
    }

    private static void carveRoom(DungeonMap map, Room room) {
        for (int x = room.x; x < room.x + room.width; x++) {
            for (int y = room.y; y < room.y + room.height; y++) {
                map.setTile(x, y, Tile.FLOOR);
            }
        }
    }

    /** Cava um corredor em L: primeiro no eixo X, depois no eixo Y. */
    private static void carveCorridor(DungeonMap map, int startX, int startY, int endX, int endY) {
        int x = startX;
        int y = startY;

        while (x != endX) {
            map.setTile(x, y, Tile.CORRIDOR);
            x += Integer.signum(endX - x);
        }
        while (y != endY) {
            map.setTile(x, y, Tile.CORRIDOR);
            y += Integer.signum(endY - y);
        }
        map.setTile(endX, endY, Tile.CORRIDOR);
    }
}
