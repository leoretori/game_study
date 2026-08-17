package com.leore.dungeoncrawler.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.leore.dungeoncrawler.GameStats;
import com.leore.dungeoncrawler.entity.Chest;
import com.leore.dungeoncrawler.entity.Enemy;
import com.leore.dungeoncrawler.entity.EnemyType;
import com.leore.dungeoncrawler.entity.GoldCoin;
import com.leore.dungeoncrawler.entity.HealthPotion;
import com.leore.dungeoncrawler.entity.Item;
import com.leore.dungeoncrawler.entity.Merchant;
import com.leore.dungeoncrawler.entity.Perk;
import com.leore.dungeoncrawler.entity.Player;
import com.leore.dungeoncrawler.entity.WeaponPickup;
import com.leore.dungeoncrawler.entity.WeaponType;
import com.leore.dungeoncrawler.entity.WorldObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Dono do estado de uma "run": o mapa do andar atual, o jogador e os inimigos/itens nele.
 * Sabe gerar um andar novo, avançar pra escada, atualizar a simulação e desenhar tudo.
 * A GameScreen só cuida do ciclo de vida do LibGDX e delega a lógica pra cá.
 */
public class World {

    private static final int MAP_WIDTH_TILES = 60;
    private static final int MAP_HEIGHT_TILES = 40;
    private static final int ROOM_COUNT = 10;
    private static final int BASE_MAX_ENEMIES_PER_ROOM = 3;
    private static final float VISIBILITY_RADIUS_TILES = 6f;

    public static final int FINAL_FLOOR = 5;
    private static final float MERCHANT_INTERACT_RANGE = 44f;
    private static final int PRICE_POTION = 20;
    private static final int PRICE_MAX_HEALTH = 35;
    private static final int PRICE_ATTACK_DAMAGE = 50;
    private static final int PRICE_FULL_HEAL = 15;
    private static final int MAX_HEALTH_BONUS = 20;
    private static final int ATTACK_DAMAGE_BONUS = 5;

    private static final float CHEST_SPAWN_CHANCE = 0.25f;
    private static final float MIMIC_CHANCE = 0.2f;
    private static final int CHEST_GOLD_MIN = 25;
    private static final int CHEST_GOLD_MAX = 45;

    private DungeonMap map;
    private Player player;
    private int floor;
    private boolean victory = false;
    private boolean nearMerchant = false;
    private boolean statsRecorded = false;
    private boolean lastRunWasRecord = false;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<Chest> chests = new ArrayList<>();
    private Merchant merchant;
    private Enemy boss;

    public World() {
        restart();
    }

    /** Recomeça a run do zero (andar 1, jogador novo). Usado depois que o jogador morre. */
    public final void restart() {
        floor = 1;
        victory = false;
        statsRecorded = false;
        lastRunWasRecord = false;
        generateFloor(null);
    }

    private void descend() {
        floor++;
        generateFloor(player);
    }

    private void generateFloor(Player existingPlayer) {
        DungeonGenerator.Result dungeon = DungeonGenerator.generate(
            MAP_WIDTH_TILES, MAP_HEIGHT_TILES, ROOM_COUNT, /* minRoomSize */ 5, /* maxRoomSize */ 10);
        map = dungeon.map;
        enemies.clear();
        items.clear();
        chests.clear();
        boss = null;

        Room spawnRoom = dungeon.rooms.get(0);
        float spawnX = spawnRoom.centerX() * DungeonMap.TILE_SIZE;
        float spawnY = spawnRoom.centerY() * DungeonMap.TILE_SIZE;

        if (existingPlayer == null) {
            player = new Player(spawnX, spawnY, map);
        } else {
            player = existingPlayer;
            player.setMap(map);
            player.teleportTo(spawnX, spawnY);
        }

        spawnMerchant(spawnRoom);

        if (floor == FINAL_FLOOR) {
            spawnBoss(dungeon.rooms);
        } else {
            spawnEnemies(dungeon.rooms);
        }
        spawnItems(dungeon.rooms);
        spawnChests(dungeon.rooms);

        map.revealAround(spawnX, spawnY, VISIBILITY_RADIUS_TILES);
    }

    private void spawnMerchant(Room spawnRoom) {
        float mx = (spawnRoom.x + 1) * DungeonMap.TILE_SIZE;
        float my = (spawnRoom.y + 1) * DungeonMap.TILE_SIZE;
        merchant = new Merchant(mx, my);
    }

    private void spawnBoss(List<Room> rooms) {
        Room bossRoom = rooms.get(rooms.size() - 1);
        float bx = bossRoom.centerX() * DungeonMap.TILE_SIZE;
        float by = bossRoom.centerY() * DungeonMap.TILE_SIZE;
        boss = new Enemy(EnemyType.BOSS, bx, by, map);
        enemies.add(boss);
    }

