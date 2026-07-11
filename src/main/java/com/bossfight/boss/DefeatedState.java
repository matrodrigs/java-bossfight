package com.bossfight.boss;

import com.bossfight.entities.Player;

public class DefeatedState implements BossState {
    @Override
    public BossVisualState getVisualState() {
        return BossVisualState.DEFEATED;
    }

    @Override
    public void enter(Boss boss) {
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSpawner projectileSpawner, Player player) {
    }

    @Override
    public void exit(Boss boss) {
    }
}
