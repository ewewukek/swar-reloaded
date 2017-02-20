package ewewukek.swar;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import org.joml.Vector2f;

import static ewewukek.swar.Constants.*;
import static ewewukek.swar.Util.*;
import static ewewukek.swar.Shader.*;

import ewewukek.swar.Particle;
import ewewukek.swar.particle.Exhaust;

public class Ship {
    public static final float size = 15;
    public static final float line_width = 20;

    public static final int vertexCount = 22;
    public static final int triangleCount = 21;

    private static int vbo_position;
    private static int vbo_indices;

    public Ship() {
    }

    public float cr = 1;
    public float cg = 1;
    public float cb = 1;

    public float x;
    public float y;
    public float a;

    public float xv;
    public float yv;
    public float av;

    public void turn_left(float delta) {
        if (av > -max_angle_velocity) {
            av -= turn_acceleration * delta;
        } else {
            av = -max_angle_velocity;
        }
    }

    public void turn_right(float delta) {
        if (av < max_angle_velocity) {
            av += turn_acceleration * delta;
        } else {
            av = max_angle_velocity;
        }
    }

    public void throttle(float delta) {
        double speed_2 = xv * xv + yv * yv;
        if (speed_2 < max_velocity * max_velocity) {
            xv += (float)Math.sin(a) * acceleration * delta;
            yv += (float)Math.cos(a) * acceleration * delta;
        }
        Particle.add(new Exhaust(
            x - (float)Math.sin(a) * size * 0.5f,
            y - (float)Math.cos(a) * size * 0.5f,
            -(float)Math.sin(a) * max_velocity,
            -(float)Math.cos(a) * max_velocity
        ));
    }

    public void update(float delta) {
        x += xv * delta;
        y += yv * delta;
        a += av * delta;

        if (x < -base_width / 2) {
            x += base_width;
        }
        if (x >= base_width / 2) {
            x -= base_width;
        }
        if (y < -base_height / 2) {
            y += base_height;
        }
        if (y >= base_height / 2) {
            y -= base_height;
        }

        double speed = Math.sqrt(xv * xv + yv * yv);
        if (speed < friction) {
            xv = 0;
            yv = 0;
        } else {
            xv *= (speed - friction * delta) / speed;
            yv *= (speed - friction * delta) / speed;
        }
        if (Math.abs(av) < angle_friction) {
            av = 0;
        } else {
            av -= Math.signum(av) * angle_friction;
        }
    }

    public void draw() {
        glBindBuffer(GL_ARRAY_BUFFER, vbo_position);
        glVertexAttribPointer(ATTRIBUTE_POSITION_LINEDIR, 4, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(ATTRIBUTE_POSITION_LINEDIR);

        glVertexAttrib4f(ATTRIBUTE_COLOR, cr, cg, cb, 1);
        glDisableVertexAttribArray(ATTRIBUTE_COLOR);

        glVertexAttrib1f(ATTRIBUTE_LINE_WIDTH, 0.5f);
        glDisableVertexAttribArray(ATTRIBUTE_LINE_WIDTH);

        glVertexAttrib1f(ATTRIBUTE_LINE_OFFSET, 0);
        glDisableVertexAttribArray(ATTRIBUTE_LINE_OFFSET);

        glVertexAttrib1f(ATTRIBUTE_GLOW_RADIUS, line_width);
        glDisableVertexAttribArray(ATTRIBUTE_GLOW_RADIUS);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_indices);

        shader().setRotation(a);

        for (int i = -1; i != 2; ++i) {
            for (int j = -1; j != 2; ++j) {
                if (x + i * base_width + size + line_width > -base_width / 2
                 && x + i * base_width - size - line_width < base_width / 2
                 && y + j * base_height + size + line_width > -base_height / 2
                 && y + j * base_height - size - line_width < base_height / 2) {
                    draw_at(x + i * base_width, y + j * base_height);
                }
            }
        }
    }

    private void draw_at(float x, float y) {
        shader().setPosition(x, y);
        glDrawElements(GL_TRIANGLES, triangleCount * 3, GL_UNSIGNED_INT, 0);
    }

