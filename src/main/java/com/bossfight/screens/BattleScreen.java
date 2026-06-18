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
import com.bossfight.boss.BossVisualState;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.AudioManager;
import com.bossfight.systems.CollisionSystem;
import com.bossfight.systems.ParticleSystem;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.systems.RetroTextFactory;
import com.bossfight.systems.TextureDraw;
import com.bossfight.systems.VintageFloralBackground;
import com.bossfight.Constants;

public class BattleScreen extends ScreenAdapter {
    private static final String BATTLE_MUSIC_PATH = "audio/music/boss_fight_theme.mp3";
    private static final String INTRO_NARRATION_PATH = "audio/voice/narrator_intro.wav";
    private static final String KNOCKOUT_NARRATION_PATH = "audio/voice/narrator_knockout.wav";
    private static final float BATTLE_MUSIC_VOLUME = 0.04f;
    private static final float INTRO_NARRATION_VOLUME = 1f;
    private static final float KNOCKOUT_NARRATION_VOLUME = 1.3f;
    private static final float INTRO_READY_TEXT_ONSET = 0.1f;
    private static final float KNOCKOUT_DURATION = 3.2f;
    private static final float KNOCKOUT_TEXT_ONSET = 0.1f;
    private static final float KNOCKOUT_TEXT_FADE_START = 1.2f;
    private static final float KNOCKOUT_TEXT_FADE_DURATION = 0.35f;
    private static final float KNOCKOUT_PARTICLE_DURATION = 2.35f;
    private static final float KNOCKOUT_PARTICLE_INTERVAL = 0.11f;
    private static final String HP_BOX_PATH = "sprites/ui/player_hp_box.png";
    private static final String SPECIAL_CLOCK_PATH = "sprites/ui/special_clock.png";
    private static final float PLAYER_HUD_X = 34f;
    private static final float PLAYER_HUD_Y = 24f;
    private static final float HP_BOX_WIDTH = 106f;
    private static final float HP_BOX_HEIGHT = 58f;
    private static final float HP_TEXT_PADDING_X = 11f;
    private static final float HP_TEXT_PADDING_Y = 8f;
    private static final float PLAYER_HUD_GAP = 3f;
    private static final float SPECIAL_CLOCK_SIZE = 70f;
    private static final float SPECIAL_CLOCK_X = PLAYER_HUD_X + HP_BOX_WIDTH + PLAYER_HUD_GAP;
    private static final float SPECIAL_CLOCK_Y = PLAYER_HUD_Y + (HP_BOX_HEIGHT - SPECIAL_CLOCK_SIZE) * 0.5f;
    private static final int MAX_PHASE_SHOCKWAVES = 8;
    private static final float PHASE_SHOCKWAVE_DURATION = 0.72f;
    private static final float PHASE_SHOCKWAVE_START_RADIUS_X = 48f;
    private static final float PHASE_SHOCKWAVE_END_RADIUS_X = 650f;
    private static final float PHASE_SHOCKWAVE_START_RADIUS_Y = 18f;
    private static final float PHASE_SHOCKWAVE_END_RADIUS_Y = 150f;

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
    private final Texture[] hpTexts;
    private final Texture hpBoxTexture;
    private final Texture specialClockTexture;
    private final TextureRegion specialClockFillRegion;
    private final Player player;
    private final Boss boss;
    private final VintageFloralBackground background;
    private final ProjectileSystem projectileSystem;
    private final ParticleSystem particleSystem;
    private final CollisionSystem collisionSystem;
    private final float[] phaseShockwaveTimers = new float[MAX_PHASE_SHOCKWAVES];
    private final float[] phaseShockwaveStrengths = new float[MAX_PHASE_SHOCKWAVES];
    private float elapsed;
    private float introTimer;
    private float hitstopTimer;
    private float shakeTimer;
    private float shakeDuration;
    private float shakeMagnitude;
    private float knockoutTimer;
    private float knockoutParticleTimer;
    private boolean introVoicePlayed;
    private boolean fightStarted;
    private boolean introPausedForTransition;
    private boolean knockoutSequenceActive;
    private boolean endTransitionRequested;

