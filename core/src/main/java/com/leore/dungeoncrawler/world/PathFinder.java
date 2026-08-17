package com.leore.dungeoncrawler.world;

import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* simples (4 direções, sem diagonais) sobre a grade de tiles da dungeon.
 * Usado pelos inimigos pra perseguir o jogador desviando de paredes em vez de
 * andar em linha reta e ficar preso em quinas.
 */
public final class PathFinder {

    private static final int MAX_ITERATIONS = 2000;
    private static final int[] DX = {1, -1, 0, 0};
    private static final int[] DY = {0, 0, 1, -1};

    private PathFinder() {
    }

    /** Retorna os tiles do caminho (sem incluir o de partida), ou lista vazia se não há caminho. */
    public static List<GridPoint2> findPath(DungeonMap map, int startX, int startY, int goalX, int goalY) {
        if (!map.getTile(goalX, goalY).isWalkable()) {
            return new ArrayList<>();
        }
        if (startX == goalX && startY == goalY) {
            return new ArrayList<>();
        }

        Map<Long, Node> visited = new HashMap<>();
        Set<Long> closed = new HashSet<>();
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));

        Node start = new Node(startX, startY, null, 0, heuristic(startX, startY, goalX, goalY));
        open.add(start);
        visited.put(key(startX, startY), start);

        int iterations = 0;
        while (!open.isEmpty() && iterations++ < MAX_ITERATIONS) {
            Node current = open.poll();
            long currentKey = key(current.x, current.y);
            if (!closed.add(currentKey)) {
                continue;
            }

            if (current.x == goalX && current.y == goalY) {
                return buildPath(current);
            }

            for (int i = 0; i < 4; i++) {
                int nx = current.x + DX[i];
                int ny = current.y + DY[i];
                if (!map.getTile(nx, ny).isWalkable()) {
                    continue;
                }
                long nKey = key(nx, ny);
                if (closed.contains(nKey)) {
                    continue;
                }

                float g = current.g + 1;
                Node existing = visited.get(nKey);
                if (existing == null || g < existing.g) {
                    Node next = new Node(nx, ny, current, g, g + heuristic(nx, ny, goalX, goalY));
                    visited.put(nKey, next);
                    open.add(next);
                }
            }
        }

        return new ArrayList<>(); // sem caminho encontrado (ou estourou o limite de segurança)
    }

    private static List<GridPoint2> buildPath(Node node) {
        LinkedList<GridPoint2> path = new LinkedList<>();
        Node current = node;
        while (current != null && current.parent != null) {
            path.addFirst(new GridPoint2(current.x, current.y));
            current = current.parent;
        }
        return path;
    }

    private static float heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }

    private static final class Node {
        final int x, y;
        final Node parent;
        final float g;
        final float f;

        Node(int x, int y, Node parent, float g, float f) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }
    }
}
