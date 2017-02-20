package ewewukek.swar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import static ewewukek.swar.Shader.*;

public abstract class Particle {
    private static final List<Particle> particles = new ArrayList<Particle>();
    private static int vertexCount = 0;
    private static int triangleCount = 0;

    private static int vbo_vertices;
    private static int vbo_indices;

    public static void add(Particle p) {
        if (vbo_vertices == 0) {
            init();
        }
        vertexCount += p.getVertexCount();
        triangleCount += p.getTriangleCount();
        particles.add(p);
    }

    public static void updateAll(float delta) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            if (!p.update(delta)) {
                vertexCount -= p.getVertexCount();
                triangleCount -= p.getTriangleCount();
                it.remove();
            }
        }
    }

    public static void drawAll() {
        if (triangleCount == 0) return;
        FloatBuffer fb = BufferUtils.createFloatBuffer(11 * vertexCount);
        IntBuffer ib = BufferUtils.createIntBuffer(3 * triangleCount);
        Iterator<Particle> it = particles.iterator();
        int i = 0;
        while (it.hasNext()) {
            Particle p = it.next();
            p.draw(fb, i, ib);
            i += p.getVertexCount();
        }
        fb.flip();
        ib.flip();

        shader().setPosition(0, 0);
        shader().setRotation(0);

        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertices);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(ATTRIBUTE_POSITION_LINEDIR, 4, GL_FLOAT, false, 11 * 4, 0);
        glEnableVertexAttribArray(ATTRIBUTE_POSITION_LINEDIR);

        glVertexAttribPointer(ATTRIBUTE_COLOR, 4, GL_FLOAT, false, 11 * 4, 4 * 4);
        glEnableVertexAttribArray(ATTRIBUTE_COLOR);

        glVertexAttribPointer(ATTRIBUTE_LINE_WIDTH, 4, GL_FLOAT, false, 11 * 4, 8 * 4);
        glEnableVertexAttribArray(ATTRIBUTE_LINE_WIDTH);

        glVertexAttribPointer(ATTRIBUTE_LINE_OFFSET, 4, GL_FLOAT, false, 11 * 4, 9 * 4);
        glEnableVertexAttribArray(ATTRIBUTE_LINE_OFFSET);

        glVertexAttribPointer(ATTRIBUTE_GLOW_RADIUS, 4, GL_FLOAT, false, 11 * 4, 10 * 4);
        glEnableVertexAttribArray(ATTRIBUTE_GLOW_RADIUS);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_DYNAMIC_DRAW);

        glDrawElements(GL_TRIANGLES, 3 * triangleCount, GL_UNSIGNED_INT, 0);
    }

    private static void init() {
        vbo_vertices = glGenBuffers();
        vbo_indices = glGenBuffers();
    }

    protected abstract int getVertexCount();
    protected abstract int getTriangleCount();
    protected abstract boolean update(float delta);
    protected abstract void draw(FloatBuffer fb, int i, IntBuffer ib);
}
