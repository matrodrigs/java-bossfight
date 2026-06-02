package com.bossfight.boss;

import com.badlogic.gdx.math.MathUtils;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.util.Constants;

public class AttackTwoState implements BossState {
    private float elapsed;
    private float burstTimer;

    @Override
    public String getName() {
        return "Rajada circular";
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        burstTimer = 0.25f;
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        elapsed += delta;
        burstTimer -= delta;

        if (burstTimer <= 0f) {
            fireBurst(boss, projectileSystem);
            burstTimer = boss.isPhaseTwo() ? 0.58f : 0.82f;
        }

        float duration = boss.isPhaseTwo() ? 2.7f : 2.35f;
        if (elapsed >= duration) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private void fireBurst(Boss boss, ProjectileSystem projectileSystem) {
        int projectileCount = boss.isPhaseTwo() ? 12 : 8;
        float speed = boss.isPhaseTwo() ? 310f : 250f;

        for (int i = 0; i < projectileCount; i++) {
            float angle = MathUtils.PI2 * i / projectileCount;
            projectileSystem.addProjectile(new Projectile(
                    Projectile.Owner.BOSS,
                    boss.getCenterX(),
                    boss.getCenterY(),
                    Constants.BOSS_PROJECTILE_WIDTH,
                    Constants.BOSS_PROJECTILE_HEIGHT,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    Constants.BOSS_PROJECTILE_DAMAGE
            ));
        }
    }
}
