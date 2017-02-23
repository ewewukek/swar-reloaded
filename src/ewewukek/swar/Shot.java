package ewewukek.swar;

import static ewewukek.swar.Util.*;

public class Shot extends Entity {
    public static final float speed = 7.5f;

    private int team;
    private float t = 0;

    public Shot(int team, float shipX, float shipY, float shipA, float dist) {
        this.team = team;
        float s = (float)Math.sin(shipA);
        float c = (float)Math.cos(shipA);
        x = shipX + s * (dist + speed);
        y = shipY + c * (dist + speed);
        a = shipA;
        xv = s * speed;
        yv = c * speed;
        av = 0;
    }

    @Override
    public boolean update() {
        super.update();
        t += 0.05f;
        if (t > 1) t -= 1;
        if (x > 0) {
            effectExplosion();
            return false;
        }
        return x > -Game.WIDTH / 2 && x < Game.WIDTH / 2
            && y > -Game.HEIGHT / 2 && y < Game.HEIGHT / 2;
    }

    @Override
    public void draw(Batch batch) {
        batch.setOrigin(x, y);
        batch.setRotation(a);
        float tm = (float)Math.cos(t * 2 * Math.PI);
        float cm = 0.9f + tm * 0.1f;
        batch.setColor(
            Ship.teamColorR[team] * cm,
            Ship.teamColorG[team] * cm,
            Ship.teamColorB[team] * cm,
            1
        );
        batch.setLineParams(0.5f, 0, 20, 4 + 2 * (1 - tm));
        batch.setGlowShift(-xv * 20 / speed, -yv * 20 / speed);
        batch.addLine(0, 0, 0, -speed);
    }

    public void effectExplosion() {
        for (int i = 0; i != 20; ++i) {
            float a = rand() * 2 * (float)Math.PI;
            float r = 1 + rand() * 9;
            Game.addEntity(new Particle(
                x, y, a, r, 2,
                Ship.teamColorR[team],
                Ship.teamColorG[team],
                Ship.teamColorB[team],
                1,
                1.5f, 0.08f
            ));
        }
    }
}