    private void spawnEnemies(List<Room> rooms) {
        int maxPerRoom = BASE_MAX_ENEMIES_PER_ROOM + (floor - 1) / 2;
        // Sala 0 é a sala do jogador: fica livre de inimigos.
        for (int i = 1; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            int enemyCount = MathUtils.random(0, maxPerRoom);
            for (int e = 0; e < enemyCount; e++) {
                Vector2 pos = randomPointInRoom(room);
                enemies.add(new Enemy(randomEnemyType(), pos.x, pos.y, map));
            }
        }
    }

    private EnemyType randomEnemyType() {
        float roll = MathUtils.random();
        if (floor >= 3 && roll < 0.12f) {
            return EnemyType.BRUTE;
        }
        if (floor >= 2 && roll < 0.30f) {
            return EnemyType.MASKED_ORC;
        }
        if (roll < 0.55f) {
            return EnemyType.SKELETON;
        }
        if (roll < 0.78f) {
            return EnemyType.GOBLIN;
        }
        return EnemyType.SLIME;
    }

    private void spawnItems(List<Room> rooms) {
        for (int i = 1; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if (MathUtils.randomBoolean(0.35f)) {
                Vector2 pos = randomPointInRoom(room);
                items.add(new HealthPotion(pos.x, pos.y));
            }
            if (MathUtils.randomBoolean(0.5f)) {
                Vector2 pos = randomPointInRoom(room);
                items.add(new GoldCoin(pos.x, pos.y));
            }
            if (MathUtils.randomBoolean(0.12f)) {
                Vector2 pos = randomPointInRoom(room);
                WeaponType[] weapons = WeaponType.values();
                items.add(new WeaponPickup(pos.x, pos.y, weapons[MathUtils.random(weapons.length - 1)]));
            }
        }
    }

    private void spawnChests(List<Room> rooms) {
        for (int i = 1; i < rooms.size(); i++) {
            if (MathUtils.randomBoolean(CHEST_SPAWN_CHANCE)) {
                Vector2 pos = randomPointInRoom(rooms.get(i));
                chests.add(new Chest(pos.x, pos.y));
            }
        }
    }

    private Vector2 randomPointInRoom(Room room) {
        float px = (room.x + MathUtils.random(1, Math.max(1, room.width - 2))) * DungeonMap.TILE_SIZE;
        float py = (room.y + MathUtils.random(1, Math.max(1, room.height - 2))) * DungeonMap.TILE_SIZE;
        return new Vector2(px, py);
    }

    public void update(float delta) {
        if (!player.isAlive()) {
            recordStatsOnce();
            return;
        }

        // Checado antes de "victory": o golpe que derruba o chefe pode render XP suficiente
        // pra subir de nível no mesmo frame, e essa escolha precisa continuar respondendo
        // a input mesmo depois da vitória ser marcada (senão trava esperando 1/2/3 pra sempre).
        if (player.hasPendingPerkChoice()) {
            handlePerkChoiceInput();
            return;
        }

        if (victory) {
            recordStatsOnce();
            return;
        }

        player.update(delta);
        for (Enemy enemy : enemies) {
            enemy.update(delta, player);
        }
        for (Item item : items) {
            item.update(delta);
        }
        if (merchant != null) {
            merchant.update(delta);
        }

        if (player.tryAttack()) {
            resolvePlayerAttack();
        }
        player.tryUsePotion();

        awardXpForDeadEnemies();
        if (floor == FINAL_FLOOR && boss != null && !boss.isAlive()) {
            victory = true;
        }
        enemies.removeIf(enemy -> !enemy.isAlive());

        checkItemPickups();
        checkChestInteractions();
        map.revealAround(player.getCenterX(), player.getCenterY(), VISIBILITY_RADIUS_TILES);
        handleMerchant();
        checkStairs();
    }

    private void recordStatsOnce() {
        if (statsRecorded) {
            return;
        }
        lastRunWasRecord = GameStats.recordRunEnd(floor, victory);
        statsRecorded = true;
    }

    public boolean isLastRunNewRecord() {
        return lastRunWasRecord;
    }

