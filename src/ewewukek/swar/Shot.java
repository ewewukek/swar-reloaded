package ewewukek.swar;

public class Shot extends Entity {
    public static final float speed = 7.5f;
    // public static final float speed = 1;
    public float t = 0;

    public Shot(float shipX, float shipY, float shipA, float dist) {
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
    public boolean update(float delta) {
        super.update(delta);
        t += 0.05f * delta;
        if (t > 1) t -= 1;
        if (x > 0) {
            effectExplosion();
            return false;
        }
        return x > -Game.WIDTH / 2 && x < Game.WIDTH / 2
            && y > -Game.HEIGHT / 2 && y < Game.HEIGHT / 2;
    }

    public void effectExplosion() {
        for (int i = 0; i != 20; ++i) {
            float a = Game.rnd.nextFloat() * 2 * (float)Math.PI;
            float r = 1 + Game.rnd.nextFloat() * 9;
            float s = (float)Math.sin(a);
            float c = (float)Math.cos(a);
            Game.addEntity(new Particle(
                x, y, a, r, 2,
                1.0f, 0.3f, 0.3f, 1.0f,
                // 0.0f, 1f, 0.5f, 1.0f,
                1.5f, 0.08f
            ));
        }
    }

    @Override
    public void draw(Batch batch) {
        batch.setPosition(x, y);
        batch.setRotation(a);
        batch.setLineWidth(0.5f);
        batch.setLineOffset(0);
        float tm = (float)Math.cos(t * 2 * Math.PI);
        batch.setGlowRadius(20);
        batch.setFalloffMultiplier(4 + 2 * (1 - tm));
        batch.setGlowShift(-xv * 20 / speed, -yv * 20 / speed);
        float cm = 0.9f + tm * 0.1f;
        batch.setColor(cm, cm * 0.25f, cm * 0.25f, 1);
        batch.addLine(0, 0, 0, -15);
    }
}