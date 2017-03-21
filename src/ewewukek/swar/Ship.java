package ewewukek.swar;

import org.joml.Vector2f;

import static ewewukek.swar.Util.*;

public class Ship extends Entity {
    public static final float size = 17.5f;
    public static final float glowRadius = 25;
    public static final float falloffMultiplier = 6.5f;

    public static final float acceleration = 5;
    public static final float maxVelocity = 12;
    public static final float friction = 1.2f;
    public static final float turnAcceleration = (float)Math.toRadians(18);
    public static final float maxAngularVelocity = (float)Math.toRadians(18);
    public static final float minAngularVelocity = (float)Math.toRadians(1);
    public static final float angularFriction = 0.5f;

    public static final int startHp = 2;
    public static final float shotDelay = 0.7f;
    public static final float startShieldTime = 1.5f;
    public static final float respawnDelay = 2;

    public static final int teamCount = 4;
    public static final float[] teamColorR = new float[] {     1, 0.5f,  0.1f, 0.85f };
    public static final float[] teamColorG = new float[] { 0.35f, 0.5f, 0.75f, 0.85f };
    public static final float[] teamColorB = new float[] { 0.35f,    1,  0.1f,     0 };

    private static int nextId = 1;

    public int id;

    public int team;

    public AI ai;

    public float inputTurn;
    public boolean keyThrottle;
    public boolean keyFire;

    private int hp;
    private float shieldTime;
    private float shotTime;

    private float respawnTime;

    public Ship() {
        id = nextId;
        ++nextId;
    }

    public void placeRandom() {
        x = (rand() - 0.5f) * Game.WIDTH;
        y = (rand() - 0.5f) * Game.HEIGHT;
        xv = 0;
        yv = 0;
    }

    public void spawn() {
        shieldTime = startShieldTime;
        hp = 2;
        effectSpawn();
    }

    public boolean alive() {
        return hp > 0;
    }

    public void hit() {
        if (shieldTime > 0) return;
        if (hp < 1) return;
        hp--;
        if (hp < 1) {
            effectExplosion();
            x = (rand() - 0.5f) * Game.WIDTH;
            y = (rand() - 0.5f) * Game.HEIGHT;
            a = rand() * 2 * (float)Math.PI;
            xv = 0;
            yv = 0;
            av = 0;
            respawnTime = time() + respawnDelay;
        }
    }

    public boolean isShieldActive() {
        return shieldTime > 0;
    }

