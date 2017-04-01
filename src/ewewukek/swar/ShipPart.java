package ewewukek.swar;

import static ewewukek.swar.Util.*;

public class ShipPart extends Entity {
    public static final float glowRadius = 25;
    private float luminosity;

    private int team;
    private float x1;
    private float y1;
    private float x2;
    private float y2;

    public ShipPart(float shipX, float shipY, float shipA,
                    float shipXV, float shipYV, int team,
                    float px1, float py1, float px2, float py2) {
        float s = (float)Math.sin(shipA);
        float c = (float)Math.cos(shipA);
        x1 = c * px1 + s * py1;
        y1 = -s * px1 + c * py1;
        x2 = c * px2 + s * py2;
        y2 = -s * px2 + c * py2;
        x = (x1 + x2) * 0.5f;
        y = (y1 + y2) * 0.5f;
        xv = x * 0.1f;
        yv = y * 0.1f;
        x1 -= x;
        y1 -= y;
        x2 -= x;
        y2 -= y;
        x += shipX;
        y += shipY;
        xv += shipXV * 0.1f;
        yv += shipYV * 0.1f;
        av = (rand() - 0.5f) * 0.1f;
        this.team = team;
        luminosity = 1.2f;
    }

    @Override
    public boolean update() {
        super.update();
        luminosity -= 0.05f;
        return luminosity > 0;
    }

    @Override
    public void draw(Batch batch, float delta) {
        batch.setDefaults();
        batch.setOrigin(x + xv * delta, y + yv * delta);
        batch.setRotation(a + av * delta);
        float cm = Math.min(1, luminosity);
        batch.setColor(
            Ship.teamColorR[team] * cm,
            Ship.teamColorG[team] * cm,
            Ship.teamColorB[team] * cm,
            1
        );
        batch.setGlowRadius(glowRadius);
        batch.setGlowShift(-xv, -yv);
        batch.addLine(x1, y1, x2, y2);
    }
}