    public BattleScreen(MainGame game, boolean introPausedForTransition) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        bossSpriteSheet = loadTexture("sprites/boss/flower_boss_sheet.png");
        bossFrames = splitBossFrames(bossSpriteSheet);
        playerSpriteSheet = loadTexture("sprites/player/clock_player_sheet.png");
        hpBoxTexture = loadTexture(HP_BOX_PATH);
        specialClockTexture = loadTexture(SPECIAL_CLOCK_PATH);
        specialClockFillRegion = new TextureRegion(specialClockTexture);
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
        hpTexts = createHpTexts();
        player = new Player();
        boss = new Boss();
        background = new VintageFloralBackground();
        projectileSystem = new ProjectileSystem();
        particleSystem = new ParticleSystem();
        collisionSystem = new CollisionSystem();
        this.introPausedForTransition = introPausedForTransition;
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
        hpBoxTexture.dispose();
        specialClockTexture.dispose();
        background.dispose();
        textFactory.dispose();
        projectileSystem.dispose();
        particleSystem.clear();
    }

    public void startIntroAfterTransition() {
        if (!introPausedForTransition) {
            return;
        }

        introPausedForTransition = false;
        introTimer = 0f;
        introVoicePlayed = false;
        fightStarted = false;
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private Texture[] createHpTexts() {
        Texture[] valueTexts = new Texture[Constants.PLAYER_MAX_HEALTH + 1];
        for (int i = 0; i < valueTexts.length; i++) {
            valueTexts[i] = textFactory.createPlayerHealthHud(i);
        }
        return valueTexts;
    }

    private TextureRegion playerFrame(int x, int y, int width, int height) {
        return new TextureRegion(playerSpriteSheet, x, y, width, height);
    }

    private boolean update(float delta) {
        if (!introPausedForTransition && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.getAudioManager().playCue(AudioManager.Cue.MENU_BACK);
            game.showMenuScreen();
            return false;
        }

        elapsed += delta;
        shakeTimer = Math.max(0f, shakeTimer - delta);
        particleSystem.update(delta);
        updatePhaseShockwaves(delta);

        if (introPausedForTransition) {
            return true;
        }

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
            requestEndTransition(false);
            return true;
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
        projectileSystem.renderWarnings(shapeRenderer);

        shapeRenderer.end();

        if (hasActivePhaseShockwave()) {
            Gdx.gl.glLineWidth(4f);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            drawPhaseShockwaves(shapeRenderer);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1f);
        }

        renderBossSprite();
        renderPlayerSprite();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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
        BossVisualState state = boss.getVisualState();
        boolean defeated = boss.isDefeated();
        boolean vineStrike = state == BossVisualState.VINE_STRIKE;
        boolean magicHands = state == BossVisualState.MAGIC_HANDS;
        boolean pollenRain = state == BossVisualState.POLLEN_RAIN;
        boolean pollenBreath = state == BossVisualState.POLLEN_BREATH;
        boolean phaseTransition = state == BossVisualState.ENRAGING;
        TextureRegion frame = bossFrames[selectBossFrame(state)];

        float breath = defeated ? 0f : MathUtils.sin(elapsed * 3.4f);
        float windup = boss.isTelegraphing() ? 1f - boss.getTelegraphAlpha() : 0f;
        float attackPulse = !defeated && (vineStrike || magicHands || pollenRain || pollenBreath || phaseTransition)
                ? MathUtils.sin(elapsed * 9.5f)
                : 0f;
        float visualHeight = 506f + breath * 5f + (boss.isPhaseTwo() ? 18f : 0f);
        visualHeight += vineStrike ? windup * 18f : 0f;
        visualHeight += phaseTransition ? windup * 24f : 0f;
        float visualWidth = visualHeight * frame.getRegionWidth() / frame.getRegionHeight();

        float x = boss.getCenterX() - visualWidth * 0.5f - 18f;
        x += vineStrike ? attackPulse * 8f - 18f - windup * 18f : 0f;
        x += magicHands ? MathUtils.sin(elapsed * 7f) * 5f - windup * 10f : 0f;
        x += pollenBreath ? -windup * 18f + attackPulse * 4f : 0f;

        float y = Constants.FLOOR_Y - 30f;
        y += breath * 2.5f;
        y += pollenRain ? MathUtils.sin(elapsed * 8.5f) * 6f : 0f;
        y += phaseTransition ? attackPulse * 6f : 0f;

        float scaleX = 1f + breath * 0.014f + (magicHands ? attackPulse * 0.02f : 0f);
        float scaleY = 1f - breath * 0.01f + (vineStrike ? attackPulse * 0.028f : 0f);
        float rotation = MathUtils.sin(elapsed * 2.1f) * 0.8f
                + attackPulse * (pollenRain || phaseTransition ? 1.4f : 0.45f);
        boolean flipX = pollenBreath && player.getCenterX() < boss.getCenterX();

        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        if (boss.isPhaseTwo()) {
            game.getBatch().setColor(1f, 0.92f, 0.92f, 1f);
        }
        game.getBatch().draw(bossSpriteSheet,
                x,
                y,
                visualWidth * 0.48f,
                44f,
                visualWidth,
                visualHeight,
                scaleX,
                scaleY,
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
        if (pose == PlayerPose.HURT
                || pose == PlayerPose.SHOOT
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

    private int selectBossFrame(BossVisualState state) {
        if (!fightStarted) {
            return 0;
        }
        if (boss.isDefeated()) {
            return 7;
        }
        return switch (state) {
            case ENRAGING, MAGIC_HANDS -> 3;
            case VINE_STRIKE -> 4;
            case POLLEN_BREATH -> 5;
            case POLLEN_RAIN -> 6;
            case DEFEATED -> 7;
            case IDLE -> ((int) (elapsed * 4f) & 1) == 0 ? 1 : 2;
        };
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
        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        drawPlayerHealthBox();
        drawPlayerHealthText();
        drawSpecialClock();
        drawIntroOverlay();
        drawKnockoutOverlay();
        game.getBatch().end();
    }

    private void drawPlayerHealthBox() {
        game.getBatch().draw(hpBoxTexture, PLAYER_HUD_X, PLAYER_HUD_Y, HP_BOX_WIDTH, HP_BOX_HEIGHT);
    }

    private void drawPlayerHealthText() {
        int healthIndex = MathUtils.clamp(player.getHealth(), 0, hpTexts.length - 1);
        drawTextureInBox(hpTexts[healthIndex],
                PLAYER_HUD_X + HP_TEXT_PADDING_X,
                PLAYER_HUD_Y + HP_TEXT_PADDING_Y,
                HP_BOX_WIDTH - HP_TEXT_PADDING_X * 2f,
                HP_BOX_HEIGHT - HP_TEXT_PADDING_Y * 2f);
    }

    private void drawSpecialClock() {
        if (player.isSpecialReady()) {
            drawReadySpecialClock();
            return;
        }

        float percent = MathUtils.clamp(player.getSpecialEnergyPercent(), 0f, 1f);

        game.getBatch().setColor(0.28f, 0.25f, 0.2f, 0.48f);
        game.getBatch().draw(specialClockTexture, SPECIAL_CLOCK_X, SPECIAL_CLOCK_Y,
                SPECIAL_CLOCK_SIZE, SPECIAL_CLOCK_SIZE);

        if (percent > 0.01f) {
            int sourceHeight = Math.max(1, Math.round(specialClockTexture.getHeight() * percent));
            int sourceY = specialClockTexture.getHeight() - sourceHeight;
            float fillHeight = SPECIAL_CLOCK_SIZE * sourceHeight / specialClockTexture.getHeight();
            specialClockFillRegion.setRegion(0, sourceY, specialClockTexture.getWidth(), sourceHeight);
            game.getBatch().setColor(1f, 1f, 1f, 0.96f);
            game.getBatch().draw(specialClockFillRegion, SPECIAL_CLOCK_X, SPECIAL_CLOCK_Y,
                    SPECIAL_CLOCK_SIZE, fillHeight);
        }

        game.getBatch().setColor(Color.WHITE);
    }

    private void drawReadySpecialClock() {
        float bob = MathUtils.sin(elapsed * 10f) * 3.4f;
        float rotation = MathUtils.sin(elapsed * 13f) * 6.5f;
        float scale = 1f + MathUtils.sin(elapsed * 16f) * 0.045f;
        float size = SPECIAL_CLOCK_SIZE * scale;
        float x = SPECIAL_CLOCK_X + (SPECIAL_CLOCK_SIZE - size) * 0.5f;
        float y = SPECIAL_CLOCK_Y + (SPECIAL_CLOCK_SIZE - size) * 0.5f + bob;

        game.getBatch().setColor(0.02f, 0.015f, 0.01f, 0.32f);
        drawClockTexture(x + 3f, y - 4f, size, rotation);
        game.getBatch().setColor(Color.WHITE);
        drawClockTexture(x, y, size, rotation);
    }

    private void drawClockTexture(float x, float y, float size, float rotation) {
        game.getBatch().draw(specialClockTexture,
                x,
                y,
                size * 0.5f,
                size * 0.5f,
                size,
                size,
                1f,
                1f,
                rotation,
                0,
                0,
                specialClockTexture.getWidth(),
                specialClockTexture.getHeight(),
                false,
                false);
    }

    private void drawTextureInBox(Texture texture, float x, float y, float width, float height) {
        float scale = Math.min(width / texture.getWidth(), height / texture.getHeight());
        float drawWidth = texture.getWidth() * scale;
        float drawHeight = texture.getHeight() * scale;
        game.getBatch().draw(texture,
                x + (width - drawWidth) * 0.5f,
                y + (height - drawHeight) * 0.5f,
                drawWidth,
                drawHeight);
    }

    private void drawIntroSpotlight(ShapeRenderer shapeRenderer) {
        float pulse = (MathUtils.sin(elapsed * 8f) + 1f) * 0.5f;
        shapeRenderer.setColor(1f, 0.93f, 0.54f, 0.16f + pulse * 0.08f);
        shapeRenderer.triangle(820f, Constants.WORLD_HEIGHT, 1160f, Constants.WORLD_HEIGHT,
                boss.getCenterX(), Constants.FLOOR_Y + 4f);
    }

    private void drawIntroOverlay() {
        if (fightStarted || introPausedForTransition) {
            return;
        }

        if (introTimer < Constants.INTRO_READY_DURATION) {
            float readyElapsed = introTimer - INTRO_READY_TEXT_ONSET;
            if (readyElapsed < 0f) {
                return;
            }

            float pop = MathUtils.clamp(readyElapsed / 0.18f, 0f, 1f);
            float wobble = MathUtils.sin(elapsed * 18f) * 2.2f;
            TextureDraw.centeredAnimated(game.getBatch(), readyText, Constants.WORLD_WIDTH * 0.5f,
                    Constants.WORLD_HEIGHT * 0.58f, 0.9f + pop * 0.18f, wobble, 0f, 1f);
        } else {
            float goElapsed = introTimer - Constants.INTRO_READY_DURATION;
            float pop = MathUtils.clamp(goElapsed / 0.15f, 0f, 1f);
            float scale = 0.98f + pop * 0.28f + MathUtils.sin(elapsed * 24f) * 0.035f;
            TextureDraw.centeredAnimated(game.getBatch(), goText, Constants.WORLD_WIDTH * 0.5f,
                    Constants.WORLD_HEIGHT * 0.58f, scale, -MathUtils.sin(elapsed * 20f) * 1.6f,
                    MathUtils.sin(elapsed * 9f) * 1.8f, 1f);
        }
    }

    private void updateIntro(float delta) {
        if (fightStarted || introPausedForTransition) {
            return;
        }

        if (!introVoicePlayed) {
            introVoicePlayed = true;
            game.getAudioManager().playVoice(INTRO_NARRATION_PATH, INTRO_NARRATION_VOLUME);
        }

        introTimer = Math.min(Constants.INTRO_TOTAL_DURATION, introTimer + delta);

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
                case PHASE_ROAR -> AudioManager.Cue.BOSS_PHASE_ROAR;
                case PHASE_SHOCKWAVE -> AudioManager.Cue.BOSS_PHASE_SHOCKWAVE;
            };
            game.getAudioManager().playCue(cue);
            if (soundEvent == BossSoundEvent.PHASE_ROAR) {
                requestShake(14f, 0.42f);
                spawnPhaseShockwave(1.2f);
            } else if (soundEvent == BossSoundEvent.PHASE_SHOCKWAVE) {
                requestShake(10f, 0.24f);
                spawnPhaseShockwave(1f);
            }
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
            requestEndTransition(true);
        }

        return true;
    }

    private void requestEndTransition(boolean victory) {
        if (endTransitionRequested) {
            return;
        }

        endTransitionRequested = true;
        if (!victory) {
            game.getAudioManager().playCue(AudioManager.Cue.DEFEAT);
        }
        game.showEndScreen(victory);
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
        TextureDraw.centeredAnimated(game.getBatch(), knockoutText, Constants.WORLD_WIDTH * 0.5f,
                Constants.WORLD_HEIGHT * 0.6f, scale, 0f, rotation, alpha);
    }

    private void requestShake(float magnitude, float duration) {
        shakeMagnitude = Math.max(shakeMagnitude, magnitude);
        shakeDuration = Math.max(0.01f, duration);
        shakeTimer = Math.max(shakeTimer, duration);
    }

    private void updatePhaseShockwaves(float delta) {
        for (int i = 0; i < phaseShockwaveTimers.length; i++) {
            phaseShockwaveTimers[i] = Math.max(0f, phaseShockwaveTimers[i] - delta);
        }
    }

    private void spawnPhaseShockwave(float strength) {
        int slot = 0;
        float lowestTimer = phaseShockwaveTimers[0];
        for (int i = 0; i < phaseShockwaveTimers.length; i++) {
            if (phaseShockwaveTimers[i] <= 0f) {
                slot = i;
                break;
            }
            if (phaseShockwaveTimers[i] < lowestTimer) {
                lowestTimer = phaseShockwaveTimers[i];
                slot = i;
            }
        }

        phaseShockwaveTimers[slot] = PHASE_SHOCKWAVE_DURATION;
        phaseShockwaveStrengths[slot] = strength;
    }

    private boolean hasActivePhaseShockwave() {
        for (float timer : phaseShockwaveTimers) {
            if (timer > 0f) {
                return true;
            }
        }
        return false;
    }

    private void drawPhaseShockwaves(ShapeRenderer shapeRenderer) {
        float centerX = boss.getCenterX() - 42f;
        float centerY = Constants.FLOOR_Y + 340f;

        for (int i = 0; i < phaseShockwaveTimers.length; i++) {
            float timer = phaseShockwaveTimers[i];
            if (timer <= 0f) {
                continue;
            }

            float progress = 1f - MathUtils.clamp(timer / PHASE_SHOCKWAVE_DURATION, 0f, 1f);
            float strength = phaseShockwaveStrengths[i];
            float eased = 1f - (1f - progress) * (1f - progress);
            float radiusX = MathUtils.lerp(PHASE_SHOCKWAVE_START_RADIUS_X, PHASE_SHOCKWAVE_END_RADIUS_X, eased) * strength;
            float radiusY = MathUtils.lerp(PHASE_SHOCKWAVE_START_RADIUS_Y, PHASE_SHOCKWAVE_END_RADIUS_Y, eased) * strength;
            float alpha = (1f - progress) * (1f - progress) * 0.5f;

            shapeRenderer.setColor(1f, 0.9f, 0.36f, alpha);
            shapeRenderer.ellipse(centerX - radiusX, centerY - radiusY, radiusX * 2f, radiusY * 2f);
            shapeRenderer.setColor(1f, 0.28f, 0.12f, alpha * 0.42f);
            shapeRenderer.ellipse(centerX - radiusX * 0.72f, centerY - radiusY * 0.72f,
                    radiusX * 1.44f, radiusY * 1.44f);
        }
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
