package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.leore.dungeoncrawler.graphics.Assets;
import com.leore.dungeoncrawler.world.DungeonMap;

import java.util.ArrayList;
import java.util.List;

/** Jogador controlado por WASD/setas: movimento livre, ataque corpo-a-corpo, inventário e progressão. */
public class Player extends Entity {

    private static final float BASE_SPEED = 220f; // pixels por segundo
    private static final float SIZE = 22f;
    private static final int STARTING_MAX_HEALTH = 100;
    private static final float INVULNERABILITY_DURATION = 0.45f;

    private static final float MIN_ATTACK_COOLDOWN = 0.1f;
    private static final float ATTACK_ANIM_DURATION = 0.15f;

    private static final int POTION_HEAL_AMOUNT = 30;
    private static final int STARTING_POTIONS = 1;

    private static final float XP_GROWTH = 1.35f;
    private static final int STARTING_XP_TO_NEXT_LEVEL = 50;

    private DungeonMap map;
    private float attackCooldownTimer = 0f;
    private float attackAnimTimer = 0f;

    private float speed = BASE_SPEED;
    private float lifestealPercent = 0f;

    // Os stats de ataque vêm da arma equipada + bônus acumulados de perks/loja,
    // em vez de valores fixos — assim trocar de arma não apaga o progresso ganho.
    private WeaponType weapon = WeaponType.SWORD;
    private int bonusAttackDamage = 0;
    private float bonusAttackRange = 0f;
    private float attackCooldownMultiplier = 1f;

    private int level = 1;
    private int xp = 0;
    private int xpToNextLevel = STARTING_XP_TO_NEXT_LEVEL;
    private int pendingPerkChoices = 0;
    private final List<Perk> currentPerkChoices = new ArrayList<>();

    private int gold = 0;
    private int potions = STARTING_POTIONS;
    private int enemiesKilled = 0;

    private boolean moving = false;

    public Player(float x, float y, DungeonMap map) {
        super(x, y, SIZE, STARTING_MAX_HEALTH);
        this.map = map;
    }

    public void update(float delta) {
        tickTimers(delta);
        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= delta;
        }
        if (attackAnimTimer > 0f) {
            attackAnimTimer -= delta;
        }

        handleMovement(delta);
    }

    private void handleMovement(float delta) {
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY -= 1f;
        }

        if (moveX != 0f && moveY != 0f) {
            // Normaliza para não andar mais rápido na diagonal.
            float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;
        }

        moving = moveX != 0f || moveY != 0f;
        if (moveX < 0f) {
            facingLeft = true;
        } else if (moveX > 0f) {
            facingLeft = false;
        }

        moveAxis(moveX * speed * delta, 0f, map);
        moveAxis(0f, moveY * speed * delta, map);
    }

    @Override
    public TextureRegion getCurrentFrame() {
        if (isAttackAnimating()) {
            return Assets.knightHit;
        }
        Animation<TextureRegion> anim = moving ? Assets.knightRun : Assets.knightIdle;
        return anim.getKeyFrame(stateTime, true);
    }

    /** Consome a tecla de ataque (se houver e não estiver em cooldown) e retorna se disparou golpe. */
    public boolean tryAttack() {
        if (attackCooldownTimer > 0f || !Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            return false;
        }
        attackCooldownTimer = getAttackCooldown();
        attackAnimTimer = ATTACK_ANIM_DURATION;
        return true;
    }

    /** Consome a tecla de uso de poção (E). Retorna se uma poção foi de fato usada. */
    public boolean tryUsePotion() {
        if (potions <= 0 || !Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            return false;
        }
        potions--;
        currentHealth = Math.min(maxHealth, currentHealth + POTION_HEAL_AMOUNT);
        return true;
    }

    public void healToFull() {
        currentHealth = maxHealth;
    }

    public boolean isAttackAnimating() {
        return attackAnimTimer > 0f;
    }

    public int getAttackDamage() {
        return weapon.damage + bonusAttackDamage;
    }

    public float getAttackRange() {
        return weapon.range + bonusAttackRange;
    }

    private float getAttackCooldown() {
        return Math.max(MIN_ATTACK_COOLDOWN, weapon.cooldown * attackCooldownMultiplier);
    }

    public void equipWeapon(WeaponType newWeapon) {
        this.weapon = newWeapon;
    }

    public WeaponType getWeapon() {
        return weapon;
    }

    public void recordKill() {
        enemiesKilled++;
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public void healFromLifesteal(int damageDealt) {
        if (lifestealPercent <= 0f) {
            return;
        }
        int healAmount = Math.round(damageDealt * lifestealPercent);
        currentHealth = Math.min(maxHealth, currentHealth + healAmount);
    }

    // --- Economia / loja -------------------------------------------------

    public void addGold(int amount) {
        gold += amount;
    }

    public int getGold() {
        return gold;
    }

    public boolean trySpendGold(int amount) {
        if (gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }

    public void addPotion() {
        potions++;
    }

    public int getPotions() {
        return potions;
    }

    // --- Perks -------------------------------------------------------------

    public void upgradeMaxHealth(int amount) {
        increaseMaxHealth(amount);
    }

    public void upgradeAttackDamage(int amount) {
        bonusAttackDamage += amount;
    }

    public void upgradeSpeed(float percent) {
        speed *= (1f + percent);
    }

    public void upgradeAttackRange(float amount) {
        bonusAttackRange += amount;
    }

    public void upgradeAttackSpeed(float percent) {
        attackCooldownMultiplier *= (1f - percent);
    }

    public void upgradeLifesteal(float percent) {
        lifestealPercent += percent;
    }

    // --- XP / nível ----------------------------------------------------

    public void addXp(int amount) {
        xp += amount;
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            level++;
            xpToNextLevel = Math.round(xpToNextLevel * XP_GROWTH);
            pendingPerkChoices++;
        }
    }

    public boolean hasPendingPerkChoice() {
        return pendingPerkChoices > 0;
    }

    /** Sorteia (uma única vez) as opções da escolha de perk atual. */
    public List<Perk> getCurrentPerkChoices() {
        if (currentPerkChoices.isEmpty() && hasPendingPerkChoice()) {
            currentPerkChoices.addAll(Perk.randomThree());
        }
        return currentPerkChoices;
    }

    public void choosePerk(Perk perk) {
        perk.apply(this);
        pendingPerkChoices--;
        currentPerkChoices.clear();
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getXpToNextLevel() {
        return xpToNextLevel;
    }

    /** Usado ao trocar de andar: o jogador some do mapa antigo e aparece no novo, mantendo status. */
    public void teleportTo(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }

    public void setMap(DungeonMap map) {
        this.map = map;
    }

    @Override
    protected float getInvulnerabilityDuration() {
        return INVULNERABILITY_DURATION;
    }
}
