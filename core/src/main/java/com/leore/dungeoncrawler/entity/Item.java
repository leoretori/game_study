package com.leore.dungeoncrawler.entity;

/** Objeto parado no chão da dungeon que o jogador coleta ao encostar. */
public abstract class Item extends WorldObject {

    protected Item(float x, float y, float size) {
        super(x, y, size);
    }

    /** Aplica o efeito da coleta (curar, dar ouro, guardar no inventário, etc.). */
    public abstract void onPickup(Player player);
}
