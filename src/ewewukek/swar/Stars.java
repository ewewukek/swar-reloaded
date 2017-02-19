package ewewukek.swar;

import java.util.Random;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import static ewewukek.swar.Shader.*;

public class Stars {
    public static final float size = 5;

    public static void draw() {
        shader().setPosition(0, 0);
        shader().setRotation(0);

        fb.clear();
        for (int i = 0; i != starCount; ++i) {
            float c = luminosity[i] * (rnd.nextFloat()*0.35f + 0.65f);
            for (int v = 0; v != 5; ++v) {
                fb.put(c);
                fb.put(c);
                fb.put(c);
                fb.put(1);
            }
        }
        fb.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo_color);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(ATTRIBUTE_COLOR, 4, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(ATTRIBUTE_COLOR);

        glBindBuffer(GL_ARRAY_BUFFER, vbo_position);
        glVertexAttribPointer(ATTRIBUTE_POSITION_LINEDIR, 4, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(ATTRIBUTE_POSITION_LINEDIR);

        glVertexAttrib1f(ATTRIBUTE_LINE_WIDTH, 0.25f);
        glDisableVertexAttribArray(ATTRIBUTE_LINE_WIDTH);

        glVertexAttrib1f(ATTRIBUTE_LINE_OFFSET, 0);
        glDisableVertexAttribArray(ATTRIBUTE_LINE_OFFSET);

        glVertexAttrib1f(ATTRIBUTE_GLOW_RADIUS, size);
        glDisableVertexAttribArray(ATTRIBUTE_GLOW_RADIUS);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_indices);
        glDrawElements(GL_TRIANGLES, starCount * 4 * 3, GL_UNSIGNED_INT, 0);
    }

    private static Random rnd = new Random();

    private static int starCount;

    private static FloatBuffer fb;
    private static float[] luminosity;

    private static int vbo_position;
    private static int vbo_color;
    private static int vbo_indices;

    public static void init(int width, int height) {
        if (vbo_position != 0) {
            return;
        }
        starCount = 512;
        fb = BufferUtils.createFloatBuffer(4 * 5 * starCount);
        IntBuffer ib = BufferUtils.createIntBuffer(3 * 4 * starCount);
        luminosity = new float[starCount];

        fb.clear();
        for (int i = 0; i != starCount; ++i) {
            luminosity[i] = 0.125f + rnd.nextFloat() * 0.875f;
            float x = (rnd.nextFloat() - 0.5f) * width;
            float y = (rnd.nextFloat() - 0.5f) * height;
            if (x < -512 || x > 512) System.out.println("x = "+x);
            if (y < -384 || y > 384) System.out.println("y = "+y);
            fb.put(x);          fb.put(y);          fb.put(0);      fb.put(0);
            fb.put(x + size);   fb.put(y + size);   fb.put(size);   fb.put(size);
            fb.put(x + size);   fb.put(y - size);   fb.put(size);   fb.put(-size);
            fb.put(x - size);   fb.put(y - size);   fb.put(-size);  fb.put(-size);
            fb.put(x - size);   fb.put(y + size);   fb.put(-size);  fb.put(size);
            int i0 = i * 5;
            ib.put(i0); ib.put(i0 + 1); ib.put(i0 + 2);
            ib.put(i0); ib.put(i0 + 2); ib.put(i0 + 3);
            ib.put(i0); ib.put(i0 + 3); ib.put(i0 + 4);
            ib.put(i0); ib.put(i0 + 4); ib.put(i0 + 1);
        }

        fb.flip();
        ib.flip();

        vbo_position = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_position);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);

        vbo_color = glGenBuffers();

        vbo_indices = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_STATIC_DRAW);
    }
}