package com.bossfight.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.config.Constants;
import com.bossfight.entities.Hitbox;
import com.bossfight.entities.Player;

import java.util.ArrayDeque;

public class Boss {
    private static final float ACTION_IMPULSE_DURATION = 0.30f;
    private static final float HIT_REACTION_DURATION = 0.18f;
    private static final float SPECIAL_HIT_REACTION_DURATION = 0.36f;
    private static final float PHASE_ONE_RECOVERY_DURATION = 0.90f;
    private static final float PHASE_TWO_RECOVERY_DURATION = 0.42f;
    private static final float FINAL_RAGE_RECOVERY_DURATION = 0.34f;
    private static final float CHAIN_RECOVERY_DURATION = 0.20f;
    private static final float FINAL_RAGE_HEALTH_RATIO = 0.20f;

    private enum Phase {
        ONE,
        TWO,
        FINAL_RAGE
    }

    private final Hitbox hitbox;
    private final ArrayDeque<BossSoundEvent> soundEvents = new ArrayDeque<>();
    private final int maxHealth;
    private final Color telegraphColor = new Color(1f, 0.24f, 0.1f, 1f);
    private BossState currentState;
    private Phase phase = Phase.ONE;
    private int health;
    private int lastAttackIndex = -1;
    private int phaseTwoAttackCount;
    private BossState queuedFollowUpState;
    private float x;
    private float y;
    private float telegraphTimer;
    private float telegraphDuration = 1f;
    private float visualStateTime;
    private float actionImpulseTimer;
    private float hitReactionTimer;
    private float specialHitReactionTimer;

    public Boss() {
        x = Constants.BOSS_START_X;
        y = Constants.BOSS_START_Y;
        maxHealth = Constants.BOSS_MAX_HEALTH;
        health = maxHealth;
        hitbox = new Hitbox(x, y, Constants.BOSS_WIDTH, Constants.BOSS_HEIGHT);
        currentState = new IdleState(1.2f);
    }

    public void update(float delta, ProjectileSpawner projectileSpawner, Player player) {
        telegraphTimer = Math.max(0f, telegraphTimer - delta);
        visualStateTime += delta;
        actionImpulseTimer = Math.max(0f, actionImpulseTimer - delta);
        hitReactionTimer = Math.max(0f, hitReactionTimer - delta);
        specialHitReactionTimer = Math.max(0f, specialHitReactionTimer - delta);

        if (phase == Phase.ONE && reachedPhaseTwoHealth()) {
            phase = Phase.TWO;
            setState(new PhaseTwoTransitionState());
        } else if (phase == Phase.TWO && reachedFinalRageHealth()) {
            phase = Phase.FINAL_RAGE;
            setState(new FinalRageState());
        }

        currentState.update(this, delta, projectileSpawner, player);
        hitbox.setPosition(x, y);
    }

    public boolean takeDamage(int amount) {
        return takeDamage(amount, false);
    }

    public boolean takeDamage(int amount, boolean specialHit) {
        if (isDefeated() || isInvulnerable()) {
            return false;
        }

        health = MathUtils.clamp(health - amount, 0, maxHealth);
        hitReactionTimer = HIT_REACTION_DURATION;
        specialHitReactionTimer = specialHit ? SPECIAL_HIT_REACTION_DURATION : 0f;
        if (health == 0) {
            soundEvents.clear();
            setState(new DefeatedState());
        } else if (specialHit) {
            emitSound(BossSoundEvent.BOSS_STAGGER);
        }
        return true;
    }

    public void emitSound(BossSoundEvent soundEvent) {
        if (soundEvent != null && !isDefeated()) {
            soundEvents.offer(soundEvent);
            if (soundEvent == BossSoundEvent.VINE_STRIKE
                    || soundEvent == BossSoundEvent.MAGIC_VOLLEY
                    || soundEvent == BossSoundEvent.POLLEN_DROP
                    || soundEvent == BossSoundEvent.BOSS_STAGGER
                    || soundEvent == BossSoundEvent.FINAL_RAGE
                    || soundEvent == BossSoundEvent.PHASE_ROAR
                    || soundEvent == BossSoundEvent.PHASE_SHOCKWAVE) {
                actionImpulseTimer = ACTION_IMPULSE_DURATION;
            }
        }
    }

