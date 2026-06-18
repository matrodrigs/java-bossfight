package com.bossfight.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.bossfight.Constants;

public class VintageFloralBackground implements Disposable {
    private static final String ROOT = "sprites/background/floral_vintage/";

    private final Texture sky;
    private final Texture farHills;
    private final Texture distantGarden;
    private final Texture groundMain;
    private final Texture groundEdgeLeft;
    private final Texture groundEdgeRight;
    private final Texture treeLeft;
    private final Texture treeRight;
    private final Texture shrubA;
    private final Texture shrubB;
    private final Texture shrubC;
    private final Texture fgLeft;
    private final Texture fgCenter;
    private final Texture fgRight;
    private final Texture decorWarm;
    private final Texture decorDaisy;

    public VintageFloralBackground() {
        sky = load("sky/sky_wash.png");
        farHills = load("horizon/far_hills.png");
        distantGarden = load("horizon/distant_garden.png");
        groundMain = load("ground/ground_main.png");
        groundEdgeLeft = load("ground/ground_edge_left.png");
        groundEdgeRight = load("ground/ground_edge_right.png");
        treeLeft = load("midground/tree_left_canopy.png");
        treeRight = load("midground/tree_right_trunk.png");
        shrubA = load("midground/shrub_cluster_a.png");
        shrubB = load("midground/shrub_cluster_b.png");
        shrubC = load("midground/shrub_cluster_c.png");
        fgLeft = load("foreground/fg_foliage_left.png");
        fgCenter = load("foreground/fg_foliage_center.png");
        fgRight = load("foreground/fg_foliage_right.png");
        decorWarm = load("decor/flower_cluster_warm.png");
        decorDaisy = load("decor/flower_cluster_daisy.png");
    }

    public void renderBack(SpriteBatch batch, OrthographicCamera camera, float elapsed) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawParallax(batch, camera, sky, -40f, -26f, Constants.WORLD_WIDTH + 80f, Constants.WORLD_HEIGHT + 56f,
                0.02f, 1f, 0f);
        drawParallax(batch, camera, farHills, 84f, 160f, 1120f, 244f,
                0.12f, 0.78f, 0f);
        drawParallax(batch, camera, distantGarden, -18f, 100f, 1320f, 172f,
                0.24f, 0.78f, 0f);

        drawParallax(batch, camera, treeLeft, -12f, 100f, 240f, 370f,
                0.38f, 0.84f, 0f);
        drawParallax(batch, camera, treeRight, 1034f, 42f, 264f, 390f,
                0.48f, 0.96f, 0f);

        drawParallax(batch, camera, shrubA, 175f, 98f, 310f, 92f,
                0.58f, 0.74f, 0f);
        drawParallax(batch, camera, shrubB, 535f, 99f, 220f, 112f,
                0.6f, 0.78f, 0f);
        drawParallax(batch, camera, shrubC, 792f, 98f, 340f, 118f,
                0.62f, 0.8f, 0f);

        drawParallax(batch, camera, groundMain, -40f, -20f, 1350f, 168f,
                0.86f, 1f, 0f);
        drawParallax(batch, camera, groundEdgeLeft, -50f, -10f, 160f, 178f,
                0.88f, 1f, 0f);
        drawParallax(batch, camera, groundEdgeRight, 1160f, -10f, 160f, 156f,
                0.88f, 1f, 0f);

        drawParallax(batch, camera, decorWarm, 235f, 64f, 140f, 78f,
                0.9f, 0.95f, 0f);
        drawParallax(batch, camera, decorDaisy, 610f, 64f, 150f, 70f,
                0.9f, 0.95f, 0f);

        batch.setColor(Color.WHITE);
        batch.end();
    }

    public void renderForeground(SpriteBatch batch, OrthographicCamera camera, float elapsed) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawParallax(batch, camera, fgLeft, -20f, -44f, 370f, 168f,
                1.08f, 0.96f, 0f);
        drawParallax(batch, camera, fgCenter, 420f, -46f, 380f, 154f,
                1.08f, 0.94f, 0f);
        drawParallax(batch, camera, fgRight, 890f, -44f, 390f, 154f,
                1.08f, 0.96f, 0f);

        batch.setColor(Color.WHITE);
        batch.end();
    }

    private Texture load(String relativePath) {
        Texture texture = new Texture(Gdx.files.internal(ROOT + relativePath));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private void drawParallax(SpriteBatch batch, OrthographicCamera camera, Texture texture, float x, float y,
            float width, float height, float parallax, float alpha, float yDrift) {
        float cameraDeltaX = camera.position.x - Constants.WORLD_WIDTH * 0.5f;
        float cameraDeltaY = camera.position.y - Constants.WORLD_HEIGHT * 0.5f;
        float drawX = x + cameraDeltaX * (1f - parallax);
        float drawY = y + cameraDeltaY * (1f - parallax) + yDrift;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, drawX, drawY, width, height);
    }

    @Override
    public void dispose() {
        sky.dispose();
        farHills.dispose();
        distantGarden.dispose();
        groundMain.dispose();
        groundEdgeLeft.dispose();
        groundEdgeRight.dispose();
        treeLeft.dispose();
        treeRight.dispose();
        shrubA.dispose();
        shrubB.dispose();
        shrubC.dispose();
        fgLeft.dispose();
        fgCenter.dispose();
        fgRight.dispose();
        decorWarm.dispose();
        decorDaisy.dispose();
    }
}
