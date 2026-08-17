package com.leore.dungeoncrawler;

import com.badlogic.gdx.Game;
import com.leore.dungeoncrawler.graphics.Assets;
import com.leore.dungeoncrawler.screens.MenuScreen;

/**
 * Ponto de entrada compartilhado entre todas as plataformas.
 * Um {@link Game} do LibGDX apenas delega o ciclo de vida para a Screen ativa.
 */
public class DungeonCrawlerGame extends Game {

    @Override
    public void create() {
        GameStats.init();
        Assets.load();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        Assets.dispose();
    }
}
