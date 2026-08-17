package com.leore.dungeoncrawler.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.leore.dungeoncrawler.DungeonCrawlerGame;
import com.leore.dungeoncrawler.GameStats;

/** Tela de título: só espera ENTER e troca para a GameScreen. */
public class MenuScreen implements Screen {

    private final DungeonCrawlerGame game;

    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont font;

    public MenuScreen(DungeonCrawlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        titleFont = new BitmapFont();
        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(2.2f);

        font = new BitmapFont();
        font.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        batch.begin();
        titleFont.draw(batch, "DUNGEON CRAWLER", centerX - 190, centerY + 60);
        font.draw(batch, "Pressione ENTER para jogar", centerX - 110, centerY);
        font.draw(batch, "WASD/setas: mover   ESPACO: atacar   E: usar pocao",
            centerX - 200, centerY - 30);
        font.draw(batch, String.format("Recorde: andar %d  |  Vitorias: %d  |  Tentativas: %d",
                GameStats.getBestFloor(), GameStats.getVictories(), GameStats.getRuns()),
            centerX - 190, centerY - 70);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            dispose();
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        font.dispose();
    }
}
