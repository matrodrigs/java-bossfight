package com.bossfight.boss;

import com.badlogic.gdx.graphics.Color;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.systems.ProjectileSystem;

public class PhaseTwoTransitionState implements BossState {
    private static final float DURATION = 1.15f;

    private float elapsed;
    private boolean secondPulsePlayed;

    @Override
    public BossVisualState getVisualState() {
        return BossVisualState.ENRAGING;
    }

    @Override
    public void enter(Boss boss) {
        elapsed = 0f;
        secondPulsePlayed = false;
        boss.emitSound(BossSoundEvent.POLLEN_CHARGE);
        boss.showTelegraph(new Color(1f, 0.22f, 0.08f, 1f), DURATION);
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
        elapsed += delta;

        if (!secondPulsePlayed && elapsed >= 0.55f) {
            secondPulsePlayed = true;
            boss.emitSound(BossSoundEvent.VINE_CHARGE);
            boss.showTelegraph(new Color(1f, 0.74f, 0.18f, 1f), 0.46f);
        }

        if (elapsed >= DURATION) {
            boss.finishCurrentAttack();
        }
    }

    @Override
    public void exit(Boss boss) {
    }
}
