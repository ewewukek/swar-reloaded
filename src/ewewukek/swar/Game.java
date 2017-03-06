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
    public static List<Entity> ships = new ArrayList<Entity>();
    public static List<Entity> shots = new ArrayList<Entity>();

    private static Ship playerShip;

    public static void addShip(Ship ship) {
        ships.add(ship);
    }

    public static void addShot(Shot shot) {
        shots.add(shot);
    }

    public static void addLocalEntity(Entity e) {
        localEntities.add(e);
    }

    public static boolean isPlayerShip(Ship ship) {
        return ship == playerShip;
    }

    public static void setPlayerTeam(int team) {
        if (playerShip == null) {
            playerShip = new Ship();
            addShip(playerShip);
        }
        playerShip.team = team;
        playerShip.x = (rand() - 0.5f) * WIDTH;
        playerShip.y = (rand() - 0.5f) * HEIGHT;
        playerShip.spawn();
    }

    public static void setPlayerKeys(boolean left, boolean right, boolean throttle, boolean fire) {
        if (playerShip == null) return;
        playerShip.keyLeft = left;
        playerShip.keyRight = right;
        playerShip.keyThrottle = throttle;
        playerShip.keyFire = fire;
    }

    private static void drawList(List<Entity> list, Batch batch, float delta) {
        Iterator<Entity> it = list.iterator();
        while (it.hasNext()) {
            Entity e = (Entity)it.next();
            e.draw(batch, delta);
        }
    }

    private static final float[] markerX = new float[] { 0, -5,  5 };
    private static final float[] markerY = new float[] { 0,  5,  5 };
    private static final float[] markerL = new float[] { 0, 0, 0 };
    private static final int[] markerTris = new int[] { 0, 1, 2 };

    public static void draw(Batch batch, float delta) {
        drawList(ships, batch, delta);
        drawList(shots, batch, delta);
        drawList(localEntities, batch, delta);
        if (playerShip != null) {
            batch.setDefaults();
            batch.setOrigin(
                playerShip.x + playerShip.xv * delta,
                playerShip.y + Ship.size + 7.5 + playerShip.yv * delta
            );
            batch.addArrays(markerX, markerY, markerL, markerL, markerTris, markerL);
        }
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
        updateList(ships);
        updateList(shots);
        updateList(localEntities);

        Iterator<Entity> shotIt = shots.iterator();
        while (shotIt.hasNext()) {
            Shot shot = (Shot)shotIt.next();
            Iterator<Entity> shipIt = ships.iterator();
            while (shipIt.hasNext()) {
                Ship ship = (Ship)shipIt.next();
                if (ship.team == shot.team || !ship.alive()) continue;
                if (dist(ship.x, ship.y, shot.x, shot.y) < Ship.size) {
                    ship.hit();
                    shot.hit(ship.isShieldActive());
                }
            }
        }
    }
}