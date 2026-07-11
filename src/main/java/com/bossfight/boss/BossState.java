package com.bossfight.boss;

import com.bossfight.entities.Player;

public interface BossState {
    BossVisualState getVisualState();

    void enter(Boss boss);

    void update(Boss boss, float delta, ProjectileSpawner projectileSpawner, Player player);

    void exit(Boss boss);
}
