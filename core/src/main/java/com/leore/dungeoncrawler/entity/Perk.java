package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Bônus escolhido pelo jogador ao subir de nível. Cada constante tem seu próprio
 * comportamento (um exemplo de enum com corpo por constante, em vez de um switch externo).
 */
public enum Perk {
    VIGOR("Vigor", "+15 vida maxima") {
        @Override
        public void apply(Player player) {
            player.upgradeMaxHealth(15);
        }
    },
    FORCA("Forca", "+3 dano de ataque") {
        @Override
        public void apply(Player player) {
            player.upgradeAttackDamage(3);
        }
    },
    AGILIDADE("Agilidade", "+15% velocidade de movimento") {
        @Override
        public void apply(Player player) {
            player.upgradeSpeed(0.15f);
        }
    },
    ALCANCE("Alcance", "+8 alcance de ataque") {
        @Override
        public void apply(Player player) {
            player.upgradeAttackRange(8f);
        }
    },
    FURIA("Furia", "-15% cooldown de ataque") {
        @Override
        public void apply(Player player) {
            player.upgradeAttackSpeed(0.15f);
        }
    },
    VAMPIRISMO("Vampirismo", "+15% do dano causado vira cura") {
        @Override
        public void apply(Player player) {
            player.upgradeLifesteal(0.15f);
        }
    };

    public final String title;
    public final String description;

    Perk(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public abstract void apply(Player player);

    /** Sorteia 3 perks distintos para o jogador escolher. */
    public static List<Perk> randomThree() {
        List<Perk> pool = new ArrayList<>(List.of(values()));
        for (int i = pool.size() - 1; i > 0; i--) {
            int j = MathUtils.random(i);
            Perk temp = pool.get(i);
            pool.set(i, pool.get(j));
            pool.set(j, temp);
        }
        return pool.subList(0, Math.min(3, pool.size()));
    }
}
