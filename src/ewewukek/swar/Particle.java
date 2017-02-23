package ewewukek.swar;

public class Particle extends Entity {
    protected float luminosity;
    protected float luminosityModifier = 0.05f;

    protected float colorR;
    protected float colorG;
    protected float colorB;
    protected float colorA;

    public Particle() {};

    public Particle(float x, float y, float a, float dist, float speed,
                    float R, float G, float B, float A,
                    float luminosity, float luminosityModifier) {
        this.x = x + (float)Math.sin(a) * dist;
        this.y = y + (float)Math.cos(a) * dist;
        this.xv = (float)Math.sin(a) * speed;
        this.yv = (float)Math.cos(a) * speed;
        colorR = R;
        colorG = G;
        colorB = B;
        colorA = A;
        this.luminosity = luminosity;
        this.luminosityModifier = luminosityModifier;
    }

    @Override
    public boolean update(float delta) {
        super.update(delta);
        xv *= 0.95f;
        yv *= 0.95f;
        luminosity -= luminosityModifier;
        return luminosity > 0;
    }

    @Override
    public void draw(Batch batch) {
        batch.setPosition(x, y);
        batch.setRotation(0);
        batch.setLineWidth(0.5f);
        batch.setLineOffset(0);
        batch.setGlowRadius(10);
        batch.setFalloffMultiplier(10);
        batch.setGlowShift(-xv, -yv);
        float cr = Math.min(colorR * luminosity, 1);
        float cg = Math.min(colorG * luminosity, 1);
        float cb = Math.min(colorB * luminosity, 1);
        float ca = colorA;
        batch.setColor(cr, cg, cb, ca);
        batch.addLine(0, 0, -xv, -yv);
    }
}