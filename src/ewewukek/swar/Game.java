package ewewukek.swar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static ewewukek.swar.Util.*;

public class Game {
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;

    public static final float TIME_STEP = 0.05f;

    private long tick = 0;

    private static List<Entity> localEntities = new ArrayList<Entity>();
    private static List<Entity> entities = new ArrayList<Entity>();
    private static List<Entity> newEntities = new ArrayList<Entity>();

    public static void addEntity(Entity e) {
        newEntities.add(e);
    }

    public static void addLocalEntity(Entity e) {
        localEntities.add(e);
    }

    private static void drawList(List<Entity> list, Batch batch, float delta) {
        Iterator<Entity> it = list.iterator();
        while (it.hasNext()) {
            Entity e = (Entity)it.next();
            e.draw(batch, delta);
        }
    }

    public static void draw(Batch batch, float delta) {
        drawList(entities, batch, delta);
        drawList(localEntities, batch, delta);
    }

    private static void updateList(List<Entity> list) {
        Iterator<Entity> it = list.iterator();
        while (it.hasNext()) {
            Entity e = (Entity)it.next();
            if (!e.update()) {
                it.remove();
            }
        }
    }

    public static void update() {
        entities.addAll(newEntities);
        newEntities.clear();
        updateList(entities);
        updateList(localEntities);
    }
}