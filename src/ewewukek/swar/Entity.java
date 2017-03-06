package ewewukek.swar;

public abstract class Entity {
    public float x;
    public float y;
    public float a;

    public float xv;
    public float yv;
    public float av;

    public boolean update() {
        x += xv;
        y += yv;
        a += av;
        return true;
    }

    public abstract void draw(Batch batch, float delta);
}