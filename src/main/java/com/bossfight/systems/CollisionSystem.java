package com.bossfight.systems;

import com.badlogic.gdx.utils.Array;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;

public class CollisionSystem {
    private float bossContactCooldown;

    public void resolve(Player player, Boss boss, ProjectileSystem projectileSystem, float delta) {
        bossContactCooldown = Math.max(0f, bossContactCooldown - delta);
        resolvePlayerProjectiles(boss, projectileSystem.getPlayerProjectiles());
        resolveBossProjectiles(player, projectileSystem.getBossProjectiles());
        resolveBossContact(player, boss);
    }

    private void resolvePlayerProjectiles(Boss boss, Array<Projectile> projectiles) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            if (!boss.isDefeated() && projectile.getHitbox().overlaps(boss.getHitbox())) {
                boss.takeDamage(projectile.getDamage());
                projectile.deactivate();
                projectiles.removeIndex(i);
            }
        }
    }

    private void resolveBossProjectiles(Player player, Array<Projectile> projectiles) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            if (projectile.getHitbox().overlaps(player.getHitbox())) {
                player.takeDamage(projectile.getDamage());
                projectile.deactivate();
                projectiles.removeIndex(i);
            }
        }
    }

    private void resolveBossContact(Player player, Boss boss) {
        if (boss.isDefeated() || bossContactCooldown > 0f) {
            return;
        }

        if (player.getHitbox().overlaps(boss.getHitbox())) {
            player.takeDamage(1);
            bossContactCooldown = 0.8f;
        }
    }
}
