package com.bossfight.systems;

import com.badlogic.gdx.math.MathUtils;

public class AnimationSystem {
    public float pulse(float elapsed, float min, float max, float speed) {
        float alpha = (MathUtils.sin(elapsed * speed) + 1f) * 0.5f;
        return MathUtils.lerp(min, max, alpha);
    }
}
