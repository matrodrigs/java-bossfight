package com.bossfight.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.config.Constants;

public class AttackThreeState implements BossState {
    private static final float COLUMN_WARNING_TIME = 0.36f;
    private static final int MAX_PENDING_COLUMNS = 5;

    private final float[] pendingTimers = new float[MAX_PENDING_COLUMNS];
    private final float[] pendingXs = new float[MAX_PENDING_COLUMNS];
    private float elapsed;
    private float spawnTimer;
    private int pendingCount;

    @Override
    public BossVisualState getVisualState() {
        return BossVisualState.POLLEN_RAIN;
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        spawnTimer = 0.12f;
        pendingCount = 0;
        boss.emitSound(BossSoundEvent.POLLEN_CHARGE);
        boss.showTelegraph(new Color(0.76f, 0.28f, 1f, 1f), 0.42f);
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSpawner projectileSpawner, Player player) {
        elapsed += delta;
        spawnTimer -= delta;

        updatePendingColumns(boss, delta, projectileSpawner);

        if (spawnTimer <= 0f && pendingCount == 0) {
            if (boss.isPhaseTwo() && elapsed > 0.7f && MathUtils.randomBoolean(0.34f)) {
                spawnGardenPattern(projectileSpawner);
                spawnTimer = 0.86f;
            } else {
                spawnWarningColumn(projectileSpawner);
                spawnTimer = boss.isPhaseTwo() ? 0.42f : 0.56f;
            }

            if (boss.isPhaseTwo() && MathUtils.randomBoolean(0.45f)) {
                spawnDriftingPollen(projectileSpawner,
                        MathUtils.random(Constants.ARENA_LEFT + 26f, Constants.ARENA_RIGHT - 46f));
            }
        }

        float duration = boss.isPhaseTwo() ? 3.2f : 2.65f;
        if (elapsed >= duration) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private void updatePendingColumns(Boss boss, float delta, ProjectileSpawner projectileSpawner) {
        for (int i = pendingCount - 1; i >= 0; i--) {
            pendingTimers[i] -= delta;
            if (pendingTimers[i] <= 0f) {
                float x = pendingXs[i];
                pendingCount--;
                pendingTimers[i] = pendingTimers[pendingCount];
                pendingXs[i] = pendingXs[pendingCount];
                spawnFallingProjectile(boss, projectileSpawner, x);
            }
        }
    }

    private void spawnWarningColumn(ProjectileSpawner projectileSpawner) {
        float x = MathUtils.random(Constants.ARENA_LEFT + 22f, Constants.ARENA_RIGHT - 44f);
        addPendingColumn(projectileSpawner, x);
    }

    private void spawnGardenPattern(ProjectileSpawner projectileSpawner) {
        int safeLane = MathUtils.random(3);
        float laneWidth = (Constants.ARENA_RIGHT - Constants.ARENA_LEFT) / 4f;

        for (int lane = 0; lane < 4; lane++) {
            if (lane == safeLane) {
                continue;
            }

            float x = Constants.ARENA_LEFT + laneWidth * (lane + 0.5f) + MathUtils.random(-24f, 24f);
            addPendingColumn(projectileSpawner, x);
        }
    }

    private void addPendingColumn(ProjectileSpawner projectileSpawner, float x) {
        if (pendingCount >= MAX_PENDING_COLUMNS) {
            return;
        }

        pendingXs[pendingCount] = x;
        pendingTimers[pendingCount] = COLUMN_WARNING_TIME;
        pendingCount++;
        projectileSpawner.addProjectile(Projectile.bossWarning(
                x - 12f,
                Constants.FLOOR_Y,
                34f,
                Constants.WORLD_HEIGHT - Constants.FLOOR_Y,
                COLUMN_WARNING_TIME
        ));
    }

    private void spawnFallingProjectile(Boss boss, ProjectileSpawner projectileSpawner, float x) {
        boss.emitSound(BossSoundEvent.POLLEN_DROP);
        float y = Constants.WORLD_HEIGHT + 30f;
        float horizontalDrift = boss.isPhaseTwo() ? MathUtils.random(-65f, 65f) : MathUtils.random(-28f, 28f);
        float fallSpeed = boss.isPhaseTwo() ? -560f : -430f;

        projectileSpawner.addProjectile(Projectile.bossPetalBomb(
                x,
                y,
                Constants.BOSS_PROJECTILE_WIDTH + 4f,
                Constants.BOSS_PROJECTILE_HEIGHT + 4f,
                horizontalDrift,
                fallSpeed,
                0f
        ));
    }

    private void spawnDriftingPollen(ProjectileSpawner projectileSpawner, float x) {
        projectileSpawner.addProjectile(Projectile.bossPollen(
                x,
                Constants.WORLD_HEIGHT + 26f,
                Constants.BOSS_PROJECTILE_WIDTH + 12f,
                Constants.BOSS_PROJECTILE_HEIGHT + 12f,
                MathUtils.random(-90f, 90f),
                -330f,
                0f
        ));
    }
}
