package ewewukek.swar;

import java.util.Iterator;

import static ewewukek.swar.Util.*;

public abstract class AI {
    public abstract void update(Ship ship);

    protected Ship findEnemy(Ship self) {
        Ship closest = null;
        float closest_dist = 0;
        Iterator<Entity> it = Game.ships.iterator();
        while (it.hasNext()) {
            Ship other = (Ship)it.next();
            if (other.team == self.team || !other.alive()) continue;
            float d = dist(self.x, self.y, other.x, other.y);
            if (closest == null || d < closest_dist) {
                closest = other;
                closest_dist = d;
            }
        }
        return closest;
    }
}