    public BossSoundEvent pollSoundEvent() {
        return soundEvents.poll();
    }

    public void showTelegraph(Color color, float duration) {
        telegraphColor.set(color);
        telegraphDuration = Math.max(0.01f, duration);
        telegraphTimer = telegraphDuration;
    }

    public BossState createNextAttackState() {
        if (queuedFollowUpState != null) {
            BossState followUpState = queuedFollowUpState;
            queuedFollowUpState = null;
            return followUpState;
        }

        int attackCount = isPhaseTwo() ? 5 : 3;
        int nextAttackIndex;

        if (isPhaseTwo() && phaseTwoAttackCount % 5 == 4 && lastAttackIndex != 4) {
            nextAttackIndex = 4;
        } else if (isPhaseTwo() && phaseTwoAttackCount % 4 == 3 && lastAttackIndex != 3) {
            nextAttackIndex = 3;
        } else {
            do {
                nextAttackIndex = MathUtils.random(attackCount - 1);
            } while (nextAttackIndex == lastAttackIndex);
        }

        lastAttackIndex = nextAttackIndex;
        if (isPhaseTwo()) {
            phaseTwoAttackCount++;
        }

        return switch (nextAttackIndex) {
            case 0 -> new AttackOneState();
            case 1 -> new AttackTwoState();
            case 2 -> new AttackThreeState();
            case 3 -> new AttackFourState();
            default -> new AttackFiveState();
        };
    }

    public void finishCurrentAttack() {
        float recoveryDuration;
        if (isFinalRage()) {
            recoveryDuration = FINAL_RAGE_RECOVERY_DURATION;
        } else if (isPhaseTwo()) {
            recoveryDuration = PHASE_TWO_RECOVERY_DURATION;
        } else {
            recoveryDuration = PHASE_ONE_RECOVERY_DURATION;
        }
        setState(new IdleState(recoveryDuration));
    }

    void finishCurrentAttack(BossState followUpState) {
        queuedFollowUpState = followUpState;
        setState(new IdleState(CHAIN_RECOVERY_DURATION));
    }

    public void setState(BossState nextState) {
        if (currentState != null) {
            currentState.exit(this);
        }

        currentState = nextState;
        visualStateTime = 0f;
        actionImpulseTimer = 0f;
        currentState.enter(this);
    }

    public boolean isPhaseTwo() {
        return phase != Phase.ONE && !isDefeated();
    }

    public boolean isFinalRage() {
        return phase == Phase.FINAL_RAGE && !isDefeated();
    }

    public boolean isDefeated() {
        return health <= 0;
    }

    public boolean isInvulnerable() {
        return currentState.isInvulnerable();
    }

    private boolean reachedPhaseTwoHealth() {
        return health <= maxHealth * 0.5f && !isDefeated();
    }

    private boolean reachedFinalRageHealth() {
        return health <= maxHealth * FINAL_RAGE_HEALTH_RATIO && !isDefeated();
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

    public BossVisualState getVisualState() {
        return currentState.getVisualState();
    }

    public boolean isTelegraphing() {
        return telegraphTimer > 0f;
    }

    public float getTelegraphAlpha() {
        if (telegraphTimer <= 0f) {
            return 0f;
        }
        return MathUtils.clamp(telegraphTimer / telegraphDuration, 0f, 1f);
    }

    public Color getTelegraphColor() {
        return telegraphColor;
    }

    public float getVisualStateTime() {
        return visualStateTime;
    }

    public float getActionImpulse() {
        return MathUtils.clamp(actionImpulseTimer / ACTION_IMPULSE_DURATION, 0f, 1f);
    }

    public float getHitReaction() {
        return MathUtils.clamp(hitReactionTimer / HIT_REACTION_DURATION, 0f, 1f);
    }

    public float getSpecialHitReaction() {
        return MathUtils.clamp(specialHitReactionTimer / SPECIAL_HIT_REACTION_DURATION, 0f, 1f);
    }
}