    private void handlePerkChoiceInput() {
        List<Perk> choices = player.getCurrentPerkChoices();
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && choices.size() > 0) {
            player.choosePerk(choices.get(0));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && choices.size() > 1) {
            player.choosePerk(choices.get(1));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && choices.size() > 2) {
            player.choosePerk(choices.get(2));
        }
    }

    private void resolvePlayerAttack() {
        int damage = player.getAttackDamage();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && player.distanceTo(enemy) <= player.getAttackRange()) {
                enemy.takeDamage(damage);
                player.healFromLifesteal(damage);
            }
        }
    }

    private void awardXpForDeadEnemies() {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                player.addXp(enemy.getXpReward());
                player.recordKill();
            }
        }
    }

    private void checkItemPickups() {
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            if (player.distanceTo(item) <= (player.getSize() + item.getSize()) / 2f) {
                item.onPickup(player);
                it.remove();
            }
        }
    }

    /** A maioria dos baús dá recompensa; uma fração vira um mimic que ataca de surpresa. */
    private void checkChestInteractions() {
        Iterator<Chest> it = chests.iterator();
        while (it.hasNext()) {
            Chest chest = it.next();
            if (player.distanceTo(chest) > (player.getSize() + chest.getSize()) / 2f) {
                continue;
            }
            it.remove();

            if (MathUtils.randomBoolean(MIMIC_CHANCE)) {
                enemies.add(new Enemy(EnemyType.MIMIC, chest.getX(), chest.getY(), map));
            } else {
                player.addGold(MathUtils.random(CHEST_GOLD_MIN, CHEST_GOLD_MAX));
                if (MathUtils.randomBoolean(0.4f)) {
                    player.addPotion();
                }
            }
        }
    }

    private void handleMerchant() {
        nearMerchant = merchant != null && player.distanceTo(merchant) <= MERCHANT_INTERACT_RANGE;
        if (!nearMerchant) {
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && player.trySpendGold(PRICE_POTION)) {
            player.addPotion();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && player.trySpendGold(PRICE_MAX_HEALTH)) {
            player.upgradeMaxHealth(MAX_HEALTH_BONUS);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && player.trySpendGold(PRICE_ATTACK_DAMAGE)) {
            player.upgradeAttackDamage(ATTACK_DAMAGE_BONUS);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) && player.trySpendGold(PRICE_FULL_HEAL)) {
            player.healToFull();
        }
    }

    private void checkStairs() {
        if (floor == FINAL_FLOOR) {
            return; // não tem próximo andar: precisa vencer o chefe
        }
        int tileX = (int) (player.getCenterX() / DungeonMap.TILE_SIZE);
        int tileY = (int) (player.getCenterY() / DungeonMap.TILE_SIZE);
        if (map.getTile(tileX, tileY) == Tile.STAIRS_DOWN) {
            descend();
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        map.render(batch, player.getCenterX(), player.getCenterY(), VISIBILITY_RADIUS_TILES);

        batch.begin();
        if (merchant != null && map.isExplored(tileXOf(merchant), tileYOf(merchant))) {
            merchant.render(batch);
        }
        for (Item item : items) {
            if (map.isExplored(tileXOf(item), tileYOf(item))) {
                item.render(batch);
            }
        }
        for (Chest chest : chests) {
            if (map.isExplored(tileXOf(chest), tileYOf(chest))) {
                chest.render(batch);
            }
        }
        for (Enemy enemy : enemies) {
            if (isEnemyVisible(enemy)) {
                enemy.render(batch);
            }
        }
        player.render(batch);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy enemy : enemies) {
            if (isEnemyVisible(enemy)) {
                enemy.renderHealthBar(shapeRenderer);
            }
        }
        shapeRenderer.end();

        if (player.isAttackAnimating()) {
            renderAttackRange(shapeRenderer);
        }
        if (boss != null && boss.isSlamTelegraphing()) {
            renderBossSlamWarning(shapeRenderer);
        }
    }

    private void renderBossSlamWarning(ShapeRenderer renderer) {
        renderer.begin(ShapeRenderer.ShapeType.Line);
        boss.renderSlamWarning(renderer);
        renderer.end();
    }

    private boolean isEnemyVisible(Enemy enemy) {
        return map.isCurrentlyVisible(enemy.getCenterX(), enemy.getCenterY(),
            player.getCenterX(), player.getCenterY(), VISIBILITY_RADIUS_TILES);
    }

    private int tileXOf(WorldObject object) {
        return (int) (object.getCenterX() / DungeonMap.TILE_SIZE);
    }

    private int tileYOf(WorldObject object) {
        return (int) (object.getCenterY() / DungeonMap.TILE_SIZE);
    }

    private void renderAttackRange(ShapeRenderer renderer) {
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(1f, 1f, 1f, 0.6f);
        renderer.circle(player.getCenterX(), player.getCenterY(), player.getAttackRange(), 24);
        renderer.end();
    }

    public void renderMinimap(ShapeRenderer renderer, float originX, float originY, float scale) {
        map.renderMinimap(renderer, originX, originY, scale, player.getCenterX(), player.getCenterY());
    }

    public int getMapWidth() {
        return map.getWidth();
    }

    public int getMapHeight() {
        return map.getHeight();
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public int getFloor() {
        return floor;
    }

    public boolean isVictory() {
        return victory;
    }

    public boolean isBossFloor() {
        return floor == FINAL_FLOOR;
    }

    public boolean isNearMerchant() {
        return nearMerchant;
    }
}
