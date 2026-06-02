package com.bossfight.systems;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.bossfight.entities.Projectile;

public class ProjectileSystem {
    private final Array<Projectile> playerProjectiles = new Array<>();
    private final Array<Projectile> bossProjectiles = new Array<>();

    public void addProjectile(Projectile projectile) {
        if (projectile.getOwner() == Projectile.Owner.PLAYER) {
            playerProjectiles.add(projectile);
        } else {
            bossProjectiles.add(projectile);
        }
    }

    public void update(float delta) {
        updateProjectiles(playerProjectiles, delta);
        updateProjectiles(bossProjectiles, delta);
    }

    public void render(ShapeRenderer shapeRenderer) {
        for (Projectile projectile : playerProjectiles) {
            projectile.render(shapeRenderer);
        }
        for (Projectile projectile : bossProjectiles) {
            projectile.render(shapeRenderer);
        }
    }

    public void clear() {
        playerProjectiles.clear();
        bossProjectiles.clear();
    }

    public Array<Projectile> getPlayerProjectiles() {
        return playerProjectiles;
    }

    public Array<Projectile> getBossProjectiles() {
        return bossProjectiles;
    }

    private void updateProjectiles(Array<Projectile> projectiles, float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);

            if (!projectile.isActive() || projectile.isOutsideWorld()) {
                projectiles.removeIndex(i);
            }
        }
    }
}
