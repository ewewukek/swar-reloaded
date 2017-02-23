package ewewukek.swar;

import org.joml.Vector2f;

import static ewewukek.swar.Util.*;

public class Ship extends Entity {
    public static final float size = 17.5f;
    public static final float line_width = 25;

    public static float turn_acceleration = (float)(Math.PI / 100);
    public static float max_angle_velocity = (float)(Math.PI / 40);
    public static float acceleration = 1.0f;
    public static float max_velocity = 4.0f;
    public static float friction = 0.08f;
    public static float angle_friction = (float)(Math.PI / 360);

    private float shield;

    public Ship() {
    }

    public float cr = 1;
    public float cg = 1;
    public float cb = 1;

    public void turn_left(float delta) {
        if (av > -max_angle_velocity) {
            av -= turn_acceleration * delta;
        } else {
            av = -max_angle_velocity;
        }
    }

    public void turn_right(float delta) {
        if (av < max_angle_velocity) {
            av += turn_acceleration * delta;
        } else {
            av = max_angle_velocity;
        }
    }

    public void throttle(float delta) {
        double speed = Math.sqrt(xv * xv + yv * yv);
        if (speed < max_velocity) {
            xv += (float)Math.sin(a) * acceleration * delta;
            yv += (float)Math.cos(a) * acceleration * delta;
        } else {
            xv *= max_velocity / speed;
            yv *= max_velocity / speed;
        }
        effectExhaust();
    }

    public void shoot() {
        Game.addEntity(new Shot(
            x,
            y,
            a,
            size
        ));
    }

    public void effectExhaust() {
        Game.addEntity(new Particle(
            x, y, a, -size * 0.5f, -max_velocity,
            1.0f, 0.5f, 0.0f, 1.0f,
            2.0f, 0.1f
        ));
    }

    public void effectSpawn() {
        for (int i = 0; i != 25; ++i) {
            Game.addEntity(new Particle(
                x, y, Game.rnd.nextFloat() * 2 * (float)Math.PI, Game.rnd.nextFloat() * 10, 1 + Game.rnd.nextFloat() * 2,
                0.0f, 1.0f, 0.5f, 1.0f,
                1 + Game.rnd.nextFloat() * 0.3f, 0.02f
            ));
        }
    }

    public void effectExplosion() {
        for (int i = 0; i != 50; ++i) {
            Game.addEntity(new Particle(
                x, y, Game.rnd.nextFloat() * 2 * (float)Math.PI, Game.rnd.nextFloat() * 10, 1.5f + Game.rnd.nextFloat() * 2,
                1.0f, 0.2f + Game.rnd.nextFloat() * 0.3f, 0.0f, 1.0f,
                1.2f + Game.rnd.nextFloat() * 0.3f, 0.015f
            ));
        }
        Game.addEntity(new ShipPart(
            x, y, a,
            (float)Math.sin(a + Math.PI * 0.375) + xv * 0.1f, (float)Math.cos(a + Math.PI * 0.375) + yv * 0.1f, (Game.rnd.nextFloat() - 0.5f) * 0.02f,
            1.0f, 0.35f, 0.35f, 1.0f,
            px[0], py[0], px[1], py[1]
        ));
        Game.addEntity(new ShipPart(
            x, y, a,
            (float)Math.sin(a - Math.PI * 0.375) + xv * 0.1f, (float)Math.cos(a - Math.PI * 0.375) + yv * 0.1f, (Game.rnd.nextFloat() - 0.5f) * 0.02f,
            1.0f, 0.35f, 0.35f, 1.0f,
            px[0], py[0], px[3], py[3]
        ));
        Game.addEntity(new ShipPart(
            x, y, a,
            (float)Math.sin(a + Math.PI * 0.875) + xv * 0.1f, (float)Math.cos(a + Math.PI * 0.875) + yv * 0.1f, (Game.rnd.nextFloat() - 0.5f) * 0.02f,
            1.0f, 0.35f, 0.35f, 1.0f,
            px[1], py[1], px[2], py[2]
        ));
        Game.addEntity(new ShipPart(
            x, y, a,
            (float)Math.sin(a - Math.PI * 0.875) + xv * 0.1f, (float)Math.cos(a - Math.PI * 0.875) + yv * 0.1f, (Game.rnd.nextFloat() - 0.5f) * 0.02f,
            1.0f, 0.35f, 0.35f, 1.0f,
            px[2], py[2], px[3], py[3]
        ));
    }

    public void respawn(float x, float y) {
        this.x = x;
        this.y = y;
        this.shield = 1.5f;
        effectSpawn();
    }

    @Override
    public boolean update(float delta) {
        x += xv * delta;
        y += yv * delta;
        a += av * delta;
        if (shield > 0) shield -= 0.016f * delta;

        if (x < -Game.WIDTH / 2) {
            x += Game.WIDTH;
        }
        if (x >= Game.WIDTH / 2) {
            x -= Game.WIDTH;
        }
        if (y < -Game.HEIGHT / 2) {
            y += Game.HEIGHT;
        }
        if (y >= Game.HEIGHT / 2) {
            y -= Game.HEIGHT;
        }

        double speed = Math.sqrt(xv * xv + yv * yv);
        if (speed < friction) {
            xv = 0;
            yv = 0;
        } else {
            xv *= (speed - friction * delta) / speed;
            yv *= (speed - friction * delta) / speed;
        }
        if (Math.abs(av) < angle_friction) {
            av = 0;
        } else {
            av -= Math.signum(av) * angle_friction;
        }
        return true;
    }

