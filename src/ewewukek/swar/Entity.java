package ewewukek.swar;

public abstract class Entity {
    protected float x;
    protected float y;
    protected float a;

    protected float xv;
    protected float yv;
    protected float av;

    public boolean update(float delta) {
        x += xv * delta;
        y += yv * delta;
        a += av * delta;
        return true;
    }

    public abstract void draw(Batch batch);
}