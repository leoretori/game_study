package com.leore.dungeoncrawler.entity;

/** Cada arma é um perfil diferente de dano/alcance/velocidade de ataque — puro dado, sem lógica. */
public enum WeaponType {
    SWORD("Espada", 20, 46f, 0.4f),
    AXE("Machado", 30, 40f, 0.55f),
    KATANA("Katana", 16, 50f, 0.28f),
    SPEAR("Lanca", 18, 65f, 0.45f),
    HAMMER("Martelo", 38, 42f, 0.75f);

    public final String displayName;
    public final int damage;
    public final float range;
    public final float cooldown;

    WeaponType(String displayName, int damage, float range, float cooldown) {
        this.displayName = displayName;
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
    }
}
