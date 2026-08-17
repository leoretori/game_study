package com.leore.dungeoncrawler.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Base de tudo que tem posição e é desenhado na dungeon: entidades vivas (jogador, inimigos)
 * e objetos estáticos (itens no chão, mercador). Quem precisa de vida/combate/movimento é a {@link Entity}.
 */
public abstract class WorldObject {

    protected float x, y;
    protected final float size;
    protected float stateTime = 0f;

    protected WorldObject(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    /** Avança a animação. Subclasses com lógica própria de update devem chamar isso também. */
    public void update(float delta) {
        stateTime += delta;
    }

    /** Qual frame de sprite desenhar agora, de acordo com o estado atual do objeto. */
    public abstract TextureRegion getCurrentFrame();

    public void render(SpriteBatch batch) {
        drawFrame(batch, getCurrentFrame(), false);
    }

    /** Desenha um frame, espelhando horizontalmente sem precisar mutar a TextureRegion compartilhada. */
    protected void drawFrame(SpriteBatch batch, TextureRegion frame, boolean flip) {
        if (flip) {
            batch.draw(frame, x + size, y, -size, size);
        } else {
            batch.draw(frame, x, y, size, size);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }

    public float getCenterX() {
        return x + size / 2f;
    }

    public float getCenterY() {
        return y + size / 2f;
    }

    public float distanceTo(WorldObject other) {
        float dx = getCenterX() - other.getCenterX();
        float dy = getCenterY() - other.getCenterY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