    @Override
    public boolean update() {
        if (hp < 1) {
            if (respawnTime != 0 && respawnTime < time()) spawn();
            return true;
        }
        super.update();
        if (ai != null) ai.update(this);
        if (Math.abs(av) < minAngularVelocity) {
            av = 0;
        } else {
            av *= angularFriction;
        }
        av += Math.max(-1, Math.min(1, inputTurn)) * turnAcceleration;
        av = Math.max(-maxAngularVelocity, Math.min(maxAngularVelocity, av));
        if (keyThrottle) {
            xv += (float)Math.sin(a) * acceleration;
            yv += (float)Math.cos(a) * acceleration;
            float speed = (float)Math.sqrt(xv * xv + yv * yv);
            if (speed > maxVelocity) {
                xv *= maxVelocity / speed;
                yv *= maxVelocity / speed;
            }
        }
        if (keyFire) {
            if (shotTime < time()) {
                Game.addShot(new Shot(team, x, y, a, size));
                shotTime = time() + shotDelay;
            }
        }
        if (x < -Game.WIDTH / 2) x += Game.WIDTH;
        if (x >= Game.WIDTH / 2) x -= Game.WIDTH;
        if (y < -Game.HEIGHT / 2) y += Game.HEIGHT;
        if (y >= Game.HEIGHT / 2) y -= Game.HEIGHT;
        float speed = (float)Math.sqrt(xv * xv + yv * yv);
        if (speed < friction) {
            xv = 0;
            yv = 0;
        } else {
            xv *= (speed - friction) / speed;
            yv *= (speed - friction) / speed;
        }
        if (shieldTime > 0) shieldTime -= 0.05f;
        return true;
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (hp < 1 && !Game.isPlayerShip(this)) return;
        if (hp > 0 && keyThrottle) effectExhaust(delta);
        batch.setDefaults();
        batch.setRotation(a + av * delta);
        float s = rand();
        float dx = xv * (0.3f + s * 0.2f) * glowRadius / maxVelocity;
        float dy = yv * (0.3f + s * 0.2f) * glowRadius / maxVelocity;
        float shieldLuminance = Math.min(1, 0.5f + 2 * (shieldTime - 0.05f * delta));
        float cm = 1;
        if (hp < 1) {
            cm = (respawnTime - time()) * 1.5f;
            cm = cm - (int)cm;
            cm = Math.abs(cm - 0.5f) + 0.25f;
        }
        for (int i = -1; i != 2; ++i) {
            for (int j = -1; j != 2; ++j) {
                if (x + i * Game.WIDTH + size + glowRadius > -Game.WIDTH / 2
                 && x + i * Game.WIDTH - size - glowRadius < Game.WIDTH / 2
                 && y + j * Game.HEIGHT + size + glowRadius > -Game.HEIGHT / 2
                 && y + j * Game.HEIGHT - size - glowRadius < Game.HEIGHT / 2) {
                    batch.setOrigin(x + xv * delta + i * Game.WIDTH, y + yv * delta + j * Game.HEIGHT);
                    batch.setColor(cm * teamColorR[team], cm * teamColorG[team], cm * teamColorB[team], 1);
                    batch.setLineParams(0.5f, 0, glowRadius, falloffMultiplier);
                    batch.setGlowShift(-dx, -dy);
                    batch.addArrays(px, py, lx, ly, tris, gs);
                    if (hp > 0 && shieldTime > 0) {
                        batch.setColor(0, shieldLuminance, shieldLuminance * 0.8f, 1);
                        batch.setLineParams(0.5f, size + 10, 10, falloffMultiplier);
                        batch.setGlowShift(0, 0);
                        batch.addPoint(0, 0);
                    }
                }
            }
        }
    }

    private void effectExhaust(float delta) {
        Game.addLocalEntity(new Particle(
            x + xv * delta, y + yv * delta, a + av * delta, -size * 0.6f, -4 * (float)Math.pow(Particle.frictionMultiplier, delta),
            1, 0.5f, 0, 1,
            1.7f, 0.15f
        ));
    }

    private void effectSpawn() {
        for (int i = 0; i != 25; ++i) {
            Game.addLocalEntity(new Particle(
                x, y,
                rand() * 2 * (float)Math.PI,
                rand() * 10,
                4 + rand() * 4,
                0, 1, 0.5f, 1,
                1.2f + rand() * 0.7f, 0.04f
            ));
        }
    }

    private void effectExplosion() {
        for (int i = 0; i != 50; ++i) {
            Game.addLocalEntity(new Particle(
                x, y,
                rand() * 2 * (float)Math.PI,
                rand() * 10,
                5 + rand() * 5,
                1, 0.2f + rand() * 0.3f, 0, 1,
                1.2f + rand() * 0.3f, 0.035f
            ));
        }
        Game.addLocalEntity(new ShipPart(
            x, y, a, xv, yv, team,
            px[0], py[0], px[1], py[1]
        ));
        Game.addLocalEntity(new ShipPart(
            x, y, a, xv, yv, team,
            px[0], py[0], px[3], py[3]
        ));
        Game.addLocalEntity(new ShipPart(
            x, y, a, xv, yv, team,
            px[1], py[1], px[2], py[2]
        ));
        Game.addLocalEntity(new ShipPart(
            x, y, a, xv, yv, team,
            px[2], py[2], px[3], py[3]
        ));
    }

    private static final float[] px = new float[22];
    private static final float[] py = new float[22];
    private static final float[] lx = new float[22];
    private static final float[] ly = new float[22];
    private static final float[] gs = new float[22];
    private static final int[] tris = new int[] {
        0,  4,  5,
        0,  5,  6,
        0,  6,  7,
        0,  7,  8,
        8,  1,  0,
        1,  8,  9,
        1,  9, 10,
        1, 10, 11,
        2,  1, 12,
        1, 11, 12,
        3,  2, 13,
        3, 13, 14,
        3, 14, 15,
        3, 15, 16,
        3, 16, 17,
        0,  3, 17,
        0, 17,  4,
        0,  1, 18,
        1,  2, 19,
        2,  3, 20,
        3,  0, 21
    };

