package ewewukek.swar;

public class Particle extends Entity {
    protected float luminosity;
    protected float luminosityModifier;

    protected float colorR;
    protected float colorG;
    protected float colorB;
    protected float colorA;

    public Particle() {};

    public Particle(float x, float y, float a, float dist, float speed,
                    float colorR, float colorG, float colorB, float colorA,
                    float luminosity, float luminosityModifier) {
        this.x = x + (float)Math.sin(a) * dist;
        this.y = y + (float)Math.cos(a) * dist;
        xv = (float)Math.sin(a) * speed;
        yv = (float)Math.cos(a) * speed;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
        this.colorA = colorA;
        this.luminosity = luminosity;
        this.luminosityModifier = luminosityModifier;
    }

    @Override
    public boolean update() {
        super.update();
        xv *= 0.95f;
        yv *= 0.95f;
        luminosity -= luminosityModifier;
        return luminosity > 0;
    }

    @Override
    public void draw(Batch batch) {
        batch.setDefaults();
        batch.setOrigin(x, y);
        batch.setGlowShift(-xv, -yv);
        float cr = Math.min(colorR * luminosity, 1);
        float cg = Math.min(colorG * luminosity, 1);
        float cb = Math.min(colorB * luminosity, 1);
        float ca = colorA;
        batch.setColor(cr, cg, cb, ca);
        batch.addLine(0, 0, -xv, -yv);
    }
}