package ewewukek.swar;

public abstract class Entity {
    protected float x;
    protected float y;
    protected float a;

    protected float xv;
    protected float yv;
    protected float av;

    public boolean update() {
        x += xv;
        y += yv;
        a += av;
        return true;
    }

    public abstract void draw(Batch batch);
}