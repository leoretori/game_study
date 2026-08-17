package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.leore.dungeoncrawler.graphics.Assets;

/** Coleta instantânea: soma ouro ao jogador. */
public class GoldCoin extends Item {

    private static final float SIZE = 10f;
    private static final int VALUE = 10;

    public GoldCoin(float x, float y) {
        super(x, y, SIZE);
    }

    @Override
    public void onPickup(Player player) {
        player.addGold(VALUE);
    }

    @Override
    public TextureRegion getCurrentFrame() {
        return Assets.coinAnim.getKeyFrame(stateTime, true);
    }
}
