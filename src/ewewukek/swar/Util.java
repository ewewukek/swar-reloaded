package ewewukek.swar;

import java.util.Random;

import org.joml.Vector2f;

public class Util {
    private static final Random random = new Random();

    public static float rand() { return random.nextFloat(); }

    public static Vector2f intersect(float l1x, float l1y, float l2x, float l2y) {
        float d = l1x*l2y - l2x*l1y;
        float a1 = l1x*l1x + l1y*l1y;
        float a2 = l2x*l2x + l2y*l2y;
        return new Vector2f( (a1*l2y - a2*l1y) / d, (l1x*a1 - l2x*a2) / d );
    }
}