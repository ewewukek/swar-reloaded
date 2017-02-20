package ewewukek.swar.particle;

import ewewukek.swar.Particle;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Exhaust extends Particle {
    public static final float size = 8;

    private float x;
    private float y;
    private float xv;
    private float yv;
    private float luminosity;

    public Exhaust(float x, float y, float xv, float yv) {
        this.x = x;
        this.y = y;
        this.xv = xv;
        this.yv = yv;
        luminosity = 2;
    }

    @Override
    protected int getVertexCount() {
        return 5;
    }

    @Override
    protected int getTriangleCount() {
        return 4;
    }

    @Override
    protected boolean update(float delta) {
        x += xv * delta;
        y += yv * delta;
        xv *= 0.98f;
        yv *= 0.98f;
        luminosity -= 0.05f;
        return luminosity > 0;
    }

    @Override
    protected void draw(FloatBuffer fb, int i, IntBuffer ib) {
        float cr = Math.min(luminosity, 1);
        float cg = Math.min(0.5f * luminosity, 1);
        float cb = 0;
        float lw = 0;
        fb.put(x);  fb.put(y);  fb.put(0);  fb.put(0);
        fb.put(cr); fb.put(cg); fb.put(cb); fb.put(1);
        fb.put(lw);
        fb.put(0);
        fb.put(size);
        fb.put(x - size);   fb.put(y - size);   fb.put(-size);  fb.put(-size);
        fb.put(cr); fb.put(cg); fb.put(cb); fb.put(1);
        fb.put(lw);
        fb.put(0);
        fb.put(size);
        fb.put(x + size);   fb.put(y - size);   fb.put(size);   fb.put(-size);
        fb.put(cr); fb.put(cg); fb.put(cb); fb.put(1);
        fb.put(lw);
        fb.put(0);
        fb.put(size);
        fb.put(x + size);   fb.put(y + size);   fb.put(size);   fb.put(size);
        fb.put(cr); fb.put(cg); fb.put(cb); fb.put(1);
        fb.put(lw);
        fb.put(0);
        fb.put(size);
        fb.put(x - size);   fb.put(y + size);   fb.put(-size);  fb.put(size);
        fb.put(cr); fb.put(cg); fb.put(cb); fb.put(1);
        fb.put(lw);
        fb.put(0);
        fb.put(size);
        ib.put(i);  ib.put(i + 1);  ib.put(i + 2);
        ib.put(i);  ib.put(i + 2);  ib.put(i + 3);
        ib.put(i);  ib.put(i + 3);  ib.put(i + 4);
        ib.put(i);  ib.put(i + 4);  ib.put(i + 1);
    }
}