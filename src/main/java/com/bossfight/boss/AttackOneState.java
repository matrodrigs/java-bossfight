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

    private float currentWarningTime;
    private float warningTimer;
    private float recoveryTimer;
    private boolean upperLane;
    private boolean warningSpawned;
    private boolean fired;
    private int strikesFired;
    private int strikesTotal;

    @Override
    public BossVisualState getVisualState() {
        return BossVisualState.VINE_STRIKE;
    }

    @Override
    public void enter(Boss boss) {
        strikesFired = 0;
        strikesTotal = boss.isPhaseTwo() ? 2 : 1;
        startStrike(boss, MathUtils.randomBoolean());
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        if (!warningSpawned) {
            warningSpawned = true;
            projectileSystem.addProjectile(Projectile.bossWarning(
                    Constants.ARENA_LEFT,
                    getLaneY(),
                    getThornHitboxWidth(),
                    getLaneHeight(),
                    currentWarningTime
            ));
        }

        if (!fired) {
            warningTimer -= delta;
        }

        if (!fired && warningTimer <= 0f) {
            fired = true;
            strikesFired++;
            recoveryTimer = boss.isPhaseTwo() ? 0.28f : 0.42f;
            boss.emitSound(BossSoundEvent.VINE_STRIKE);
            projectileSystem.addProjectile(Projectile.bossThorn(
                    Constants.ARENA_LEFT,
                    getLaneY(),
                    getThornHitboxWidth(),
                    getLaneHeight(),
                    boss.isPhaseTwo() ? 0.42f : 0.34f
            ));
        }

        if (fired) {
            recoveryTimer -= delta;
            if (recoveryTimer <= 0f) {
                if (strikesFired < strikesTotal) {
                    startStrike(boss, !upperLane);
                } else {
                    boss.finishCurrentAttack();
                }
            }
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private void startStrike(Boss boss, boolean useUpperLane) {
        upperLane = useUpperLane;
        warningSpawned = false;
        fired = false;
        currentWarningTime = boss.isPhaseTwo() ? 0.52f : WARNING_TIME;
        warningTimer = currentWarningTime;
        recoveryTimer = 0f;
        boss.emitSound(BossSoundEvent.VINE_CHARGE);
        boss.showTelegraph(new Color(1f, 0.16f, 0.08f, 1f), warningTimer);
    }

    private float getLaneY() {
        return upperLane ? Constants.FLOOR_Y + 142f : Constants.FLOOR_Y + 8f;
    }

    private float getLaneHeight() {
        return upperLane ? 54f : 50f;
    }

    private float getThornHitboxWidth() {
        return Constants.BOSS_START_X - Constants.ARENA_LEFT + 62f;
    }
}
