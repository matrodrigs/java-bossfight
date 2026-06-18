package com.bossfight.boss;

import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.systems.ProjectileSystem;

public interface BossState {
    BossVisualState getVisualState();

    void enter(Boss boss);

    void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player);

    void exit(Boss boss);
}
