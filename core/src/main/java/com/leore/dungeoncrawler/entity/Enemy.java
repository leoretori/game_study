package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.leore.dungeoncrawler.graphics.Assets;
import com.leore.dungeoncrawler.world.DungeonMap;
import com.leore.dungeoncrawler.world.PathFinder;

import java.util.List;

/** Inimigo com IA simples: fica parado até o jogador chegar perto, então persegue (com A*) e ataca. */
public class Enemy extends Entity {

    private enum State { IDLE, CHASING }

    private static final float ATTACK_RANGE = 26f;
    private static final float ATTACK_COOLDOWN = 1f;
    private static final float BAR_HEIGHT = 4f;
    private static final float REPATH_INTERVAL = 0.4f;
    private static final float WAYPOINT_TOLERANCE = 4f;

    private static final float SLAM_RANGE = 80f;
    private static final float SLAM_COOLDOWN = 4.5f;
    private static final float SLAM_DAMAGE_MULTIPLIER = 1.5f;
    private static final float SLAM_TELEGRAPH_DURATION = 0.4f;

    private final DungeonMap map;
    private final EnemyType type;
    private State state = State.IDLE;
    private float attackCooldownTimer = 0f;
    private float slamCooldownTimer = 0f;
    private float slamTelegraphTimer = 0f;

    private List<GridPoint2> path = List.of();
    private int pathIndex = 0;
    private float repathTimer = 0f;

    public Enemy(EnemyType type, float x, float y, DungeonMap map) {
        super(x, y, type.size, type.maxHealth);
        this.type = type;
        this.map = map;
    }

    public void update(float delta, Player player) {
        tickTimers(delta);
        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= delta;
        }
        if (slamCooldownTimer > 0f) {
            slamCooldownTimer -= delta;
        }

        float distance = distanceTo(player);
        state = distance <= type.detectionRadius ? State.CHASING : State.IDLE;

        if (state != State.CHASING) {
            return;
        }

        // Durante o "wind-up" do slam o chefe fica parado, telegrafando o golpe;
        // o jogador tem essa janela pra sair do alcance e escapar do dano maior.
        if (slamTelegraphTimer > 0f) {
            slamTelegraphTimer -= delta;
            facingLeft = player.getCenterX() < getCenterX();
            if (slamTelegraphTimer <= 0f && distanceTo(player) <= SLAM_RANGE) {
                player.takeDamage(Math.round(type.attackDamage * SLAM_DAMAGE_MULTIPLIER));
            }
            return;
        }

        if (type.hasSlamAttack && slamCooldownTimer <= 0f && distance <= SLAM_RANGE) {
            slamTelegraphTimer = SLAM_TELEGRAPH_DURATION;
            slamCooldownTimer = SLAM_COOLDOWN;
            return;
        }

        if (distance > ATTACK_RANGE) {
            chase(player, delta);
        } else {
            facingLeft = player.getCenterX() < getCenterX();
            if (attackCooldownTimer <= 0f) {
                player.takeDamage(type.attackDamage);
                attackCooldownTimer = ATTACK_COOLDOWN;
            }
        }
    }

    public boolean isSlamTelegraphing() {
        return slamTelegraphTimer > 0f;
    }

    /** Desenha o anel de aviso do slam enquanto ele está "carregando" o golpe. */
    public void renderSlamWarning(ShapeRenderer renderer) {
        renderer.setColor(1f, 0.3f, 0.1f, 0.75f);
        renderer.circle(getCenterX(), getCenterY(), SLAM_RANGE, 28);
    }

    /** Segue um caminho calculado por A*, recalculando periodicamente em vez de todo frame. */
    private void chase(Player player, float delta) {
        repathTimer -= delta;
        if (repathTimer <= 0f) {
            repathTimer = REPATH_INTERVAL;
            recomputePath(player);
        }

        if (pathIndex >= path.size()) {
            return;
        }

        GridPoint2 waypoint = path.get(pathIndex);
        float targetX = waypoint.x * DungeonMap.TILE_SIZE + (DungeonMap.TILE_SIZE - size) / 2f;
        float targetY = waypoint.y * DungeonMap.TILE_SIZE + (DungeonMap.TILE_SIZE - size) / 2f;

        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < WAYPOINT_TOLERANCE) {
            pathIndex++;
            return;
        }

        dx /= dist;
        dy /= dist;
        if (dx < 0f) {
            facingLeft = true;
        } else if (dx > 0f) {
            facingLeft = false;
        }

        moveAxis(dx * type.speed * delta, 0f, map);
        moveAxis(0f, dy * type.speed * delta, map);
    }

    private void recomputePath(Player player) {
        int startX = (int) (getCenterX() / DungeonMap.TILE_SIZE);
        int startY = (int) (getCenterY() / DungeonMap.TILE_SIZE);
        int goalX = (int) (player.getCenterX() / DungeonMap.TILE_SIZE);
        int goalY = (int) (player.getCenterY() / DungeonMap.TILE_SIZE);
        path = PathFinder.findPath(map, startX, startY, goalX, goalY);
        pathIndex = 0;
    }

    public int getXpReward() {
        return type.xpReward;
    }

    @Override
    public TextureRegion getCurrentFrame() {
        boolean moving = state == State.CHASING;
        Animation<TextureRegion> anim;
        switch (type) {
            case SLIME:
                anim = Assets.slugAnim;
                break;
            case GOBLIN:
                anim = moving ? Assets.goblinRun : Assets.goblinIdle;
                break;
            case SKELETON:
                anim = moving ? Assets.skeletRun : Assets.skeletIdle;
                break;
            case MASKED_ORC:
                anim = moving ? Assets.maskedOrcRun : Assets.maskedOrcIdle;
                break;
            case BRUTE:
                anim = moving ? Assets.ogreRun : Assets.ogreIdle;
                break;
            case MIMIC:
                anim = Assets.chestMimicAnim;
                break;
            case BOSS:
            default:
                anim = moving ? Assets.demonRun : Assets.demonIdle;
                break;
        }
        return anim.getKeyFrame(stateTime, true);
    }

    /** Desenhada à parte, depois do lote de sprites, porque usa ShapeRenderer em vez de SpriteBatch. */
    public void renderHealthBar(ShapeRenderer renderer) {
        float barX = x;
        float barY = y + size + 4f;
        float healthPct = currentHealth / (float) maxHealth;

        renderer.setColor(Color.BLACK);
        renderer.rect(barX - 1, barY - 1, size + 2, BAR_HEIGHT + 2);
        renderer.setColor(Color.DARK_GRAY);
        renderer.rect(barX, barY, size, BAR_HEIGHT);
        renderer.setColor(Color.RED);
        renderer.rect(barX, barY, size * healthPct, BAR_HEIGHT);
    }
}
