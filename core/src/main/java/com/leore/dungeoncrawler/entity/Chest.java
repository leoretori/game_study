package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.leore.dungeoncrawler.graphics.Assets;

/** Baú fechado no chão da dungeon. A lógica de "o que acontece ao abrir" vive no World. */
public class Chest extends WorldObject {

    private static final float SIZE = 20f;

    public Chest(float x, float y) {
        super(x, y, SIZE);
    }

    @Override
    public TextureRegion getCurrentFrame() {
        return Assets.chestClosed;
    }
}
