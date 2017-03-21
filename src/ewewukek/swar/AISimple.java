package ewewukek.swar;

import static ewewukek.swar.Util.*;

public class AISimple extends AI {
    private Ship enemy;

    @Override
    public void update(Ship self) {
        self.inputTurn = 0;
        self.keyThrottle = false;
        self.keyFire = false;
        if (enemy != null && (!enemy.alive() || enemy.team == self.team)) enemy = null;
        if (enemy == null) enemy = findEnemy(self);
        if (enemy == null) return;
        float lineFront = lineFunc(enemy.x, enemy.y, self.x, self.y, self.a + 0.5f * (float)Math.PI);
        float lineSight = lineFunc(enemy.x, enemy.y, self.x, self.y, self.a);
        float d = dist(self.x, self.y, enemy.x, enemy.y);
        if (lineFront > 0) {
            self.inputTurn = -Math.max(-1, Math.min(1, (lineSight / d) / (float)Math.sin(Ship.turnAcceleration)));
        } else {
            self.inputTurn = lineSight < 0 ? 1 : -1;
        }
        self.keyThrottle = d > 200 && lineFront > 0;
        self.keyFire = lineFront > 0;
    }
}