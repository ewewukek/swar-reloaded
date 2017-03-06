package ewewukek.swar;

import static ewewukek.swar.Util.*;

public class AISimple extends AI {
    public static float turnArc = (float)Math.toRadians(2);

    private Ship enemy;

    @Override
    public void update(Ship self) {
        self.keyLeft = false;
        self.keyRight = false;
        self.keyThrottle = false;
        self.keyFire = false;
        if (enemy != null && !enemy.alive()) enemy = null;
        if (enemy == null) enemy = findEnemy(self);
        if (enemy == null) return;
        float lineFront = lineFunc(enemy.x, enemy.y, self.x, self.y, self.a + 0.5f * (float)Math.PI);
        float lineSight = lineFunc(enemy.x, enemy.y, self.x, self.y, self.a);
        float lineSightL = lineFunc(enemy.x, enemy.y, self.x, self.y, self.a - turnArc);
        float lineSightR = lineFunc(enemy.x, enemy.y, self.x, self.y, self.a + turnArc);
        self.keyRight = lineFront > 0 ? lineSightL < 0 : lineSight < 0;
        self.keyLeft = lineFront > 0 ? lineSightR > 0 : lineSight > 0;
        float d = dist(self.x, self.y, enemy.x, enemy.y);
        self.keyThrottle = d > 300 && lineFront > 0;
        self.keyFire = lineFront > 0;
    }
}