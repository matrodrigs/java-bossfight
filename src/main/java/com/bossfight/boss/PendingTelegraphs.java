package com.bossfight.boss;

final class PendingTelegraphs {
    private final float[] timers;
    private final float[] positions;
    private int size;

    PendingTelegraphs(int capacity) {
        timers = new float[capacity];
        positions = new float[capacity];
    }

    void clear() {
        size = 0;
    }

    void update(float delta) {
        for (int index = 0; index < size; index++) {
            timers[index] -= delta;
        }
    }

    void add(float position, float duration) {
        if (isFull()) {
            throw new IllegalStateException("Não há espaço para outro aviso pendente");
        }

        positions[size] = position;
        timers[size] = duration;
        size++;
    }

    boolean isEmpty() {
        return size == 0;
    }

    boolean isFull() {
        return size == timers.length;
    }

    float pollExpiredPosition() {
        for (int index = size - 1; index >= 0; index--) {
            if (timers[index] <= 0f) {
                return remove(index);
            }
        }
        return Float.NaN;
    }

    boolean hasPositionWithin(float position, float distance) {
        for (int index = 0; index < size; index++) {
            if (Math.abs(position - positions[index]) < distance) {
                return true;
            }
        }
        return false;
    }

    private float remove(int index) {
        float position = positions[index];
        size--;
        timers[index] = timers[size];
        positions[index] = positions[size];
        return position;
    }
}