    @Override
    public void draw(Batch batch) {
        batch.setColor(1, 0.35f, 0.35f, 1);
        batch.setLineWidth(0.5f);
        batch.setLineOffset(0);
        batch.setGlowRadius(line_width);
        batch.setFalloffMultiplier(6.5f);
        batch.setRotation(a);
        float s = Game.rnd.nextFloat();
        float dx = xv * (0.3f + s * 0.2f) * line_width / max_velocity;
        float dy = yv * (0.3f + s * 0.2f) * line_width / max_velocity;
        for (int i = -1; i != 2; ++i) {
            for (int j = -1; j != 2; ++j) {
                if (x + i * Game.WIDTH + size + line_width > -Game.WIDTH / 2
                 && x + i * Game.WIDTH - size - line_width < Game.WIDTH / 2
                 && y + j * Game.HEIGHT + size + line_width > -Game.HEIGHT / 2
                 && y + j * Game.HEIGHT - size - line_width < Game.HEIGHT / 2) {
                    batch.setPosition(x + i * Game.WIDTH, y + j * Game.HEIGHT);
                    batch.setGlowShift(-dx, -dy);
                    batch.addArrays(px, py, lx, ly, tris, gs);
                }
            }
        }
        if (shield > 0) {
            float cm = Math.min(1.0f, 2 * shield);
            batch.setRotation(0);
            batch.setLineWidth(0);
            batch.setLineOffset(size + 10);
            batch.setGlowRadius(10);
            batch.setFalloffMultiplier(5);
            batch.setColor(0, cm * 1, cm * 0.8f, 1);
            batch.setGlowShift(0, 0);
            batch.addPoint(0, 0);
        }
    }

    static final float[] px = new float[22];
    static final float[] py = new float[22];
    static final float[] lx = new float[22];
    static final float[] ly = new float[22];
    static final float[] gs = new float[22];
    static final int[] tris = new int[] {
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
        float sin = (float)Math.sin(Math.PI*0.75);
        float cos = (float)Math.cos(Math.PI*0.75);

        px[0] = 0;
        py[0] = size;
        px[1] = (float)Math.sin(Math.PI * 0.75) * size;
        py[1] = (float)Math.cos(Math.PI * 0.75) * size;
        px[2] = 0;
        py[2] = -0.5f * size;
        px[3] = -px[1];
        py[3] = py[1];

        Vector2f n1 = new Vector2f(py[0] - py[1], px[1] - px[0]).normalize().mul(line_width);
        px[4] = px[0] - n1.x;     py[4] = py[0] + n1.y;     lx[4] =-n1.x;   ly[4] = n1.y;
        Vector2f t1 = intersect(n1.x, n1.y, 0, line_width);
        px[5] = px[0] - t1.x;     py[5] = py[0] + t1.y;     lx[5] =-t1.x;   ly[5] = t1.y;
        px[6] = px[0] + t1.x;     py[6] = py[0] + t1.y;     lx[6] = t1.x;   ly[6] = t1.y;
        px[7] = px[0] + n1.x;     py[7] = py[0] + n1.y;     lx[7] = n1.x;   ly[7] = n1.y;
        px[8] = px[1] + n1.x;     py[8] = py[1] + n1.y;     lx[8] = n1.x;   ly[8] = n1.y;
        Vector2f n2 = new Vector2f(py[1] - py[2], px[2] - px[1]).normalize().mul(line_width);
        Vector2f v1 = new Vector2f(n1).add(n2).normalize().mul(line_width);
        Vector2f t2 = intersect(n1.x, n1.y, v1.x, v1.y);
        px[9] = px[1] + t2.x;     py[9] = py[1] + t2.y;     lx[9] = t2.x;   ly[9] = t2.y;
        Vector2f t3 = new Vector2f(v1).sub(t2).mul(2).add(t2);
        px[10] = px[1] + t3.x;    py[10] = py[1] + t3.y;    lx[10] = t3.x;  ly[10] = t3.y;
        px[11] = px[1] + n2.x;    py[11] = py[1] + n2.y;    lx[11] = n2.x;  ly[11] = n2.y;
        Vector2f t4 = intersect(n2.x, n2.y, -n2.x, n2.y).normalize().mul(line_width);
        px[12] = px[2] + t4.x;    py[12] = py[2] + t4.y;    lx[12] = n2.x;  ly[12] = n2.y;
        px[13] = px[2] + t4.x;    py[13] = py[2] + t4.y;    lx[13] =-n2.x;  ly[13] = n2.y;
        px[14] = px[3] - n2.x;    py[14] = py[3] + n2.y;    lx[14] =-n2.x;  ly[14] = n2.y;
        px[15] = px[3] - t3.x;    py[15] = py[3] + t3.y;    lx[15] =-t3.x;  ly[15] = t3.y;
        px[16] = px[3] - t2.x;    py[16] = py[3] + t2.y;    lx[16] =-t2.x;  ly[16] = t2.y;
        px[17] = px[3] - n1.x;    py[17] = py[3] + n1.y;    lx[17] =-n1.x;  ly[17] = n1.y;
        float cy = py[1] - v1.y * (px[1] / v1.x);
        float n1mul = (px[0] * n1.x + (py[0] - cy) * n1.y) / (line_width * line_width);
        float n2mul = (px[2] * n2.x + (py[2] - cy) * n2.y) / (line_width * line_width);
        px[18] = 0;  py[18] = cy; lx[18] =-n1.x * n1mul;  ly[18] =-n1.y * n1mul;
        px[19] = 0;  py[19] = cy; lx[19] =-n2.x * n2mul;  ly[19] =-n2.y * n2mul;
        px[20] = 0;  py[20] = cy; lx[20] = n2.x * n2mul;  ly[20] =-n2.y * n2mul;
        px[21] = 0;  py[21] = cy; lx[21] = n1.x * n1mul;  ly[21] =-n1.y * n1mul;
        for (int i = 4; i != 18; ++i) gs[i] = 1;
    }
}