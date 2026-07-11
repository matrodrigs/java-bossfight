package com.bossfight.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.bossfight.boss.Boss;
import com.bossfight.boss.BossSoundEvent;
import com.bossfight.config.Constants;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.input.BattleInput;
import com.bossfight.rendering.BattleHud;
import com.bossfight.rendering.BossRenderer;
import com.bossfight.rendering.PlayerRenderer;
import com.bossfight.systems.AudioManager;
import com.bossfight.systems.CameraShake;
import com.bossfight.systems.CollisionSystem;
import com.bossfight.systems.ParticleSystem;
import com.bossfight.systems.PhaseShockwaveEffect;
import com.bossfight.systems.ProjectileRenderer;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.systems.RetroTextFactory;
import com.bossfight.systems.VintageFloralBackground;

public class BattleScreen extends ScreenAdapter {
    private final GameContext game;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final RetroTextFactory textFactory;
    private final BattleFlow battleFlow;
    private final Player player;
    private final Boss boss;
    private final PlayerRenderer playerRenderer;
    private final BossRenderer bossRenderer;
    private final BattleHud battleHud;
    private final VintageFloralBackground background;
    private final ProjectileSystem projectileSystem;
    private final ProjectileRenderer projectileRenderer;
    private final ParticleSystem particleSystem;
    private final CollisionSystem collisionSystem;
    private final BattleInput battleInput;
    private final ControlsOverlay controlsOverlay;
    private final CameraShake cameraShake;
    private final PhaseShockwaveEffect phaseShockwaveEffect;
    private float elapsed;
    private float hitstopTimer;

