package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.leore.dungeoncrawler.world.DungeonMap;

/**
 * Base para tudo que se move colidindo com paredes e pode levar/causar dano
 * dentro da dungeon (jogador, inimigos, futuramente projéteis...).
 */
public abstract class Entity extends WorldObject {

    protected int maxHealth;
    protected int currentHealth;
    protected boolean facingLeft = false;

    private float invulnerableTimer = 0f;
    private float hitFlashTimer = 0f;

    protected Entity(float x, float y, float size, int maxHealth) {
        super(x, y, size);
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (hitFlashTimer > 0f) {
            batch.setColor(1f, 0.35f, 0.35f, 1f);
        }
        drawFrame(batch, getCurrentFrame(), facingLeft);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** Subclasses devem chamar isso no início do próprio update() para os timers avançarem. */
    protected void tickTimers(float delta) {
        stateTime += delta;
        if (invulnerableTimer > 0f) {
            invulnerableTimer -= delta;
        }
        if (hitFlashTimer > 0f) {
            hitFlashTimer -= delta;
        }
    }

    public void takeDamage(int amount) {
        if (!isAlive() || invulnerableTimer > 0f) {
            return;
        }
        currentHealth = Math.max(0, currentHealth - amount);
        hitFlashTimer = 0.12f;
        invulnerableTimer = getInvulnerabilityDuration();
    }

    /** Quanto tempo a entidade fica imune após tomar um golpe. 0 = sem invulnerabilidade. */
    protected float getInvulnerabilityDuration() {
        return 0f;
    }

    protected void increaseMaxHealth(int amount) {
        maxHealth += amount;
        currentHealth += amount;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    /** Move um eixo por vez e desfaz o passo se qualquer canto da entidade bater em parede. */
    protected void moveAxis(float dx, float dy, DungeonMap map) {
        float newX = x + dx;
        float newY = y + dy;

        if (canStandAt(newX, newY, map)) {
            x = newX;
            y = newY;
        }
    }

    private boolean canStandAt(float testX, float testY, DungeonMap map) {
        return map.isWalkableWorld(testX, testY)
            && map.isWalkableWorld(testX + size, testY)
            && map.isWalkableWorld(testX, testY + size)
            && map.isWalkableWorld(testX + size, testY + size);
    }
}
