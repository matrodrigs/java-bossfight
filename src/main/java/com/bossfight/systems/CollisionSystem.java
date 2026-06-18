package com.bossfight.systems;

import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;

public class CollisionSystem {
    private float bossContactCooldown;
    private float requestedHitstop;
    private float requestedShake;

    public void resolve(Player player, Boss boss, ProjectileSystem projectileSystem, ParticleSystem particleSystem,
                        AudioManager audioManager, float delta) {
        requestedHitstop = 0f;
        requestedShake = 0f;
        bossContactCooldown = Math.max(0f, bossContactCooldown - delta);
        resolvePlayerProjectiles(player, boss, projectileSystem, particleSystem, audioManager);
        resolveBossProjectiles(player, projectileSystem, particleSystem, audioManager);
        resolveBossContact(player, boss, particleSystem, audioManager);
    }

    public float consumeRequestedHitstop() {
        float value = requestedHitstop;
        requestedHitstop = 0f;
        return value;
    }

    public float consumeRequestedShake() {
        float value = requestedShake;
        requestedShake = 0f;
        return value;
    }

    private void resolvePlayerProjectiles(Player player, Boss boss, ProjectileSystem projectileSystem,
                                           ParticleSystem particleSystem, AudioManager audioManager) {
        projectileSystem.removePlayerProjectilesIf(projectile -> {
            if (!boss.isDefeated() && projectile.getHitbox().overlaps(boss.getHitbox())) {
                boolean hit = boss.takeDamage(projectile.getDamage());
                if (hit) {
                    boolean special = projectile.isSpecial();
                    particleSystem.spawnBossHit(projectile.getCenterX(), projectile.getCenterY(), special);
                    player.addSpecialEnergy(special ? 0f : com.bossfight.Constants.PLAYER_SPECIAL_HIT_CHARGE);
                    if (!boss.isDefeated()) {
                        audioManager.playCue(special ? AudioManager.Cue.PLAYER_SPECIAL : AudioManager.Cue.BOSS_HIT);
                    }
                    requestedHitstop = Math.max(requestedHitstop, special ? 0.08f : 0.035f);
                    requestedShake = Math.max(requestedShake, special ? 10f : 4f);
                }
                return true;
            }
            return false;
        });
    }

    private void resolveBossProjectiles(Player player, ProjectileSystem projectileSystem, ParticleSystem particleSystem,
                                        AudioManager audioManager) {
        projectileSystem.removeBossProjectilesIf(projectile -> {
            if (player.isInvulnerableAfterHit()) {
                return false;
            }

            if (projectile.getDamage() > 0 && projectile.getHitbox().overlaps(player.getHitbox())) {
                boolean damaged = player.takeDamage(projectile.getDamage(), impactSourceX(player, projectile));
                if (damaged) {
                    particleSystem.spawnPlayerDamage(player.getCenterX(), player.getCenterY());
                    audioManager.playCue(AudioManager.Cue.PLAYER_HIT);
                    requestedHitstop = Math.max(requestedHitstop, 0.07f);
                    requestedShake = Math.max(requestedShake, 8f);
                }
                return projectile.shouldRemoveOnHit();
            }
            return false;
        });
    }

    private float impactSourceX(Player player, Projectile projectile) {
        float velocityX = projectile.getVelocityX();
        if (Math.abs(velocityX) > 1f) {
            return player.getCenterX() - Math.signum(velocityX);
        }

        if (projectile.getKind() == Projectile.Kind.BOSS_THORN) {
            return projectile.getX();
        }

        return projectile.getCenterX();
    }

    private void resolveBossContact(Player player, Boss boss, ParticleSystem particleSystem, AudioManager audioManager) {
        if (boss.isDefeated() || bossContactCooldown > 0f) {
            return;
        }

        if (player.getHitbox().overlaps(boss.getHitbox())) {
            boolean damaged = player.takeDamage(1, boss.getCenterX());
            if (damaged) {
                particleSystem.spawnPlayerDamage(player.getCenterX(), player.getCenterY());
                audioManager.playCue(AudioManager.Cue.PLAYER_HIT);
                requestedHitstop = Math.max(requestedHitstop, 0.07f);
                requestedShake = Math.max(requestedShake, 8f);
                bossContactCooldown = 0.8f;
            }
        }
    }
}
