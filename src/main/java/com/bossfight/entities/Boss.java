package com.bossfight.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.boss.AttackOneState;
import com.bossfight.boss.AttackThreeState;
import com.bossfight.boss.AttackTwoState;
import com.bossfight.boss.BossState;
import com.bossfight.boss.DefeatedState;
import com.bossfight.boss.IdleState;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.util.Constants;

public class Boss {
    private final Hitbox hitbox;
    private final int maxHealth;
    private BossState currentState;
    private int health;
    private int nextAttackIndex;
    private float x;
    private float y;

    public Boss() {
        x = Constants.BOSS_START_X;
        y = Constants.BOSS_START_Y;
        maxHealth = Constants.BOSS_MAX_HEALTH;
        health = maxHealth;
        hitbox = new Hitbox(x, y, Constants.BOSS_WIDTH, Constants.BOSS_HEIGHT);
        setState(new IdleState(1.2f));
    }

    public void update(float delta, ProjectileSystem projectileSystem, Player player) {
        if (health <= 0 && !(currentState instanceof DefeatedState)) {
            setState(new DefeatedState());
        }

        currentState.update(this, delta, projectileSystem, player);
        hitbox.setPosition(x, y);
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (isDefeated()) {
            shapeRenderer.setColor(Color.DARK_GRAY);
        } else if (isPhaseTwo()) {
            shapeRenderer.setColor(Color.FIREBRICK);
        } else {
            shapeRenderer.setColor(Color.ORANGE);
        }

        shapeRenderer.rect(x, y, Constants.BOSS_WIDTH, Constants.BOSS_HEIGHT);

        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.circle(getCenterX(), y + Constants.BOSS_HEIGHT - 42f, 18f);
    }

    public void takeDamage(int amount) {
        if (isDefeated()) {
            return;
        }

        health = MathUtils.clamp(health - amount, 0, maxHealth);
        if (health == 0) {
            setState(new DefeatedState());
        }
    }

    public BossState createNextAttackState() {
        BossState nextState;
        if (nextAttackIndex == 0) {
            nextState = new AttackOneState();
        } else if (nextAttackIndex == 1) {
            nextState = new AttackTwoState();
        } else {
            nextState = new AttackThreeState();
        }

        nextAttackIndex = (nextAttackIndex + 1) % 3;
        return nextState;
    }

    public void finishCurrentAttack() {
        setState(new IdleState(isPhaseTwo() ? 0.55f : 0.9f));
    }

    public void setState(BossState nextState) {
        if (currentState != null) {
            currentState.exit(this);
        }

        currentState = nextState;
        currentState.enter(this);
    }

    public boolean isPhaseTwo() {
        return health <= maxHealth * 0.5f && health > 0;
    }

    public boolean isDefeated() {
        return health <= 0;
    }

    public float getCenterX() {
        return x + Constants.BOSS_WIDTH * 0.5f;
    }

    public float getCenterY() {
        return y + Constants.BOSS_HEIGHT * 0.5f;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public String getStateName() {
        return currentState.getName();
    }
}
