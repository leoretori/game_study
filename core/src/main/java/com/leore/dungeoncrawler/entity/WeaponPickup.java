package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.leore.dungeoncrawler.graphics.Assets;

/** Arma largada no chão; ao ser pega, substitui a arma equipada do jogador. */
public class WeaponPickup extends Item {

    private static final float SIZE = 16f;

    private final WeaponType weaponType;

    public WeaponPickup(float x, float y, WeaponType weaponType) {
        super(x, y, SIZE);
        this.weaponType = weaponType;
    }

    @Override
    public void onPickup(Player player) {
        player.equipWeapon(weaponType);
    }

    @Override
    public TextureRegion getCurrentFrame() {
        switch (weaponType) {
            case AXE:
                return Assets.axeIcon;
            case KATANA:
                return Assets.katanaIcon;
            case SPEAR:
                return Assets.spearIcon;
            case HAMMER:
                return Assets.hammerIcon;
            case SWORD:
            default:
                return Assets.swordIcon;
        }
    }
}
