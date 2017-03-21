package ewewukek.swar;

public class Particle extends Entity {
    public static final float frictionMultiplier = 0.86f;
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
        xv *= frictionMultiplier;
        yv *= frictionMultiplier;
        luminosity -= luminosityModifier;
        return luminosity > 0;
    }

    @Override
    public void draw(Batch batch, float delta) {
        batch.setDefaults();
        batch.setOrigin(x + xv * delta, y + yv * delta);
        batch.setGlowShift(-xv, -yv);
        float l = luminosity - luminosityModifier * delta;
        float cr = Math.min(colorR * l, 1);
        float cg = Math.min(colorG * l, 1);
        float cb = Math.min(colorB * l, 1);
        float ca = colorA;
        batch.setColor(cr, cg, cb, ca);
        if (xv == 0 && yv == 0) {
            batch.addPoint(0, 0);
        } else {
            batch.addLine(0, 0, -xv, -yv);
        }
    }
}