package com.bossfight.boss;

import com.badlogic.gdx.math.Vector2;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.util.Constants;

public class AttackOneState implements BossState {
    private float elapsed;
    private float shotTimer;

    @Override
    public String getName() {
        return "Ataque direcionado";
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        shotTimer = 0.15f;
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        elapsed += delta;
        shotTimer -= delta;

        if (shotTimer <= 0f) {
            fireAimedShot(boss, projectileSystem, player);
            shotTimer = boss.isPhaseTwo() ? 0.24f : 0.38f;
        }

        float duration = boss.isPhaseTwo() ? 2.8f : 2.2f;
        if (elapsed >= duration) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private void fireAimedShot(Boss boss, ProjectileSystem projectileSystem, Player player) {
        Vector2 direction = new Vector2(
                player.getCenterX() - boss.getCenterX(),
                player.getCenterY() - boss.getCenterY()
        ).nor();

        float speed = boss.isPhaseTwo() ? 440f : 350f;
        projectileSystem.addProjectile(new Projectile(
                Projectile.Owner.BOSS,
                boss.getCenterX(),
                boss.getCenterY(),
                Constants.BOSS_PROJECTILE_WIDTH,
                Constants.BOSS_PROJECTILE_HEIGHT,
                direction.x * speed,
                direction.y * speed,
                Constants.BOSS_PROJECTILE_DAMAGE
        ));
    }
}
