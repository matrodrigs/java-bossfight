package com.bossfight.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.Constants;

public class AttackThreeState implements BossState {
    private static final float COLUMN_WARNING_TIME = 0.36f;

    private float elapsed;
    private float spawnTimer;
    private float pendingTimer;
    private float pendingX;
    private boolean hasPendingColumn;

    @Override
    public String getName() {
        return "Chuva de pólen";
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        spawnTimer = 0.12f;
        pendingTimer = 0f;
        hasPendingColumn = false;
        boss.emitSound(BossSoundEvent.POLLEN_CHARGE);
        boss.showTelegraph(new Color(0.76f, 0.28f, 1f, 1f), 0.42f);
        boss.playAttackMotion(0.5f, 0.5f);
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        elapsed += delta;
        spawnTimer -= delta;

        if (hasPendingColumn) {
            pendingTimer -= delta;
            if (pendingTimer <= 0f) {
                hasPendingColumn = false;
                spawnFallingProjectile(boss, projectileSystem, pendingX);
            }
        }

        if (spawnTimer <= 0f) {
            boss.playAttackMotion(0.3f, 0.62f);
            spawnWarningColumn(projectileSystem);
            if (boss.isPhaseTwo() && MathUtils.randomBoolean(0.45f)) {
                spawnDriftingPollen(projectileSystem,
                        MathUtils.random(Constants.ARENA_LEFT + 26f, Constants.ARENA_RIGHT - 46f));
            }
            spawnTimer = boss.isPhaseTwo() ? 0.42f : 0.56f;
        }

        float duration = boss.isPhaseTwo() ? 3.2f : 2.65f;
        if (elapsed >= duration) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private void spawnWarningColumn(ProjectileSystem projectileSystem) {
        if (hasPendingColumn) {
            return;
        }

        pendingX = MathUtils.random(Constants.ARENA_LEFT + 22f, Constants.ARENA_RIGHT - 44f);
        pendingTimer = COLUMN_WARNING_TIME;
        hasPendingColumn = true;
        projectileSystem.addProjectile(new Projectile(
                Projectile.Owner.BOSS,
                pendingX - 12f,
                Constants.FLOOR_Y,
                34f,
                Constants.WORLD_HEIGHT - Constants.FLOOR_Y,
                0f,
                0f,
                0,
                Projectile.Kind.BOSS_WARNING,
                COLUMN_WARNING_TIME
        ));
    }

    private void spawnFallingProjectile(Boss boss, ProjectileSystem projectileSystem, float x) {
        boss.emitSound(BossSoundEvent.POLLEN_DROP);
        float y = Constants.WORLD_HEIGHT + 30f;
        float horizontalDrift = boss.isPhaseTwo() ? MathUtils.random(-65f, 65f) : MathUtils.random(-28f, 28f);
        float fallSpeed = boss.isPhaseTwo() ? -560f : -430f;

        projectileSystem.addProjectile(new Projectile(
                Projectile.Owner.BOSS,
                x,
                y,
                Constants.BOSS_PROJECTILE_WIDTH + 4f,
                Constants.BOSS_PROJECTILE_HEIGHT + 4f,
                horizontalDrift,
                fallSpeed,
                Constants.BOSS_PROJECTILE_DAMAGE,
                Projectile.Kind.BOSS_PETAL_BOMB
        ));
    }

    private void spawnDriftingPollen(ProjectileSystem projectileSystem, float x) {
        projectileSystem.addProjectile(new Projectile(
                Projectile.Owner.BOSS,
                x,
                Constants.WORLD_HEIGHT + 26f,
                Constants.BOSS_PROJECTILE_WIDTH + 12f,
                Constants.BOSS_PROJECTILE_HEIGHT + 12f,
                MathUtils.random(-90f, 90f),
                -330f,
                Constants.BOSS_PROJECTILE_DAMAGE,
                Projectile.Kind.BOSS_POLLEN
        ));
    }
}
