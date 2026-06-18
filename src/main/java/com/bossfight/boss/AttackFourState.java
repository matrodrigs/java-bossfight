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
        puffTimer = 0.34f;
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
            puffTimer = boss.isPhaseTwo() ? 0.16f : 0.22f;
            boss.emitSound(BossSoundEvent.POLLEN_DROP);
        }

        if (elapsed >= (boss.isPhaseTwo() ? 2.12f : 1.76f)) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }

    private int getMaxPuffs(Boss boss) {
        return boss.isPhaseTwo() ? 7 : 5;
    }

    private void spawnPollenPuff(Boss boss, ProjectileSystem projectileSystem, Player player) {
        float width = Constants.BOSS_PROJECTILE_WIDTH + 26f;
        float height = Constants.BOSS_PROJECTILE_HEIGHT + 26f;
        float originX = boss.getCenterX() - 138f;
        float originY = Constants.FLOOR_Y + 300f + MathUtils.sin(puffsSpawned * 1.35f) * 34f;
        float speed = boss.isPhaseTwo() ? 330f : 275f;
        float laneOffset = ((puffsSpawned % 3) - 1f) * 36f;
        float targetX = player.getCenterX();
        float targetY = player.getCenterY() + laneOffset + MathUtils.random(-10f, 10f);
        float angle = MathUtils.atan2(targetY - originY, targetX - originX) + MathUtils.random(-0.06f, 0.06f);
        float velocityX = MathUtils.clamp(MathUtils.cos(angle) * speed,
                boss.isPhaseTwo() ? -380f : -320f,
                boss.isPhaseTwo() ? -245f : -215f);
        float velocityY = MathUtils.clamp(MathUtils.sin(angle) * speed,
                boss.isPhaseTwo() ? -120f : -92f,
                boss.isPhaseTwo() ? 115f : 88f);

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
