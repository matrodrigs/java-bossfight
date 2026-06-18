package com.bossfight.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.bossfight.MainGame;
import com.bossfight.boss.BossSoundEvent;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.AudioManager;
import com.bossfight.systems.CollisionSystem;
import com.bossfight.systems.ParticleSystem;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.systems.RetroTextFactory;
import com.bossfight.systems.VintageFloralBackground;
import com.bossfight.Constants;

public class BattleScreen extends ScreenAdapter {
    private static final String BATTLE_MUSIC_PATH = "audio/music/boss_fight_theme.mp3";
    private static final String KNOCKOUT_NARRATION_PATH = "audio/voice/narrator_knockout.wav";
    private static final float BATTLE_MUSIC_VOLUME = 0.03f;
    private static final float KNOCKOUT_NARRATION_VOLUME = 1f;
    private static final float KNOCKOUT_DURATION = 3.2f;
    private static final float KNOCKOUT_TEXT_ONSET = 0.1f;
    private static final float KNOCKOUT_TEXT_FADE_START = 1.2f;
    private static final float KNOCKOUT_TEXT_FADE_DURATION = 0.35f;
    private static final float KNOCKOUT_PARTICLE_DURATION = 2.35f;
    private static final float KNOCKOUT_PARTICLE_INTERVAL = 0.11f;

    private enum PlayerPose {
        HURT,
        DASH,
        SPECIAL,
        AIR_SHOOT,
        RUN_SHOOT,
        SHOOT,
        JUMP,
        RUN,
        IDLE
    }

    private final MainGame game;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final Texture bossSpriteSheet;
    private final TextureRegion[] bossFrames;
    private final Texture playerSpriteSheet;
    private final TextureRegion playerShootFrame;
    private final TextureRegion playerJumpFrame;
    private final TextureRegion playerRunFrame;
    private final TextureRegion playerRunAltFrame;
    private final TextureRegion playerHurtFrame;
    private final TextureRegion playerAirShootFrame;
    private final TextureRegion playerRunShootFrame;
    private final TextureRegion playerSpecialFrame;
    private final TextureRegion playerIdleFrame;
    private final RetroTextFactory textFactory;
    private final Texture readyText;
    private final Texture goText;
    private final Texture knockoutText;
    private final Texture hpLabelText;
    private final Texture specialLabelText;
    private final Texture bossNameText;
    private final Texture bossPreparingText;
    private final Texture bossVineText;
    private final Texture bossHandsText;
    private final Texture bossPollenText;
    private final Texture bossDefeatedText;
    private final Player player;
    private final Boss boss;
    private final VintageFloralBackground background;
    private final ProjectileSystem projectileSystem;
    private final ParticleSystem particleSystem;
    private final CollisionSystem collisionSystem;
    private float elapsed;
    private float introTimer;
    private float hitstopTimer;
    private float shakeTimer;
    private float shakeDuration;
    private float shakeMagnitude;
    private float knockoutTimer;
    private float knockoutParticleTimer;
    private boolean readyCuePlayed;
    private boolean goCuePlayed;
    private boolean fightStarted;
    private boolean knockoutSequenceActive;

    public BattleScreen(MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        bossSpriteSheet = loadTexture("sprites/boss/flower_boss_sheet_clean.png");
        bossFrames = splitBossFrames(bossSpriteSheet);
        playerSpriteSheet = loadTexture("sprites/player/clock_player_sheet.png");
        playerShootFrame = playerFrame(11, 272, 271, 271);
        playerJumpFrame = playerFrame(379, 255, 234, 265);
        playerRunFrame = playerFrame(695, 277, 225, 260);
        playerRunAltFrame = playerFrame(58, 964, 225, 260);
        playerHurtFrame = playerFrame(953, 289, 267, 291);
        playerAirShootFrame = playerFrame(314, 659, 273, 245);
        playerRunShootFrame = playerFrame(636, 690, 263, 250);
        playerSpecialFrame = playerFrame(996, 682, 195, 260);
        playerIdleFrame = playerFrame(58, 684, 191, 254);
        textFactory = new RetroTextFactory();
        readyText = textFactory.createFightCue("READY?", false);
        goText = textFactory.createFightCue("GO!", true);
        knockoutText = textFactory.createKnockout("A KNOCKOUT!");
        hpLabelText = textFactory.createHudLabel("HP.");
        specialLabelText = textFactory.createHudLabel("ESPECIAL");
        bossNameText = textFactory.createHudLabel("FLOR-MAESTRO");
        bossPreparingText = textFactory.createHudValue("PREPARANDO");
        bossVineText = textFactory.createHudValue("BOTE DE CIPÓ");
        bossHandsText = textFactory.createHudValue("MÃOS MÁGICAS");
        bossPollenText = textFactory.createHudValue("CHUVA DE PÓLEN");
        bossDefeatedText = textFactory.createHudValue("DERROTADO");
        player = new Player();
        boss = new Boss();
        background = new VintageFloralBackground();
        projectileSystem = new ProjectileSystem();
        particleSystem = new ParticleSystem();
        collisionSystem = new CollisionSystem();
    }

