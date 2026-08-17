package com.leore.dungeoncrawler.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.leore.dungeoncrawler.DungeonCrawlerGame;

/** Ponto de entrada da versão desktop: só configura a janela e inicia o jogo. */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Dungeon Crawler");
        config.setWindowedMode(800, 600);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new DungeonCrawlerGame(), config);
    }
}
