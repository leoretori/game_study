package com.leore.dungeoncrawler.entity;

/** Cada variante de inimigo é só um conjunto de stats; o comportamento é o mesmo, em {@link Enemy}. */
public enum EnemyType {
    SLIME(30, 60f, 10, 150f, 22f, 10, false),
    GOBLIN(25, 140f, 12, 170f, 20f, 15, false),
    SKELETON(55, 120f, 16, 190f, 20f, 20, false),
    MASKED_ORC(70, 90f, 20, 180f, 24f, 28, false),
    BRUTE(100, 75f, 26, 170f, 28f, 35, false),
    /** Só é spawnado ao abrir um baú-armadilha; o raio de detecção gigante faz ele já nascer perseguindo. */
    MIMIC(45, 50f, 18, 9999f, 24f, 25, false),
    BOSS(300, 65f, 30, 260f, 44f, 150, true);

    public final int maxHealth;
    public final float speed;
    public final int attackDamage;
    public final float detectionRadius;
    public final float size;
    public final int xpReward;
    public final boolean hasSlamAttack;

    EnemyType(int maxHealth, float speed, int attackDamage, float detectionRadius,
              float size, int xpReward, boolean hasSlamAttack) {
        this.maxHealth = maxHealth;
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.detectionRadius = detectionRadius;
        this.size = size;
        this.xpReward = xpReward;
        this.hasSlamAttack = hasSlamAttack;
    }
}
