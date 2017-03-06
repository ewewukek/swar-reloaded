package ewewukek.swar;

import static ewewukek.swar.Util.*;

public class Shot extends Entity {
    public static final float speed = 20f;
    public static final float length = 15f;

    public int team;

    private boolean alive = true;

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

    public void hit(boolean hitShield) {
        effectExplosion(hitShield);
        alive = false;
    }

    @Override
    public boolean update() {
        super.update();
        return alive && x > -Game.WIDTH / 2 && x < Game.WIDTH / 2
            && y > -Game.HEIGHT / 2 && y < Game.HEIGHT / 2;
    }

    @Override
    public void draw(Batch batch, float delta) {
        batch.setOrigin(x + xv * delta, y + yv * delta);
        batch.setRotation(a);
        batch.setColor(
            Ship.teamColorR[team],
            Ship.teamColorG[team],
            Ship.teamColorB[team],
            1
        );
        batch.setLineParams(0.5f, 0, 15, 6.5f);
        batch.setGlowShift(-xv * 20 / speed, -yv * 20 / speed);
        batch.addLine(0, 0, 0, -length);
    }

    public void effectExplosion(boolean hitShield) {
        for (int i = 0; i != 20; ++i) {
            float a = rand() * 2 * (float)Math.PI;
            float r = 1 + rand() * 9;
            Game.addLocalEntity(new Particle(
                x, y, a, r, 5,
                hitShield ? 0.0f : Ship.teamColorR[team],
                hitShield ? 1.0f : Ship.teamColorG[team],
                hitShield ? 0.8f : Ship.teamColorB[team],
                1,
                1.5f, 0.2f
            ));
        }
    }
}