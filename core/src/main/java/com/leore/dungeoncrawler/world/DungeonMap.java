package com.leore.dungeoncrawler.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.leore.dungeoncrawler.graphics.Assets;

/** Grade de tiles da dungeon. Sabe desenhar a si mesma com névoa de guerra e responder colisão. */
public class DungeonMap {

    public static final float TILE_SIZE = 32f;
    private static final float DIM_FACTOR = 0.35f;

    private final int width;
    private final int height;
    private final Tile[][] tiles;
    private final boolean[][] explored;

    public DungeonMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        this.explored = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = Tile.WALL;
            }
        }
    }

    public void setTile(int x, int y, Tile tile) {
        if (isInBounds(x, y)) {
            tiles[x][y] = tile;
        }
    }

    public Tile getTile(int x, int y) {
        return isInBounds(x, y) ? tiles[x][y] : Tile.WALL;
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isWalkableWorld(float worldX, float worldY) {
        int tx = (int) (worldX / TILE_SIZE);
        int ty = (int) (worldY / TILE_SIZE);
        return getTile(tx, ty).isWalkable();
    }

    /** Marca como explorados os tiles num raio (em tiles) ao redor de um ponto do mundo. */
    public void revealAround(float worldX, float worldY, float radiusTiles) {
        int cx = (int) (worldX / TILE_SIZE);
        int cy = (int) (worldY / TILE_SIZE);
        int r = (int) Math.ceil(radiusTiles);
        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                if (isInBounds(x, y) && distanceInTiles(x, y, cx, cy) <= radiusTiles) {
                    explored[x][y] = true;
                }
            }
        }
    }

    public boolean isExplored(int x, int y) {
        return isInBounds(x, y) && explored[x][y];
    }

    /** Um ponto do mundo está dentro do raio de visão atual (não só já explorado)? */
    public boolean isCurrentlyVisible(float worldX, float worldY, float viewerWorldX, float viewerWorldY, float radiusTiles) {
        float dx = (worldX - viewerWorldX) / TILE_SIZE;
        float dy = (worldY - viewerWorldY) / TILE_SIZE;
        return Math.sqrt(dx * dx + dy * dy) <= radiusTiles;
    }

    private double distanceInTiles(int x, int y, int cx, int cy) {
        double dx = x - cx;
        double dy = y - cy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void render(SpriteBatch batch, float viewerX, float viewerY, float visibleRadiusTiles) {
        batch.begin();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!explored[x][y]) {
                    continue;
                }

                float tileCenterX = x * TILE_SIZE + TILE_SIZE / 2f;
                float tileCenterY = y * TILE_SIZE + TILE_SIZE / 2f;
                boolean lit = isCurrentlyVisible(tileCenterX, tileCenterY, viewerX, viewerY, visibleRadiusTiles);

                batch.setColor(1f, 1f, 1f, lit ? 1f : DIM_FACTOR);
                batch.draw(regionFor(x, y), x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    /** Desenha um minimapa em espaço de tela: tiles explorados como pontinhos, jogador em destaque. */
    public void renderMinimap(ShapeRenderer renderer, float originX, float originY, float scale,
                               float playerWorldX, float playerWorldY) {
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderer.setColor(0f, 0f, 0f, 0.5f);
        renderer.rect(originX, originY, width * scale, height * scale);

        renderer.setColor(0.75f, 0.75f, 0.8f, 0.9f);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (explored[x][y] && tiles[x][y].isWalkable()) {
                    renderer.rect(originX + x * scale, originY + y * scale, scale, scale);
                }
            }
        }

        int playerTileX = (int) (playerWorldX / TILE_SIZE);
        int playerTileY = (int) (playerWorldY / TILE_SIZE);
        renderer.setColor(1f, 0.9f, 0.2f, 1f);
        renderer.rect(originX + playerTileX * scale - 1f, originY + playerTileY * scale - 1f,
            scale + 2f, scale + 2f);

        renderer.end();
    }

    private TextureRegion regionFor(int x, int y) {
        switch (tiles[x][y]) {
            case WALL:
                return wallRegionFor(x, y);
            case STAIRS_DOWN:
                return Assets.stairsTile;
            default:
                int index = Math.floorMod(x * 31 + y * 17, Assets.floorTiles.length);
                return Assets.floorTiles[index];
        }
    }

    /**
     * Escolhe a peça de parede olhando os vizinhos: se tem chão embaixo, é a "frente"
     * decorada da parede (o que o jogador normalmente vê); senão é só o topo plano.
     * Em ambos os casos, checa esquerda/direita pra pegar a peça de canto certa.
     */
    private TextureRegion wallRegionFor(int x, int y) {
        boolean floorBelow = getTile(x, y - 1).isWalkable();
        boolean floorLeft = getTile(x - 1, y).isWalkable();
        boolean floorRight = getTile(x + 1, y).isWalkable();

        if (floorBelow) {
            if (floorLeft) {
                return Assets.wallTopLeft;
            }
            if (floorRight) {
                return Assets.wallTopRight;
            }
            return Assets.wallTopMid;
        }

        if (floorLeft) {
            return Assets.wallLeft;
        }
        if (floorRight) {
            return Assets.wallRight;
        }
        return Assets.wallMid;
    }
}