    static {
        px[0] = 0;
        py[0] = size;
        px[1] = (float)Math.sin(Math.PI * 0.75) * size;
        py[1] = (float)Math.cos(Math.PI * 0.75) * size;
        px[2] = 0;
        py[2] = -0.5f * size;
        px[3] = -px[1];
        py[3] = py[1];
        Vector2f n1 = new Vector2f(py[0] - py[1], px[1] - px[0]).normalize().mul(glowRadius);
        px[4] = px[0] - n1.x;     py[4] = py[0] + n1.y;     lx[4] =-n1.x;   ly[4] = n1.y;
        Vector2f t1 = intersect(n1.x, n1.y, 0, glowRadius);
        px[5] = px[0] - t1.x;     py[5] = py[0] + t1.y;     lx[5] =-t1.x;   ly[5] = t1.y;
        px[6] = px[0] + t1.x;     py[6] = py[0] + t1.y;     lx[6] = t1.x;   ly[6] = t1.y;
        px[7] = px[0] + n1.x;     py[7] = py[0] + n1.y;     lx[7] = n1.x;   ly[7] = n1.y;
        px[8] = px[1] + n1.x;     py[8] = py[1] + n1.y;     lx[8] = n1.x;   ly[8] = n1.y;
        Vector2f n2 = new Vector2f(py[1] - py[2], px[2] - px[1]).normalize().mul(glowRadius);
        Vector2f v1 = new Vector2f(n1).add(n2).normalize().mul(glowRadius);
        Vector2f t2 = intersect(n1.x, n1.y, v1.x, v1.y);
        px[9] = px[1] + t2.x;     py[9] = py[1] + t2.y;     lx[9] = t2.x;   ly[9] = t2.y;
        Vector2f t3 = new Vector2f(v1).sub(t2).mul(2).add(t2);
        px[10] = px[1] + t3.x;    py[10] = py[1] + t3.y;    lx[10] = t3.x;  ly[10] = t3.y;
        px[11] = px[1] + n2.x;    py[11] = py[1] + n2.y;    lx[11] = n2.x;  ly[11] = n2.y;
        Vector2f t4 = intersect(n2.x, n2.y, -n2.x, n2.y).normalize().mul(glowRadius);
        px[12] = px[2] + t4.x;    py[12] = py[2] + t4.y;    lx[12] = n2.x;  ly[12] = n2.y;
        px[13] = px[2] + t4.x;    py[13] = py[2] + t4.y;    lx[13] =-n2.x;  ly[13] = n2.y;
        px[14] = px[3] - n2.x;    py[14] = py[3] + n2.y;    lx[14] =-n2.x;  ly[14] = n2.y;
        px[15] = px[3] - t3.x;    py[15] = py[3] + t3.y;    lx[15] =-t3.x;  ly[15] = t3.y;
        px[16] = px[3] - t2.x;    py[16] = py[3] + t2.y;    lx[16] =-t2.x;  ly[16] = t2.y;
        px[17] = px[3] - n1.x;    py[17] = py[3] + n1.y;    lx[17] =-n1.x;  ly[17] = n1.y;
        float cy = py[1] - v1.y * (px[1] / v1.x);
        float n1mul = (px[0] * n1.x + (py[0] - cy) * n1.y) / (glowRadius * glowRadius);
        float n2mul = (px[2] * n2.x + (py[2] - cy) * n2.y) / (glowRadius * glowRadius);
        px[18] = 0;  py[18] = cy; lx[18] =-n1.x * n1mul;  ly[18] =-n1.y * n1mul;
        px[19] = 0;  py[19] = cy; lx[19] =-n2.x * n2mul;  ly[19] =-n2.y * n2mul;
        px[20] = 0;  py[20] = cy; lx[20] = n2.x * n2mul;  ly[20] =-n2.y * n2mul;
        px[21] = 0;  py[21] = cy; lx[21] = n1.x * n1mul;  ly[21] =-n1.y * n1mul;
        for (int i = 4; i != 18; ++i) gs[i] = 1;
    }
}