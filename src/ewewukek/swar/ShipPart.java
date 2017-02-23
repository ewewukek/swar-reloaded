package ewewukek.swar;

public class ShipPart extends Entity {
    private float luminosity;

    private float colorR;
    private float colorG;
    private float colorB;
    private float colorA;
    
    private float x1;
    private float y1;
    private float x2;
    private float y2;

    public ShipPart(float x, float y, float a,
                    float xv, float yv, float av,
                    float cr, float cg, float cb, float ca,
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
        this.colorR = cr;
        this.colorG = cg;
        this.colorB = cb;
        this.colorA = ca;
        this.x1 = x1 - this.x;
        this.y1 = y1 - this.y;
        this.x2 = x2 - this.x;
        this.y2 = y2 - this.y;
        luminosity = 1.2f;
    }

    @Override
    public boolean update(float delta) {
        super.update(delta);
        luminosity -= 0.02f * delta;
        return luminosity > 0;
    }

    @Override
    public void draw(Batch batch) {
        batch.setLineWidth(0.25f);
        batch.setLineOffset(0);
        batch.setGlowRadius(20);
        batch.setFalloffMultiplier(7.5f);
        batch.setRotation(a);
        batch.setPosition(x, y);
        batch.setGlowShift(-xv, -yv);
        float cm = Math.min(1, luminosity);
        batch.setColor(colorR * cm, colorG * cm, colorB * cm, colorA);
        batch.addLine(x1, y1, x2, y2);
    }
}