    @Override
    public void render(float delta) {
        float safeDelta = Math.min(delta, 1f / 30f);
        if (!update(safeDelta)) {
            return;
        }

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        applyCameraShake();
        renderWorld();
        renderUi();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        game.getAudioManager().stopMusic();
        game.getAudioManager().stopVoice();
        bossSpriteSheet.dispose();
        playerSpriteSheet.dispose();
        background.dispose();
        textFactory.dispose();
        projectileSystem.dispose();
        particleSystem.clear();
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private TextureRegion playerFrame(int x, int y, int width, int height) {
        return new TextureRegion(playerSpriteSheet, x, y, width, height);
    }

    private boolean update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.getAudioManager().playCue(AudioManager.Cue.MENU_BACK);
            game.showMenuScreen();
            return false;
        }

        elapsed += delta;
        shakeTimer = Math.max(0f, shakeTimer - delta);
        particleSystem.update(delta);
        updateIntro(delta);

        if (knockoutSequenceActive) {
            return updateKnockoutSequence(delta);
        }

        if (hitstopTimer > 0f) {
            hitstopTimer = Math.max(0f, hitstopTimer - delta);
            return true;
        }

        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        boolean dash = Gdx.input.isKeyJustPressed(Input.Keys.K)
                || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT);
        boolean shoot = Gdx.input.isKeyPressed(Input.Keys.F)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        boolean special = Gdx.input.isKeyJustPressed(Input.Keys.G)
                || Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT);

        if (!fightStarted) {
            player.update(delta, false, false, false, false);
            boss.updateEntrance(delta);
            return true;
        }

        player.update(delta, moveLeft, moveRight, jump, dash);
        if (player.consumeDashStarted()) {
            particleSystem.spawnDash(player.getCenterX(), player.getCenterY(), player.getFacingDirection());
            game.getAudioManager().playCue(AudioManager.Cue.DASH);
        }

        if (special) {
            Projectile projectile = player.tryShootSpecial();
            if (projectile != null) {
                projectileSystem.addProjectile(projectile);
                particleSystem.spawnMuzzle(projectile.getCenterX(), projectile.getCenterY(), player.getFacingDirection(), true);
                game.getAudioManager().playCue(AudioManager.Cue.PLAYER_SPECIAL);
                requestShake(6f, 0.16f);
            }
        } else if (shoot) {
            Projectile projectile = player.tryShoot();
            if (projectile != null) {
                projectileSystem.addProjectile(projectile);
                particleSystem.spawnMuzzle(projectile.getCenterX(), projectile.getCenterY(), player.getFacingDirection(), false);
                game.getAudioManager().playCue(AudioManager.Cue.PLAYER_SHOOT);
            }
        }

        boss.update(delta, projectileSystem, player);
        projectileSystem.update(delta);
        collisionSystem.resolve(player, boss, projectileSystem, particleSystem, game.getAudioManager(), delta);
        float requestedHitstop = collisionSystem.consumeRequestedHitstop();
        if (requestedHitstop > 0f) {
            hitstopTimer = Math.max(hitstopTimer, requestedHitstop);
        }
        float requestedShake = collisionSystem.consumeRequestedShake();
        if (requestedShake > 0f) {
            requestShake(requestedShake, 0.18f);
        }

        if (boss.isDefeated()) {
            beginKnockoutSequence();
            return true;
        }

        playBossSoundEvents();

        if (player.isDead()) {
            game.getAudioManager().playCue(AudioManager.Cue.DEFEAT);
            game.showEndScreen(false);
            return false;
        }

        return true;
    }

    private void renderWorld() {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        background.renderBack(game.getBatch(), camera, elapsed);

        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (!fightStarted) {
            drawIntroSpotlight(shapeRenderer);
        }

        drawBossShadow(shapeRenderer);
        drawBossTelegraphGlow(shapeRenderer);

        shapeRenderer.end();

        renderBossSprite();
        renderPlayerSprite();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        projectileSystem.renderWarnings(shapeRenderer);
        particleSystem.render(shapeRenderer);
        shapeRenderer.end();

        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        projectileSystem.renderSprites(game.getBatch());
        game.getBatch().end();

        background.renderForeground(game.getBatch(), camera, elapsed);

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderBossSprite() {
        String state = boss.getStateName();
        boolean defeated = boss.isDefeated();
        boolean vineStrike = "Bote de cipó".equals(state);
        boolean magicHands = "Mãos mágicas".equals(state);
        boolean pollenRain = "Chuva de pólen".equals(state);
        TextureRegion frame = bossFrames[selectBossFrame(state)];

        float breath = defeated ? 0f : MathUtils.sin(elapsed * 3.4f);
        float windup = boss.isTelegraphing() ? 1f - boss.getTelegraphAlpha() : 0f;
        float attackPulse = !defeated && (vineStrike || magicHands || pollenRain) ? MathUtils.sin(elapsed * 9.5f) : 0f;
        float visualHeight = 506f + breath * 5f + (boss.isPhaseTwo() ? 18f : 0f);
        visualHeight += vineStrike ? windup * 18f : 0f;
        float visualWidth = visualHeight * frame.getRegionWidth() / frame.getRegionHeight();

        float x = boss.getCenterX() - visualWidth * 0.5f - 18f;
        x += vineStrike ? attackPulse * 8f - 18f - windup * 18f : 0f;
        x += magicHands ? MathUtils.sin(elapsed * 7f) * 5f - windup * 10f : 0f;

        float y = Constants.FLOOR_Y - 30f;
        y += breath * 2.5f;
        y += pollenRain ? MathUtils.sin(elapsed * 8.5f) * 6f : 0f;

        float scaleX = 1f + breath * 0.014f + (magicHands ? attackPulse * 0.02f : 0f);
        float scaleY = 1f - breath * 0.01f + (vineStrike ? attackPulse * 0.028f : 0f);
        float rotation = MathUtils.sin(elapsed * 2.1f) * 0.8f + attackPulse * (pollenRain ? 1.4f : 0.45f);

        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        if (boss.isPhaseTwo()) {
            game.getBatch().setColor(1f, 0.92f, 0.92f, 1f);
        }
        game.getBatch().draw(frame,
                x,
                y,
                visualWidth * 0.48f,
                44f,
                visualWidth,
                visualHeight,
                scaleX,
                scaleY,
                rotation);
        game.getBatch().setColor(Color.WHITE);
        game.getBatch().end();
    }

    private void renderPlayerSprite() {
        if (!player.shouldRenderSprite()) {
            return;
        }

        PlayerPose pose = selectPlayerPose();
        TextureRegion frame = frameForPlayerPose(pose);
        float poseHeight = heightForPlayerPose(pose);

        float poseWidth = poseHeight * frame.getRegionWidth() / frame.getRegionHeight();
        float idleBreath = pose == PlayerPose.IDLE ? MathUtils.sin(player.getAnimationTime() * 3f) : 0f;
        boolean groundStepPose = isGroundStepPose(pose) && player.isGrounded();
        float step = groundStepPose ? MathUtils.sin(player.getAnimationTime() * 13f) : 0f;
        float stepBounce = groundStepPose ? Math.abs(step) : 0f;
        float runBob = groundStepPose
                ? stepBounce * 2.2f
                : MathUtils.sin(player.getAnimationTime() * 3.4f) * (pose == PlayerPose.IDLE ? 1.8f : 1.2f);
        float squashX = pose == PlayerPose.DASH
                ? 1.06f
                : (groundStepPose ? 1f + stepBounce * 0.014f : 1f + idleBreath * 0.015f);
        float squashY = pose == PlayerPose.DASH
                ? 0.96f
                : (groundStepPose ? 1f - stepBounce * 0.01f : 1f - idleBreath * 0.01f);
        float rotation = pose == PlayerPose.HURT
                ? -player.getFacingDirection() * 5f
                : (groundStepPose
                ? step * 0.6f
                : idleBreath * 0.6f);

        float drawX = player.getCenterX() - poseWidth * 0.5f;
        float drawY = player.getY() - 11f + runBob;
        boolean flipX = shouldFlipPlayerPose(pose);

        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        if (pose == PlayerPose.HURT) {
            game.getBatch().setColor(1f, 0.88f, 0.86f, 1f);
        }
        game.getBatch().draw(playerSpriteSheet,
                drawX,
                drawY,
                poseWidth * 0.5f,
                18f,
                poseWidth,
                poseHeight,
                squashX,
                squashY,
                rotation,
                frame.getRegionX(),
                frame.getRegionY(),
                frame.getRegionWidth(),
                frame.getRegionHeight(),
                flipX,
                false);
        game.getBatch().setColor(Color.WHITE);
        game.getBatch().end();
    }

    private PlayerPose selectPlayerPose() {
        if (player.isHurtPoseActive()) {
            return PlayerPose.HURT;
        }
        if (player.isSpecialPoseActive()) {
            return PlayerPose.SPECIAL;
        }
        if (player.isShootPoseActive()) {
            if (player.isAirborne()) {
                return PlayerPose.AIR_SHOOT;
            }
            if (player.isMovingHorizontally()) {
                return PlayerPose.RUN_SHOOT;
            }
            return PlayerPose.SHOOT;
        }
        if (player.isDashing()) {
            return PlayerPose.DASH;
        }
        if (player.isAirborne()) {
            return PlayerPose.JUMP;
        }
        if (player.isMovingHorizontally()) {
            return PlayerPose.RUN;
        }
        return PlayerPose.IDLE;
    }

    private TextureRegion frameForPlayerPose(PlayerPose pose) {
        return switch (pose) {
            case HURT -> playerHurtFrame;
            case DASH, RUN -> playerWalkFrame();
            case SPECIAL -> playerSpecialFrame;
            case AIR_SHOOT -> playerAirShootFrame;
            case RUN_SHOOT -> playerRunShootFrame;
            case SHOOT -> playerShootFrame;
            case JUMP -> playerJumpFrame;
            case IDLE -> playerIdleFrame;
        };
    }

    private float heightForPlayerPose(PlayerPose pose) {
        return switch (pose) {
            case HURT -> 128f;
            case DASH -> 122f;
            case SPECIAL -> 126f;
            case AIR_SHOOT -> 128f;
            case RUN_SHOOT -> 122f;
            case SHOOT -> 120f;
            case JUMP -> 132f;
            case RUN -> 122f;
            case IDLE -> 126f;
        };
    }

    private boolean shouldFlipPlayerPose(PlayerPose pose) {
        if (pose == PlayerPose.SHOOT
                || pose == PlayerPose.AIR_SHOOT
                || pose == PlayerPose.RUN_SHOOT
                || pose == PlayerPose.JUMP) {
            return player.getFacingDirection() > 0;
        }
        return player.getFacingDirection() < 0;
    }

    private TextureRegion playerWalkFrame() {
        return ((int) (player.getAnimationTime() * 8f) & 1) == 0 ? playerRunFrame : playerRunAltFrame;
    }

    private boolean isGroundStepPose(PlayerPose pose) {
        return pose == PlayerPose.RUN || pose == PlayerPose.DASH || pose == PlayerPose.RUN_SHOOT;
    }

    private TextureRegion[] splitBossFrames(Texture sheet) {
        TextureRegion[][] split = TextureRegion.split(sheet, sheet.getWidth() / 4, sheet.getHeight() / 2);
        TextureRegion[] frames = new TextureRegion[8];
        int index = 0;
        for (TextureRegion[] row : split) {
            for (TextureRegion frame : row) {
                frames[index++] = frame;
            }
        }
        return frames;
    }

    private int selectBossFrame(String state) {
        if (!fightStarted) {
            return 0;
        }
        if (boss.isDefeated()) {
            return 7;
        }
        if ("Bote de cipó".equals(state)) {
            return 4;
        }
        if ("Mãos mágicas".equals(state)) {
            return 3;
        }
        if ("Chuva de pólen".equals(state)) {
            return 6;
        }
        return ((int) (elapsed * 4f) & 1) == 0 ? 1 : 2;
    }

    private void drawBossShadow(ShapeRenderer shapeRenderer) {
        float width = 330f;
        shapeRenderer.setColor(0.04f, 0.02f, 0.02f, 0.38f);
        shapeRenderer.ellipse(boss.getCenterX() - width * 0.5f - 16f, Constants.FLOOR_Y - 16f, width, 42f);
    }

    private void drawBossTelegraphGlow(ShapeRenderer shapeRenderer) {
        if (!boss.isTelegraphing() || boss.isDefeated()) {
            return;
        }

        float alpha = boss.getTelegraphAlpha();
        float radius = 118f + (1f - alpha) * 55f;
        float centerX = boss.getCenterX() - 30f;
        float centerY = Constants.FLOOR_Y + 360f;
        shapeRenderer.setColor(1f, 0.58f, 0.12f, 0.16f * alpha);
        shapeRenderer.circle(centerX, centerY, radius);
        shapeRenderer.setColor(1f, 0.94f, 0.38f, 0.16f * alpha);
        shapeRenderer.circle(centerX, centerY, radius * 0.58f);
    }

    private void renderUi() {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawPlayerHealth(170f, Constants.WORLD_HEIGHT - 58f);
        drawMeter(170f, Constants.WORLD_HEIGHT - 92f, 236f, 14f, player.getSpecialEnergyPercent(),
                player.isSpecialReady() ? Color.GOLD : Color.SKY);
        drawHealthBar(Constants.WORLD_WIDTH - 430f, Constants.WORLD_HEIGHT - 76f, 360f, 20f,
                boss.getHealth(), boss.getMaxHealth(), new Color(0.96f, 0.35f, 0.14f, 1f));
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        drawLeftTexture(hpLabelText, 70f, Constants.WORLD_HEIGHT - 58f, 0.48f);
        drawLeftTexture(specialLabelText, 70f, Constants.WORLD_HEIGHT - 92f, 0.46f);
        drawLeftTexture(bossNameText, Constants.WORLD_WIDTH - 430f, Constants.WORLD_HEIGHT - 30f, 0.48f);
        drawLeftTexture(getBossStateText(), Constants.WORLD_WIDTH - 430f, Constants.WORLD_HEIGHT - 100f, 0.46f);
        drawIntroOverlay();
        drawKnockoutOverlay();
        game.getBatch().end();
    }

    private void drawHealthBar(float x, float y, float width, float height, int value, int maxValue, Color fillColor) {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        float percent = maxValue == 0 ? 0f : (float) value / maxValue;

        shapeRenderer.setColor(0.02f, 0.02f, 0.03f, 1f);
        shapeRenderer.rect(x - 2f, y - 2f, width + 4f, height + 4f);

        shapeRenderer.setColor(0.23f, 0.23f, 0.25f, 1f);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, width * percent, height);
    }

    private void drawMeter(float x, float y, float width, float height, float percent, Color fillColor) {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        float clamped = MathUtils.clamp(percent, 0f, 1f);

        shapeRenderer.setColor(0.04f, 0.04f, 0.05f, 1f);
        shapeRenderer.rect(x - 2f, y - 2f, width + 4f, height + 4f);
        shapeRenderer.setColor(0.18f, 0.18f, 0.2f, 1f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, width * clamped, height);
    }

    private void drawPlayerHealth(float x, float y) {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        for (int i = 0; i < player.getMaxHealth(); i++) {
            float pipX = x + i * 34f;
            shapeRenderer.setColor(0.04f, 0.04f, 0.05f, 1f);
            shapeRenderer.circle(pipX, y, 13f);
            shapeRenderer.setColor(i < player.getHealth() ? new Color(0.96f, 0.18f, 0.16f, 1f) : new Color(0.28f, 0.27f, 0.27f, 1f));
            shapeRenderer.circle(pipX, y, 10f);
        }
    }

    private void drawIntroSpotlight(ShapeRenderer shapeRenderer) {
        float pulse = (MathUtils.sin(elapsed * 8f) + 1f) * 0.5f;
        shapeRenderer.setColor(1f, 0.93f, 0.54f, 0.16f + pulse * 0.08f);
        shapeRenderer.triangle(820f, Constants.WORLD_HEIGHT, 1160f, Constants.WORLD_HEIGHT,
                boss.getCenterX(), Constants.FLOOR_Y + 4f);
    }

    private void drawIntroOverlay() {
        if (fightStarted) {
            return;
        }

        if (introTimer < Constants.INTRO_READY_DURATION) {
            float pop = MathUtils.clamp(introTimer / 0.18f, 0f, 1f);
            float wobble = MathUtils.sin(elapsed * 18f) * 2.2f;
            drawCenteredTexture(readyText, Constants.WORLD_HEIGHT * 0.58f, 0.9f + pop * 0.18f, wobble, 0f, 1f);
        } else {
            float goElapsed = introTimer - Constants.INTRO_READY_DURATION;
            float pop = MathUtils.clamp(goElapsed / 0.15f, 0f, 1f);
            float scale = 0.98f + pop * 0.28f + MathUtils.sin(elapsed * 24f) * 0.035f;
            drawCenteredTexture(goText, Constants.WORLD_HEIGHT * 0.58f, scale, -MathUtils.sin(elapsed * 20f) * 1.6f,
                    MathUtils.sin(elapsed * 9f) * 1.8f, 1f);
        }
    }

    private void drawCenteredTexture(Texture texture, float centerY, float scale, float xOffset, float rotation,
                                     float alpha) {
        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;
        float x = (Constants.WORLD_WIDTH - width) * 0.5f + xOffset;
        float y = centerY - height * 0.5f;
        game.getBatch().setColor(1f, 1f, 1f, alpha);
        game.getBatch().draw(texture,
                x,
                y,
                width * 0.5f,
                height * 0.5f,
                width,
                height,
                1f,
                1f,
                rotation,
                0,
                0,
                texture.getWidth(),
                texture.getHeight(),
                false,
                false);
        game.getBatch().setColor(Color.WHITE);
    }

    private void drawLeftTexture(Texture texture, float x, float centerY, float scale) {
        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;
        float y = centerY - height * 0.5f;
        game.getBatch().draw(texture, x, y, width, height);
    }

    private Texture getBossStateText() {
        return switch (boss.getStateName()) {
            case "Bote de cipó" -> bossVineText;
            case "Mãos mágicas" -> bossHandsText;
            case "Chuva de pólen" -> bossPollenText;
            case "Derrotado" -> bossDefeatedText;
            default -> bossPreparingText;
        };
    }

    private void updateIntro(float delta) {
        if (fightStarted) {
            return;
        }

        introTimer = Math.min(Constants.INTRO_TOTAL_DURATION, introTimer + delta);

        if (!readyCuePlayed) {
            readyCuePlayed = true;
            game.getAudioManager().playCue(AudioManager.Cue.READY);
        }

        if (!goCuePlayed && introTimer >= Constants.INTRO_READY_DURATION) {
            goCuePlayed = true;
            game.getAudioManager().playCue(AudioManager.Cue.GO);
        }

        if (introTimer >= Constants.INTRO_TOTAL_DURATION) {
            fightStarted = true;
            game.getAudioManager().playMusic(BATTLE_MUSIC_PATH, true, BATTLE_MUSIC_VOLUME);
            boss.showTelegraph(new Color(1f, 0.38f, 0.12f, 1f), 0.35f);
        }
    }

    private void beginKnockoutSequence() {
        knockoutSequenceActive = true;
        knockoutTimer = 0f;
        knockoutParticleTimer = 0f;
        projectileSystem.clear();
        game.getAudioManager().stopMusic();
        game.getAudioManager().playVoice(KNOCKOUT_NARRATION_PATH, KNOCKOUT_NARRATION_VOLUME);
        spawnKnockoutExplosion();
        requestShake(16f, 0.46f);
    }

    private void playBossSoundEvents() {
        BossSoundEvent soundEvent;
        while ((soundEvent = boss.pollSoundEvent()) != null) {
            AudioManager.Cue cue = switch (soundEvent) {
                case VINE_CHARGE -> AudioManager.Cue.BOSS_VINE_CHARGE;
                case VINE_STRIKE -> AudioManager.Cue.BOSS_VINE_STRIKE;
                case MAGIC_CHARGE -> AudioManager.Cue.BOSS_MAGIC_CHARGE;
                case MAGIC_VOLLEY -> AudioManager.Cue.BOSS_MAGIC_VOLLEY;
                case POLLEN_CHARGE -> AudioManager.Cue.BOSS_POLLEN_CHARGE;
                case POLLEN_DROP -> AudioManager.Cue.BOSS_POLLEN_DROP;
            };
            game.getAudioManager().playCue(cue);
        }
    }

    private boolean updateKnockoutSequence(float delta) {
        knockoutTimer += delta;
        hitstopTimer = 0f;
        player.update(delta, false, false, false, false);

        if (knockoutTimer < KNOCKOUT_PARTICLE_DURATION) {
            knockoutParticleTimer -= delta;
            while (knockoutParticleTimer <= 0f) {
                spawnKnockoutExplosion();
                knockoutParticleTimer += KNOCKOUT_PARTICLE_INTERVAL;
            }
        }

        if (knockoutTimer >= KNOCKOUT_DURATION) {
            game.showEndScreen(true);
            return false;
        }

        return true;
    }

    private void spawnKnockoutExplosion() {
        particleSystem.spawnBossDefeatBurst(boss.getCenterX() - 20f, Constants.FLOOR_Y + 265f);
        game.getAudioManager().playCue(AudioManager.Cue.BOSS_DEFEAT_EXPLOSION);
    }

    private void drawKnockoutOverlay() {
        if (!knockoutSequenceActive || knockoutTimer < KNOCKOUT_TEXT_ONSET) {
            return;
        }

        float textTimer = knockoutTimer - KNOCKOUT_TEXT_ONSET;
        float alpha = 1f - MathUtils.clamp(
                (knockoutTimer - KNOCKOUT_TEXT_FADE_START) / KNOCKOUT_TEXT_FADE_DURATION,
                0f,
                1f);
        float slam = MathUtils.clamp(textTimer / 0.18f, 0f, 1f);
        float pulse = MathUtils.sin(elapsed * 11f) * 0.025f;
        float scale = 0.88f + slam * 0.14f + pulse;
        float rotation = MathUtils.sin(elapsed * 6.5f) * 1.1f * alpha;
        drawCenteredTexture(knockoutText, Constants.WORLD_HEIGHT * 0.6f, scale, 0f, rotation, alpha);
    }

    private void requestShake(float magnitude, float duration) {
        shakeMagnitude = Math.max(shakeMagnitude, magnitude);
        shakeDuration = Math.max(0.01f, duration);
        shakeTimer = Math.max(shakeTimer, duration);
    }

    private void applyCameraShake() {
        float shakeAlpha = shakeDuration <= 0f ? 0f : MathUtils.clamp(shakeTimer / shakeDuration, 0f, 1f);
        float offsetX = shakeAlpha > 0f ? MathUtils.random(-shakeMagnitude, shakeMagnitude) * shakeAlpha : 0f;
        float offsetY = shakeAlpha > 0f ? MathUtils.random(-shakeMagnitude, shakeMagnitude) * shakeAlpha : 0f;
        camera.position.set(Constants.WORLD_WIDTH * 0.5f + offsetX, Constants.WORLD_HEIGHT * 0.5f + offsetY, 0f);
        camera.update();
        if (shakeTimer <= 0f) {
            shakeMagnitude = 0f;
        }
    }
}
