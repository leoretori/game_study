package com.leore.dungeoncrawler.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;

/** Carrega todos os sprites uma única vez e expõe como Animation/TextureRegion prontos pra usar. */
public final class Assets {

    private static final List<Texture> loadedTextures = new ArrayList<>();

    public static Animation<TextureRegion> knightIdle;
    public static Animation<TextureRegion> knightRun;
    public static TextureRegion knightHit;

    public static Animation<TextureRegion> slugAnim;
    public static Animation<TextureRegion> goblinIdle;
    public static Animation<TextureRegion> goblinRun;
    public static Animation<TextureRegion> skeletIdle;
    public static Animation<TextureRegion> skeletRun;
    public static Animation<TextureRegion> maskedOrcIdle;
    public static Animation<TextureRegion> maskedOrcRun;
    public static Animation<TextureRegion> ogreIdle;
    public static Animation<TextureRegion> ogreRun;
    public static Animation<TextureRegion> demonIdle;
    public static Animation<TextureRegion> demonRun;
    public static Animation<TextureRegion> docIdle;

    public static TextureRegion flaskRed;
    public static Animation<TextureRegion> coinAnim;

    public static TextureRegion wallTopLeft;
    public static TextureRegion wallTopMid;
    public static TextureRegion wallTopRight;
    public static TextureRegion wallLeft;
    public static TextureRegion wallMid;
    public static TextureRegion wallRight;
    public static TextureRegion stairsTile;
    public static TextureRegion[] floorTiles;

    public static TextureRegion heartFull;
    public static TextureRegion heartHalf;
    public static TextureRegion heartEmpty;

    public static TextureRegion chestClosed;
    public static Animation<TextureRegion> chestMimicAnim;

    public static TextureRegion swordIcon;
    public static TextureRegion axeIcon;
    public static TextureRegion katanaIcon;
    public static TextureRegion spearIcon;
    public static TextureRegion hammerIcon;

    private Assets() {
    }

    public static void load() {
        knightIdle = loadAnim("knight_m_idle_anim_f", 4, 0.15f);
        knightRun = loadAnim("knight_m_run_anim_f", 4, 0.1f);
        knightHit = loadRegion("knight_m_hit_anim_f0");

        slugAnim = loadAnim("slug_anim_f", 4, 0.2f);
        goblinIdle = loadAnim("goblin_idle_anim_f", 4, 0.16f);
        goblinRun = loadAnim("goblin_run_anim_f", 4, 0.09f);
        skeletIdle = loadAnim("skelet_idle_anim_f", 4, 0.18f);
        skeletRun = loadAnim("skelet_run_anim_f", 4, 0.12f);
        maskedOrcIdle = loadAnim("masked_orc_idle_anim_f", 4, 0.2f);
        maskedOrcRun = loadAnim("masked_orc_run_anim_f", 4, 0.13f);
        ogreIdle = loadAnim("ogre_idle_anim_f", 4, 0.2f);
        ogreRun = loadAnim("ogre_run_anim_f", 4, 0.14f);
        demonIdle = loadAnim("big_demon_idle_anim_f", 4, 0.22f);
        demonRun = loadAnim("big_demon_run_anim_f", 4, 0.15f);
        docIdle = loadAnim("doc_idle_anim_f", 4, 0.25f);

        flaskRed = loadRegion("flask_red");
        coinAnim = loadAnim("coin_anim_f", 4, 0.15f);

        wallTopLeft = loadRegion("wall_top_left");
        wallTopMid = loadRegion("wall_top_mid");
        wallTopRight = loadRegion("wall_top_right");
        wallLeft = loadRegion("wall_left");
        wallMid = loadRegion("wall_mid");
        wallRight = loadRegion("wall_right");
        stairsTile = loadRegion("floor_stairs");
        floorTiles = new TextureRegion[] {
            loadRegion("floor_1"), loadRegion("floor_2"), loadRegion("floor_3"), loadRegion("floor_4")
        };

        heartFull = loadRegion("ui_heart_full");
        heartHalf = loadRegion("ui_heart_half");
        heartEmpty = loadRegion("ui_heart_empty");

        chestClosed = loadRegion("chest_full_open_anim_f0");
        chestMimicAnim = loadAnim("chest_mimic_open_anim_f", 3, 0.2f);

        swordIcon = loadRegion("weapon_regular_sword");
        axeIcon = loadRegion("weapon_axe");
        katanaIcon = loadRegion("weapon_katana");
        spearIcon = loadRegion("weapon_spear");
        hammerIcon = loadRegion("weapon_big_hammer");
    }

    private static TextureRegion loadRegion(String name) {
        Texture texture = new Texture(Gdx.files.internal("sprites/" + name + ".png"));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        loadedTextures.add(texture);
        return new TextureRegion(texture);
    }

    private static Animation<TextureRegion> loadAnim(String prefix, int frameCount, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = loadRegion(prefix + i);
        }
        return new Animation<>(frameDuration, frames);
    }

    public static void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
        loadedTextures.clear();
    }
}
