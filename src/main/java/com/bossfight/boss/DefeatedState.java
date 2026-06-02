package com.bossfight.boss;

import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.systems.ProjectileSystem;

public class DefeatedState implements BossState {
    @Override
    public String getName() {
        return "Derrotado";
    }

    @Override
    public void enter(Boss boss) {
    }

    @Override
    public void update(Boss boss, float delta, ProjectileSystem projectileSystem, Player player) {
    }

    @Override
    public void exit(Boss boss) {
    }
}
