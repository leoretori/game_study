package com.leore.dungeoncrawler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Estatísticas persistentes entre execuções do jogo (sobrevive ao fechar a janela).
 * Usa a API Preferences do LibGDX, que grava um arquivo local automaticamente —
 * uma boa introdução a I/O em Java sem precisar mexer com File/Serializable na mão.
 */
public final class GameStats {

    private static final String PREFS_NAME = "dungeon-crawler-save";
    private static final String KEY_BEST_FLOOR = "bestFloor";
    private static final String KEY_VICTORIES = "victories";
    private static final String KEY_RUNS = "runs";

    private static Preferences prefs;

    private GameStats() {
    }

    public static void init() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public static int getBestFloor() {
        return prefs.getInteger(KEY_BEST_FLOOR, 0);
    }

    public static int getVictories() {
        return prefs.getInteger(KEY_VICTORIES, 0);
    }

    public static int getRuns() {
        return prefs.getInteger(KEY_RUNS, 0);
    }

    /** Chamado uma vez ao fim de cada run (morte ou vitória). Retorna se bateu o recorde de andar. */
    public static boolean recordRunEnd(int floorReached, boolean won) {
        prefs.putInteger(KEY_RUNS, getRuns() + 1);

        boolean newRecord = floorReached > getBestFloor();
        if (newRecord) {
            prefs.putInteger(KEY_BEST_FLOOR, floorReached);
        }
        if (won) {
            prefs.putInteger(KEY_VICTORIES, getVictories() + 1);
        }

        prefs.flush();
        return newRecord;
    }
}
