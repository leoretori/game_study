package com.leore.dungeoncrawler.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.leore.dungeoncrawler.DungeonCrawlerGame;
import com.leore.dungeoncrawler.entity.Perk;
import com.leore.dungeoncrawler.entity.Player;
import com.leore.dungeoncrawler.graphics.Assets;
import com.leore.dungeoncrawler.world.World;

import java.util.List;

/** Tela principal de jogo: dona da câmera/renderers do LibGDX, delega a simulação pra World. */
public class GameScreen implements Screen {

    private static final int HEART_SLOTS = 10;
    private static final float HEART_SIZE = 18f;
    private static final float HEART_SPACING = 20f;

    private static final float MINIMAP_SCALE = 2f;
    private static final float MINIMAP_MARGIN = 10f;

    private final DungeonCrawlerGame game;
    private World world;
    private boolean paused = false;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private final Matrix4 hudMatrix = new Matrix4();

    public GameScreen(DungeonCrawlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        world = new World();

        camera = new OrthographicCamera();
        // Extend em vez de Fit: em telas mais largas que 800x600, mostra mais mundo ao redor
        // do jogador em vez de deixar tarjas pretas nas laterais.
        viewport = new ExtendViewport(800, 600, camera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void render(float delta) {
        Player player = world.getPlayer();
        boolean runOver = !player.isAlive() || world.isVictory();

        if (runOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                world.restart();
                paused = false;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                dispose();
                game.setScreen(new MenuScreen(game));
                return;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }

        // Sempre chama update (exceto pausado): o World decide sozinho o que simular —
        // mesmo com runOver=true ainda pode haver uma escolha de perk pendente do golpe
        // que terminou a run, e ela precisa continuar recebendo input até ser resolvida.
        if (!paused) {
            world.update(delta);
        }

        updateCamera(player);

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        world.render(batch, shapeRenderer);

        renderHud(player);
        renderMinimap();
    }

    private void renderMinimap() {
        float mapPixelWidth = world.getMapWidth() * MINIMAP_SCALE;
        float mapPixelHeight = world.getMapHeight() * MINIMAP_SCALE;
        float originX = Gdx.graphics.getWidth() - mapPixelWidth - MINIMAP_MARGIN;
        float originY = Gdx.graphics.getHeight() - mapPixelHeight - MINIMAP_MARGIN;

        shapeRenderer.setProjectionMatrix(hudMatrix);
        world.renderMinimap(shapeRenderer, originX, originY, MINIMAP_SCALE);
    }

    private void updateCamera(Player player) {
        camera.position.set(player.getCenterX(), player.getCenterY(), 0f);
        camera.update();
    }

    private void renderHud(Player player) {
        hudMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(hudMatrix);
        batch.begin();

        renderHealthHearts(player, Gdx.graphics.getHeight() - 28f);

        font.draw(batch, String.format("Andar %d/%d  |  Nivel %d (XP %d/%d)  |  Arma: %s",
                world.getFloor(), World.FINAL_FLOOR, player.getLevel(), player.getXp(), player.getXpToNextLevel(),
                player.getWeapon().displayName),
            10, Gdx.graphics.getHeight() - 40);
        font.draw(batch, String.format("Ouro %d  |  Pocoes %d [E]  |  FPS %d  |  [ESPACO] ataca  [ESC] pausa",
                player.getGold(), player.getPotions(), Gdx.graphics.getFramesPerSecond()),
            10, Gdx.graphics.getHeight() - 60);

        if (world.isBossFloor() && player.isAlive() && !world.isVictory()) {
            font.draw(batch, "O CHEFE ESTA NESTE ANDAR - derrote-o para vencer!",
                10, Gdx.graphics.getHeight() - 80);
        }

        if (world.isNearMerchant() && player.isAlive()) {
            font.draw(batch, "Mercador: [1] Pocao 20g  [2] +20 vida max 35g  [3] +5 dano 50g  [4] Cura completa 15g",
                10, 30);
        }

        if (player.hasPendingPerkChoice()) {
            renderPerkChoice(player);
        } else if (world.isVictory()) {
            float cx = Gdx.graphics.getWidth() / 2f;
            float cy = Gdx.graphics.getHeight() / 2f;
            font.draw(batch, "VOCE VENCEU! Derrotou o chefe da dungeon.", cx - 220, cy + 40);
            renderRunSummary(player, cx, cy + 10);
            font.draw(batch, "R reinicia  |  ESC volta ao menu", cx - 140, cy - 55);
            if (world.isLastRunNewRecord()) {
                font.draw(batch, "NOVO RECORDE DE ANDAR!", cx - 110, cy - 75);
            }
        } else if (!player.isAlive()) {
            float cx = Gdx.graphics.getWidth() / 2f;
            float cy = Gdx.graphics.getHeight() / 2f;
            font.draw(batch, "VOCE MORREU", cx - 60, cy + 40);
            renderRunSummary(player, cx, cy + 10);
            font.draw(batch, "R reinicia  |  ESC volta ao menu", cx - 140, cy - 55);
            if (world.isLastRunNewRecord()) {
                font.draw(batch, "NOVO RECORDE DE ANDAR!", cx - 110, cy - 75);
            }
        } else if (paused) {
            font.draw(batch, "PAUSADO - ESC continua",
                Gdx.graphics.getWidth() / 2f - 90, Gdx.graphics.getHeight() / 2f);
        }
        batch.end();
    }

    /** Cada coração representa uma fração igual da vida máxima, então a barra sempre tem 10 slots. */
    private void renderHealthHearts(Player player, float y) {
        float hpPerHeart = player.getMaxHealth() / (float) HEART_SLOTS;

        for (int i = 0; i < HEART_SLOTS; i++) {
            float heartFloor = i * hpPerHeart;
            float heartCeil = (i + 1) * hpPerHeart;

            TextureRegion region;
            if (player.getCurrentHealth() >= heartCeil) {
                region = Assets.heartFull;
            } else if (player.getCurrentHealth() <= heartFloor) {
                region = Assets.heartEmpty;
            } else {
                region = Assets.heartHalf;
            }
            batch.draw(region, 10 + i * HEART_SPACING, y, HEART_SIZE, HEART_SIZE);
        }
    }

    private void renderRunSummary(Player player, float centerX, float y) {
        font.draw(batch, String.format("Andar %d  |  Nivel %d  |  Ouro coletado %d  |  Inimigos derrotados %d",
                world.getFloor(), player.getLevel(), player.getGold(), player.getEnemiesKilled()),
            centerX - 260, y);
    }

    private void renderPerkChoice(Player player) {
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        List<Perk> choices = player.getCurrentPerkChoices();

        font.draw(batch, "SUBIU DE NIVEL! Escolha um bonus:", centerX - 170, centerY + 50);
        for (int i = 0; i < choices.size(); i++) {
            Perk perk = choices.get(i);
            font.draw(batch, "[" + (i + 1) + "] " + perk.title + " - " + perk.description,
                centerX - 170, centerY + 20 - i * 22);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
