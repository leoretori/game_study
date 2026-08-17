package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.leore.dungeoncrawler.graphics.Assets;

/** NPC estático que fica parado numa sala; a lógica de compra vive no World. */
public class Merchant extends WorldObject {

    private static final float SIZE = 26f;

    public Merchant(float x, float y) {
        super(x, y, SIZE);
    }

    @Override
    public TextureRegion getCurrentFrame() {
        return Assets.docIdle.getKeyFrame(stateTime, true);
    }
}
