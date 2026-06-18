package com.bossfight.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.Constants;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.ProjectileSystem;

public class AttackFiveState implements BossState {
    private static final int LANE_COUNT = 8;
    private static final int TOTAL_VINES = 8;
    private static final int MAX_PENDING_VINES = 3;
    private static final int MAX_SPORES = 8;
    private static final float FIRST_VINE_DELAY = 0.12f;
    private static final float VINE_INTERVAL = 0.28f;
    private static final float VINE_WARNING_TIME = 0.62f;
    private static final float VINE_ACTIVE_TIME = 0.36f;
    private static final float VINE_WIDTH = 52f;
    private static final float VINE_HEIGHT = 292f;
    private static final float WARNING_WIDTH = 78f;
    private static final float SPORE_INTERVAL = 0.34f;
    private static final float MIN_SPORE_DISTANCE_FROM_VINE = 100f;
    private static final float MIN_DURATION = 2.85f;

    private final float[] pendingTimers = new float[MAX_PENDING_VINES];
    private final float[] pendingXs = new float[MAX_PENDING_VINES];
    private float elapsed;
    private float vineTimer;
    private float sporeTimer;
    private int pendingCount;
    private int vinesQueued;
    private int sporesSpawned;
    private int lastLane;

    @Override
    public BossVisualState getVisualState() {
        return BossVisualState.VINE_STRIKE;
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        vineTimer = FIRST_VINE_DELAY;
        sporeTimer = 0.16f;
        pendingCount = 0;
        vinesQueued = 0;
        sporesSpawned = 0;
        lastLane = -1;
        boss.emitSound(BossSoundEvent.VINE_CHARGE);
        boss.showTelegraph(new Color(0.48f, 1f, 0.18f, 1f), VINE_WARNING_TIME);
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        elapsed += delta;
        vineTimer -= delta;
        sporeTimer -= delta;

        updatePendingVines(boss, delta, projectileSystem);

        if (vineTimer <= 0f && vinesQueued < TOTAL_VINES && pendingCount < MAX_PENDING_VINES) {
            queueVineWarning(boss, projectileSystem, player);
            vinesQueued++;
            vineTimer = VINE_INTERVAL;
        }

        if (sporeTimer <= 0f && sporesSpawned < MAX_SPORES) {
            spawnSpore(projectileSystem);
            sporesSpawned++;
            sporeTimer = SPORE_INTERVAL;
        }

        if (vinesQueued >= TOTAL_VINES && pendingCount == 0 && elapsed >= MIN_DURATION) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private void updatePendingVines(Boss boss, float delta, ProjectileSystem projectileSystem) {
        for (int i = pendingCount - 1; i >= 0; i--) {
            pendingTimers[i] -= delta;
            if (pendingTimers[i] <= 0f) {
                float x = pendingXs[i];
                pendingCount--;
                pendingTimers[i] = pendingTimers[pendingCount];
                pendingXs[i] = pendingXs[pendingCount];
                spawnVine(boss, projectileSystem, x);
            }
        }
    }

    private void queueVineWarning(Boss boss, ProjectileSystem projectileSystem, Player player) {
        int lane = chooseLane(player);
        float x = laneCenterX(lane);

        pendingXs[pendingCount] = x;
        pendingTimers[pendingCount] = VINE_WARNING_TIME;
        pendingCount++;
        boss.showTelegraph(new Color(0.48f, 1f, 0.18f, 1f), VINE_WARNING_TIME);

        projectileSystem.addProjectile(Projectile.bossWarning(
                x - WARNING_WIDTH * 0.5f,
                Constants.FLOOR_Y,
                WARNING_WIDTH,
                VINE_HEIGHT,
                VINE_WARNING_TIME
        ));
    }

    private int chooseLane(Player player) {
        int playerLane = laneFor(player.getCenterX());
        int lane = vinesQueued % 3 == 0
                ? playerLane
                : MathUtils.random(LANE_COUNT - 1);

        if (lane == lastLane) {
            lane = lane == LANE_COUNT - 1 ? lane - 1 : lane + 1;
        }

        lastLane = lane;
        return lane;
    }

    private int laneFor(float x) {
        float laneWidth = arenaWidth() / LANE_COUNT;
        return MathUtils.clamp((int) ((x - Constants.ARENA_LEFT) / laneWidth), 0, LANE_COUNT - 1);
    }

    private float laneCenterX(int lane) {
        float laneWidth = arenaWidth() / LANE_COUNT;
        float jitter = MathUtils.random(-laneWidth * 0.18f, laneWidth * 0.18f);
        return MathUtils.clamp(
                Constants.ARENA_LEFT + laneWidth * (lane + 0.5f) + jitter,
                Constants.ARENA_LEFT + VINE_WIDTH * 0.5f,
                Constants.ARENA_RIGHT - VINE_WIDTH * 0.5f
        );
    }

    private float arenaWidth() {
        return Constants.ARENA_RIGHT - Constants.ARENA_LEFT;
    }

    private void spawnVine(Boss boss, ProjectileSystem projectileSystem, float centerX) {
        boss.emitSound(BossSoundEvent.VINE_STRIKE);
        projectileSystem.addProjectile(Projectile.bossThorn(
                centerX - VINE_WIDTH * 0.5f,
                Constants.FLOOR_Y,
                VINE_WIDTH,
                VINE_HEIGHT,
                VINE_ACTIVE_TIME
        ));
    }

    private void spawnSpore(ProjectileSystem projectileSystem) {
        float width = Constants.BOSS_PROJECTILE_WIDTH + 12f;
        float height = Constants.BOSS_PROJECTILE_HEIGHT + 12f;
        float centerX = chooseSporeX();

        projectileSystem.addProjectile(Projectile.bossPollen(
                centerX - width * 0.5f,
                Constants.WORLD_HEIGHT + 28f,
                width,
                height,
                MathUtils.random(-72f, 72f),
                -380f,
                0f
        ));
    }

    private float chooseSporeX() {
        float x = MathUtils.random(Constants.ARENA_LEFT + 32f, Constants.ARENA_RIGHT - 32f);

        for (int attempt = 0; attempt < 4 && isNearPendingVine(x); attempt++) {
            x = MathUtils.random(Constants.ARENA_LEFT + 32f, Constants.ARENA_RIGHT - 32f);
        }

        return x;
    }

    private boolean isNearPendingVine(float x) {
        for (int i = 0; i < pendingCount; i++) {
            if (Math.abs(x - pendingXs[i]) < MIN_SPORE_DISTANCE_FROM_VINE) {
                return true;
            }
        }

        return false;
    }
}
