package com.bossfight.boss;

import com.badlogic.gdx.math.MathUtils;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.util.Constants;

public class AttackThreeState implements BossState {
    private float elapsed;
    private float spawnTimer;

    @Override
    public String getName() {
        return "Chuva de projeteis";
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        spawnTimer = 0.1f;
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        elapsed += delta;
        spawnTimer -= delta;

        if (spawnTimer <= 0f) {
            spawnFallingProjectile(boss, projectileSystem);
            spawnTimer = boss.isPhaseTwo() ? 0.18f : 0.28f;
        }

        float duration = boss.isPhaseTwo() ? 3.1f : 2.6f;
        if (elapsed >= duration) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private void spawnFallingProjectile(Boss boss, ProjectileSystem projectileSystem) {
        float x = MathUtils.random(Constants.ARENA_LEFT, Constants.ARENA_RIGHT - Constants.BOSS_PROJECTILE_WIDTH);
        float y = Constants.WORLD_HEIGHT + 30f;
        float horizontalDrift = boss.isPhaseTwo() ? MathUtils.random(-80f, 80f) : 0f;
        float fallSpeed = boss.isPhaseTwo() ? -520f : -410f;

        projectileSystem.addProjectile(new Projectile(
                Projectile.Owner.BOSS,
                x,
                y,
                Constants.BOSS_PROJECTILE_WIDTH,
                Constants.BOSS_PROJECTILE_HEIGHT,
                horizontalDrift,
                fallSpeed,
                Constants.BOSS_PROJECTILE_DAMAGE
        ));
    }
}
