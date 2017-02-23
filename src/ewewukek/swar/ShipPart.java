package ewewukek.swar;

import static ewewukek.swar.Util.*;

public class ShipPart extends Entity {
    private float luminosity;

    private int team;
    private float x1;
    private float y1;
    private float x2;
    private float y2;

    public ShipPart(float x, float y, float a,
                    float xv, float yv, float av,
                    int team,
                    float x1, float y1, float x2, float y2) {
        x1 += x;
        y1 += y;
        x2 += x;
        y2 += y;
        this.x = (x1 + x2) * 0.5f;
        this.y = (y1 + y2) * 0.5f;
        this.a = a;
        this.xv = xv;
        this.yv = yv;
        this.av = av;
        this.team = team;
        this.x1 = x1 - this.x;
        this.y1 = y1 - this.y;
        this.x2 = x2 - this.x;
        this.y2 = y2 - this.y;
        luminosity = 1.2f;
    }

    @Override
    public boolean update() {
        super.update();
        luminosity -= 0.02f;
        return luminosity > 0;
    }

    @Override
    public void draw(Batch batch) {
        batch.setDefaults();
        batch.setOrigin(x, y);
        batch.setRotation(a);
        float cm = Math.min(1, luminosity);
        batch.setColor(
            Ship.teamColorR[team] * cm,
            Ship.teamColorG[team] * cm,
            Ship.teamColorB[team] * cm,
            1
        );
        batch.setLineParams(0.5f, 0, Ship.glowRadius, Ship.falloffMultiplier);
        batch.setGlowShift(-xv, -yv);
        batch.addLine(x1, y1, x2, y2);
    }
}