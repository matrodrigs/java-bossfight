package com.bossfight.util;

public final class Constants {
    public static final String GAME_TITLE = "Java Bossfight";

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final int TARGET_FPS = 60;

    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;
    public static final float ARENA_LEFT = 60f;
    public static final float ARENA_RIGHT = WORLD_WIDTH - 60f;
    public static final float FLOOR_Y = 92f;
    public static final float GRAVITY = -1900f;

    public static final float PLAYER_WIDTH = 46f;
    public static final float PLAYER_HEIGHT = 82f;
    public static final float PLAYER_START_X = 170f;
    public static final float PLAYER_START_Y = FLOOR_Y;
    public static final float PLAYER_SPEED = 340f;
    public static final float PLAYER_JUMP_SPEED = 780f;
    public static final float PLAYER_DASH_SPEED = 960f;
    public static final float PLAYER_DASH_DURATION = 0.14f;
    public static final float PLAYER_DASH_COOLDOWN = 0.55f;
    public static final float PLAYER_SHOOT_COOLDOWN = 0.18f;
    public static final int PLAYER_MAX_HEALTH = 5;

    public static final float BOSS_WIDTH = 170f;
    public static final float BOSS_HEIGHT = 230f;
    public static final float BOSS_START_X = 930f;
    public static final float BOSS_START_Y = FLOOR_Y;
    public static final int BOSS_MAX_HEALTH = 120;

    public static final float PLAYER_PROJECTILE_WIDTH = 22f;
    public static final float PLAYER_PROJECTILE_HEIGHT = 10f;
    public static final float PLAYER_PROJECTILE_SPEED = 760f;
    public static final int PLAYER_PROJECTILE_DAMAGE = 2;

    public static final float BOSS_PROJECTILE_WIDTH = 24f;
    public static final float BOSS_PROJECTILE_HEIGHT = 24f;
    public static final int BOSS_PROJECTILE_DAMAGE = 1;

    private Constants() {
    }
}
