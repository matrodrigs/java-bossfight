package com.bossfight.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.Constants;

public class AttackTwoState implements BossState {
    private float elapsed;
    private float burstTimer;
    private int volleys;

    @Override
    public String getName() {
        return "Mãos mágicas";
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        burstTimer = 0.58f;
        volleys = 0;
        boss.emitSound(BossSoundEvent.MAGIC_CHARGE);
        boss.showTelegraph(new Color(0.96f, 0.74f, 0.18f, 1f), 0.58f);
        boss.playAttackMotion(0.58f, 0.45f);
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        elapsed += delta;
        burstTimer -= delta;

        if (burstTimer <= 0f) {
            boss.emitSound(BossSoundEvent.MAGIC_VOLLEY);
            boss.playAttackMotion(0.28f, 0.82f);
            fireVolley(boss, projectileSystem, player);
            volleys++;
            burstTimer = boss.isPhaseTwo() ? 0.38f : 0.52f;
            boss.showTelegraph(new Color(0.96f, 0.74f, 0.18f, 1f), Math.min(0.32f, burstTimer));
        }

        int maxVolleys = boss.isPhaseTwo() ? 5 : 4;
        if (volleys >= maxVolleys && elapsed >= (boss.isPhaseTwo() ? 2.25f : 2.55f)) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private void fireVolley(Boss boss, ProjectileSystem projectileSystem, Player player) {
        int projectileCount = boss.isPhaseTwo() ? 3 : 2;
        float speed = boss.isPhaseTwo() ? 490f : 430f;
        float originX = boss.getCenterX() - 126f;
        float originY = Constants.FLOOR_Y + (boss.isPhaseTwo() ? 392f : 372f);
        Vector2 baseDirection = new Vector2(
                player.getCenterX() - originX,
                player.getCenterY() - originY
        ).nor();

        for (int i = 0; i < projectileCount; i++) {
            float spread = (i - (projectileCount - 1) * 0.5f) * (boss.isPhaseTwo() ? 0.22f : 0.16f);
            float angle = (float) Math.atan2(baseDirection.y, baseDirection.x) + spread + MathUtils.random(-0.04f, 0.04f);
            Projectile.Kind kind = i % 2 == 0 ? Projectile.Kind.BOSS_ACORN : Projectile.Kind.BOSS_SEED;
            float sizeBonus = kind == Projectile.Kind.BOSS_ACORN ? 6f : 2f;
            float width = Constants.BOSS_PROJECTILE_WIDTH + sizeBonus;
            float height = Constants.BOSS_PROJECTILE_HEIGHT + sizeBonus * 0.45f;
            projectileSystem.addProjectile(new Projectile(
                    Projectile.Owner.BOSS,
                    originX - width * 0.5f,
                    originY - height * 0.5f,
                    width,
                    height,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    Constants.BOSS_PROJECTILE_DAMAGE,
                    kind,
                    -1f,
                    -70f
            ));
        }

        if (boss.isPhaseTwo() && volleys % 2 == 1) {
            fireArcingAcorn(boss, projectileSystem, player);
        }
    }

    private void fireArcingAcorn(Boss boss, ProjectileSystem projectileSystem, Player player) {
        float width = Constants.BOSS_PROJECTILE_WIDTH + 12f;
        float height = Constants.BOSS_PROJECTILE_HEIGHT + 15f;
        float originX = boss.getCenterX() - 112f;
        float originY = Constants.FLOOR_Y + 346f;
        float travelTime = 1.06f;
        float velocityX = (player.getCenterX() - originX) / travelTime;
        float velocityY = 360f;

        projectileSystem.addProjectile(new Projectile(
                Projectile.Owner.BOSS,
                originX - width * 0.5f,
                originY - height * 0.5f,
                width,
                height,
                velocityX,
                velocityY,
                Constants.BOSS_PROJECTILE_DAMAGE,
                Projectile.Kind.BOSS_ACORN,
                -1f,
                Constants.GRAVITY * 0.42f
        ));
    }
}
