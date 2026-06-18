package com.bossfight.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.Constants;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.ProjectileSystem;

public class AttackFourState implements BossState {
    private float elapsed;
    private float puffTimer;
    private int puffsSpawned;

    @Override
    public BossVisualState getVisualState() {
        return BossVisualState.POLLEN_BREATH;
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        puffTimer = 0.44f;
        puffsSpawned = 0;
        boss.emitSound(BossSoundEvent.POLLEN_CHARGE);
        boss.showTelegraph(new Color(0.74f, 0.32f, 1f, 1f), 0.58f);
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        elapsed += delta;
        puffTimer -= delta;

        if (puffTimer <= 0f && puffsSpawned < getMaxPuffs(boss)) {
            spawnPollenPuff(boss, projectileSystem, player);
            puffsSpawned++;
            puffTimer = boss.isPhaseTwo() ? 0.18f : 0.24f;
            boss.emitSound(BossSoundEvent.POLLEN_DROP);
        }

        if (elapsed >= (boss.isPhaseTwo() ? 2.05f : 1.72f)) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private int getMaxPuffs(Boss boss) {
        return boss.isPhaseTwo() ? 6 : 4;
    }

    private void spawnPollenPuff(Boss boss, ProjectileSystem projectileSystem, Player player) {
        float width = Constants.BOSS_PROJECTILE_WIDTH + 22f;
        float height = Constants.BOSS_PROJECTILE_HEIGHT + 22f;
        float originX = boss.getCenterX() - 138f;
        float originY = Constants.FLOOR_Y + 300f + MathUtils.sin(puffsSpawned * 1.35f) * 34f;
        float speed = boss.isPhaseTwo() ? 285f : 245f;
        float laneOffset = ((puffsSpawned % 3) - 1f) * 28f;
        float targetX = player.getCenterX();
        float targetY = player.getCenterY() + laneOffset + MathUtils.random(-12f, 12f);
        float angle = MathUtils.atan2(targetY - originY, targetX - originX) + MathUtils.random(-0.08f, 0.08f);
        float velocityX = MathUtils.clamp(MathUtils.cos(angle) * speed,
                boss.isPhaseTwo() ? -330f : -285f,
                boss.isPhaseTwo() ? -215f : -190f);
        float velocityY = MathUtils.clamp(MathUtils.sin(angle) * speed,
                boss.isPhaseTwo() ? -105f : -82f,
                boss.isPhaseTwo() ? 95f : 76f);

        projectileSystem.addProjectile(Projectile.bossPollen(
                originX - width * 0.5f,
                originY - height * 0.5f,
                width,
                height,
                velocityX,
                velocityY,
                0f
        ));
    }
}
