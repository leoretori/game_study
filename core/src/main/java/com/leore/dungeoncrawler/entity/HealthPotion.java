package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.leore.dungeoncrawler.graphics.Assets;

/** Vai para o inventário do jogador; é consumida manualmente com a tecla de uso (E). */
public class HealthPotion extends Item {

    private static final float SIZE = 14f;

    public HealthPotion(float x, float y) {
        super(x, y, SIZE);
    }

    @Override
    public void onPickup(Player player) {
        player.addPotion();
    }

    @Override
    public TextureRegion getCurrentFrame() {
        return Assets.flaskRed;
    }
}
