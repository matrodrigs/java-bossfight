package com.bossfight.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.Constants;

public class AttackOneState implements BossState {
    private static final float WARNING_TIME = 0.62f;

    private float elapsed;
    private boolean upperLane;
    private boolean warningSpawned;
    private boolean fired;

    @Override
    public String getName() {
        return "Bote de cipó";
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        upperLane = MathUtils.randomBoolean();
        warningSpawned = false;
        fired = false;
        boss.emitSound(BossSoundEvent.VINE_CHARGE);
        boss.showTelegraph(new Color(1f, 0.16f, 0.08f, 1f), WARNING_TIME);
        boss.playAttackMotion(WARNING_TIME, 0.55f);
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        elapsed += delta;

        if (!warningSpawned) {
            warningSpawned = true;
            projectileSystem.addProjectile(new Projectile(
                    Projectile.Owner.BOSS,
                    Constants.ARENA_LEFT,
                    getLaneY(),
                    Constants.BOSS_START_X - Constants.ARENA_LEFT + 42f,
                    getLaneHeight(),
                    0f,
                    0f,
                    0,
                    Projectile.Kind.BOSS_WARNING,
                    WARNING_TIME
            ));
        }

        if (!fired && elapsed >= WARNING_TIME) {
            fired = true;
            boss.emitSound(BossSoundEvent.VINE_STRIKE);
            boss.playAttackMotion(0.32f, 1.15f);
            projectileSystem.addProjectile(new Projectile(
                    Projectile.Owner.BOSS,
                    Constants.ARENA_LEFT,
                    getLaneY(),
                    Constants.BOSS_START_X - Constants.ARENA_LEFT + 62f,
                    getLaneHeight(),
                    0f,
                    0f,
                    Constants.BOSS_PROJECTILE_DAMAGE,
                    Projectile.Kind.BOSS_THORN,
                    boss.isPhaseTwo() ? 0.42f : 0.34f
            ));
        }

        if (elapsed >= (boss.isPhaseTwo() ? 1.12f : 1.22f)) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private float getLaneY() {
        return upperLane ? Constants.FLOOR_Y + 142f : Constants.FLOOR_Y + 8f;
    }

    private float getLaneHeight() {
        return upperLane ? 54f : 50f;
    }
}