    public static void init() {
        if (vbo_position != 0) {
            return;
        }
        FloatBuffer fb = BufferUtils.createFloatBuffer(4 * vertexCount);
        IntBuffer ib = BufferUtils.createIntBuffer(3 * triangleCount);

        float sin = (float)Math.sin(Math.PI*0.75);
        float cos = (float)Math.cos(Math.PI*0.75);

        float[] x = new float[4];
        float[] y = new float[4];
        x[0] = 0;
        y[0] = size;
        x[1] = (float)Math.sin(Math.PI * 0.75) * size;
        y[1] = (float)Math.cos(Math.PI * 0.75) * size;
        x[2] = 0;
        y[2] = -0.5f * size;
        x[3] = -x[1];
        y[3] = y[1];

        for (int i = 0; i != 4; ++i) {
            fb.put(x[i]);   fb.put(y[i]);   fb.put(0);  fb.put(0);
        }

        Vector2f n1 = new Vector2f(y[0] - y[1], x[1] - x[0]).normalize().mul(line_width);
        fb.put(x[0] - n1.x);    fb.put(y[0] + n1.y);    fb.put(-n1.x);  fb.put(n1.y);   // 4
        Vector2f t1 = intersect(n1.x, n1.y, 0, line_width);
        fb.put(x[0] - t1.x);    fb.put(y[0] + t1.y);    fb.put(-t1.x);  fb.put(t1.y);   // 5
        ib.put(0);  ib.put(4);  ib.put(5);
        fb.put(x[0] + t1.x);    fb.put(y[0] + t1.y);    fb.put(t1.x);   fb.put(t1.y);   // 6
        ib.put(0);  ib.put(5);  ib.put(6);
        fb.put(x[0] + n1.x);    fb.put(y[0] + n1.y);    fb.put(n1.x);   fb.put(n1.y);   // 7
        ib.put(0);  ib.put(6);  ib.put(7);
        fb.put(x[1] + n1.x);    fb.put(y[1] + n1.y);    fb.put(n1.x);   fb.put(n1.y);   // 8
        ib.put(0);  ib.put(7);  ib.put(8);
        ib.put(8);  ib.put(1);  ib.put(0);
        Vector2f n2 = new Vector2f(y[1] - y[2], x[2] - x[1]).normalize().mul(line_width);
        Vector2f v1 = new Vector2f(n1).add(n2).normalize().mul(line_width);
        Vector2f t2 = intersect(n1.x, n1.y, v1.x, v1.y);
        fb.put(x[1] + t2.x);    fb.put(y[1] + t2.y);    fb.put(t2.x);   fb.put(t2.y);   // 9
        ib.put(1);  ib.put(8);  ib.put(9);
        Vector2f t3 = new Vector2f(v1).sub(t2).mul(2).add(t2);
        fb.put(x[1] + t3.x);    fb.put(y[1] + t3.y);    fb.put(t3.x);   fb.put(t3.y);   // 10
        ib.put(1);  ib.put(9);  ib.put(10);
        fb.put(x[1] + n2.x);    fb.put(y[1] + n2.y);    fb.put(n2.x);   fb.put(n2.y);   // 11
        ib.put(1);  ib.put(10); ib.put(11);
        Vector2f t4 = intersect(n2.x, n2.y, -n2.x, n2.y).normalize().mul(line_width);
        fb.put(x[2] + t4.x);    fb.put(y[2] + t4.y);    fb.put(n2.x);   fb.put(n2.y);   // 12
        fb.put(x[2] + t4.x);    fb.put(y[2] + t4.y);    fb.put(-n2.x);  fb.put(n2.y);   // 13
        ib.put(2);  ib.put(1);  ib.put(12);
        ib.put(1);  ib.put(11); ib.put(12);
        fb.put(x[3] - n2.x);    fb.put(y[3] + n2.y);    fb.put(-n2.x);  fb.put(n2.y);   // 14
        ib.put(3);  ib.put(2);  ib.put(13);
        ib.put(3);  ib.put(13); ib.put(14);
        fb.put(x[3] - t3.x);    fb.put(y[3] + t3.y);    fb.put(-t3.x);  fb.put(t3.y);   // 15
        ib.put(3);  ib.put(14); ib.put(15);
        fb.put(x[3] - t2.x);    fb.put(y[3] + t2.y);    fb.put(-t2.x);  fb.put(t2.y);   // 16
        ib.put(3);  ib.put(15); ib.put(16);
        fb.put(x[3] - n1.x);    fb.put(y[3] + n1.y);    fb.put(-n1.x);  fb.put(n1.y);   // 17
        ib.put(3);  ib.put(16); ib.put(17);
        ib.put(0);  ib.put(3);  ib.put(17);
        ib.put(0);  ib.put(17); ib.put(4);
        float cy = y[1] - v1.y * (x[1] / v1.x);
        float n1mul = (x[0] * n1.x + (y[0] - cy) * n1.y) / (line_width * line_width);
        float n2mul = (x[2] * n2.x + (y[2] - cy) * n2.y) / (line_width * line_width);
        fb.put(0);  fb.put(cy); fb.put(-n1.x * n1mul);  fb.put(-n1.y * n1mul);  // 18
        ib.put(0);  ib.put(1);  ib.put(18);
        fb.put(0);  fb.put(cy); fb.put(-n2.x * n2mul);  fb.put(-n2.y * n2mul);  // 19
        ib.put(1);  ib.put(2);  ib.put(19);
        fb.put(0);  fb.put(cy); fb.put(n2.x * n2mul);   fb.put(-n2.y * n2mul);  // 20
        ib.put(2);  ib.put(3);  ib.put(20);
        fb.put(0);  fb.put(cy); fb.put(n1.x * n1mul);   fb.put(-n1.y * n1mul);  // 21
        ib.put(3);  ib.put(0);  ib.put(21);

        fb.flip();
        ib.flip();

        vbo_position = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_position);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);

        vbo_indices = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_STATIC_DRAW);
    }
}