    public BattleScreen(GameContext game, boolean introPausedForTransition) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        textFactory = new RetroTextFactory();
        battleFlow = new BattleFlow(game, textFactory, introPausedForTransition);
        player = new Player();
        boss = new Boss();
        playerRenderer = new PlayerRenderer();
        bossRenderer = new BossRenderer();
        battleHud = new BattleHud(textFactory);
        background = new VintageFloralBackground();
        projectileSystem = new ProjectileSystem();
        projectileRenderer = new ProjectileRenderer(projectileSystem);
        particleSystem = new ParticleSystem();
        collisionSystem = new CollisionSystem(particleSystem, game.getAudioManager());
        battleInput = new BattleInput();
        controlsOverlay = new ControlsOverlay(textFactory);
        cameraShake = new CameraShake();
        phaseShockwaveEffect = new PhaseShockwaveEffect();
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
        cameraShake.apply(camera, elapsed);
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
        bossRenderer.dispose();
        playerRenderer.dispose();
        battleHud.dispose();
        background.dispose();
        textFactory.dispose();
        projectileSystem.clear();
        projectileRenderer.dispose();
        particleSystem.clear();
    }

    public void startIntroAfterTransition() {
        battleFlow.startIntroAfterTransition();
    }

    private boolean update(float delta) {
        if (returnToMenuRequested()) {
            return false;
        }

        updateVisualEffects(delta);
        if (battleFlow.isIntroPausedForTransition()) {
            return true;
        }

        battleFlow.updateIntro(delta, controlsOverlay, boss);
        controlsOverlay.update(delta);
        if (updateNonCombatSequence(delta)) {
            return true;
        }

        battleInput.poll(delta);
        if (updateHitstop(delta)) {
            return true;
        }

        updatePlayer(delta);
        updateCombat(delta);
        resolveBattleOutcome();
        return true;
    }

    private boolean returnToMenuRequested() {
        if (battleFlow.isIntroPausedForTransition() || !Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            return false;
        }

        game.getAudioManager().playCue(AudioManager.Cue.MENU_BACK);
        game.showMenuScreen();
        return true;
    }

    private void updateVisualEffects(float delta) {
        elapsed += delta;
        cameraShake.update(delta);
        particleSystem.update(delta);
        phaseShockwaveEffect.update(delta);
    }

    private boolean updateNonCombatSequence(float delta) {
        if (battleFlow.isKnockoutSequenceActive()) {
            hitstopTimer = 0f;
            battleFlow.updateKnockoutSequence(delta, player, particleSystem, boss);
            return true;
        }
        if (!battleFlow.isFightStarted()) {
            player.update(delta, false, false, false, false);
            return true;
        }
        return false;
    }

    private boolean updateHitstop(float delta) {
        if (hitstopTimer <= 0f) {
            return false;
        }

        hitstopTimer = Math.max(0f, hitstopTimer - delta);
        return true;
    }

    private void updatePlayer(float delta) {
        boolean jump = battleInput.consumeJump();
        boolean dash = battleInput.consumeDash();
        boolean special = battleInput.consumeSpecial();

        player.update(delta, battleInput.isMoveLeftHeld(), battleInput.isMoveRightHeld(), jump, dash);
        playMovementFeedback();
        firePlayerProjectile(special);
    }

    private void playMovementFeedback() {
        if (player.consumeDashStarted()) {
            particleSystem.spawnDash(player.getCenterX(), player.getCenterY(), player.getFacingDirection());
            game.getAudioManager().playCue(AudioManager.Cue.DASH);
            cameraShake.request(2.4f, 0.09f);
        }
        if (player.consumeLanded()) {
            particleSystem.spawnLandingDust(player.getCenterX(), Constants.FLOOR_Y);
            cameraShake.request(1.35f, 0.08f);
        }
    }

    private void firePlayerProjectile(boolean specialRequested) {
        Projectile projectile;
        boolean special;
        if (specialRequested) {
            projectile = player.tryShootSpecial();
            special = true;
        } else if (battleInput.isShootHeld()) {
            projectile = player.tryShoot();
            special = false;
        } else {
            return;
        }

        if (projectile == null) {
            return;
        }
        projectileSystem.addProjectile(projectile);
        particleSystem.spawnMuzzle(
                projectile.getCenterX(), projectile.getCenterY(), player.getFacingDirection(), special);
        game.getAudioManager().playCue(special ? AudioManager.Cue.PLAYER_SPECIAL : AudioManager.Cue.PLAYER_SHOOT);
        if (special) {
            cameraShake.request(6f, 0.16f);
        }
    }

    private void updateCombat(float delta) {
        boss.update(delta, projectileSystem, player);
        projectileSystem.update(delta);
        collisionSystem.resolve(player, boss, projectileSystem, delta);
        applyCollisionFeedback();
    }

    private void applyCollisionFeedback() {
        float requestedHitstop = collisionSystem.consumeRequestedHitstop();
        if (requestedHitstop > 0f) {
            hitstopTimer = Math.max(hitstopTimer, requestedHitstop);
        }
        float requestedShake = collisionSystem.consumeRequestedShake();
        if (requestedShake > 0f) {
            cameraShake.request(requestedShake, 0.18f);
        }
    }

    private void resolveBattleOutcome() {
        if (boss.isDefeated()) {
            battleFlow.beginKnockoutSequence(projectileSystem, particleSystem, boss, cameraShake);
            return;
        }

        playBossSoundEvents();
        if (player.isDead()) {
            battleFlow.requestEndTransition(false);
        }
    }

    private void renderWorld() {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        background.renderBack(game.getBatch(), camera, elapsed);

        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (!battleFlow.isFightStarted()) {
            battleFlow.renderIntroSpotlight(shapeRenderer, boss, elapsed);
        }

        playerRenderer.renderShadow(shapeRenderer, player);
        bossRenderer.renderShadow(shapeRenderer, boss);
        bossRenderer.renderTelegraphGlow(shapeRenderer, boss);
        projectileRenderer.renderWarnings(shapeRenderer);

        shapeRenderer.end();

        if (phaseShockwaveEffect.isActive()) {
            Gdx.gl.glLineWidth(4f);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            phaseShockwaveEffect.render(shapeRenderer, boss.getCenterX() - 42f, Constants.FLOOR_Y + 340f);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1f);
        }

        bossRenderer.render(game.getBatch(), camera, boss, player, battleFlow.isFightStarted(), elapsed);
        playerRenderer.render(game.getBatch(), camera, player);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        particleSystem.render(shapeRenderer);
        shapeRenderer.end();

        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        projectileRenderer.renderSprites(game.getBatch());
        game.getBatch().end();

        background.renderForeground(game.getBatch(), camera, elapsed);

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderUi() {
        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        battleHud.render(game.getBatch(), player, elapsed);
        battleFlow.renderOverlays(game.getBatch(), elapsed);
        game.getBatch().end();
        controlsOverlay.render(game.getBatch(), game.getShapeRenderer(), camera, elapsed);
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
                cameraShake.request(14f, 0.42f);
                phaseShockwaveEffect.spawn(1.2f);
            } else if (soundEvent == BossSoundEvent.PHASE_SHOCKWAVE) {
                cameraShake.request(10f, 0.24f);
                phaseShockwaveEffect.spawn(1f);
            }
        }
    }

}
