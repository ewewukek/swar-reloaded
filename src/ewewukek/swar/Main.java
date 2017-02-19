package ewewukek.swar;

import org.lwjgl.Sys;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

import org.joml.Vector2f;

import static ewewukek.swar.Util.*;
import static ewewukek.swar.Shader.*;

public class Main {
    final String title = "swar";
    final int base_width = 1024;
    final int base_height = 768;
    int width = 1024;
    int height = 768;

    boolean[] keyPressed = new boolean[1024];

    private void loop() throws LWJGLException {
        Display.setTitle(title);
        Display.setResizable(true);
        Display.setVSyncEnabled(true);
        Display.setFullscreen(false);
        Display.setDisplayMode(new DisplayMode(width, height));
        Display.create(
            new PixelFormat(),
            new ContextAttribs(2, 1)
        );

        System.out.println("GL_VENDOR: "+glGetString(GL_VENDOR));
        System.out.println("GL_RENDERER: "+glGetString(GL_RENDERER));
        System.out.println("GL_VERSION: "+glGetString(GL_VERSION));
        System.out.println("GL_SHADING_LANGUAGE_VERSION: "+glGetString(GL_SHADING_LANGUAGE_VERSION));

        resize();

        shader().use();

        float s = 25.0f;
        float lw = 25.0f;

        float sin = (float)Math.sin(Math.PI*0.75);
        float cos = (float)Math.cos(Math.PI*0.75);

        float[] x = new float[] {
            0,
            sin * s,
            0,
            -sin * s
        };
        float[] y = new float[] {
            s,
            cos * s,
            -0.5f * s,
            cos * s
        };

        int vertex_count = 22;
        int triangle_count = 21;
        FloatBuffer fb = BufferUtils.createFloatBuffer(vertex_count * 4);
        IntBuffer ib = BufferUtils.createIntBuffer(triangle_count * 3);

        for (int i = 0; i != 4; ++i) {
            fb.put(x[i]);   fb.put(y[i]);   fb.put(0);  fb.put(0);
        }

        Vector2f n1 = new Vector2f(y[0] - y[1], x[1] - x[0]).normalize().mul(lw);
        fb.put(x[0] - n1.x);    fb.put(y[0] + n1.y);    fb.put(-n1.x);  fb.put(n1.y);   // 4
        Vector2f t1 = intersect(n1.x, n1.y, 0, lw);
        fb.put(x[0] - t1.x);    fb.put(y[0] + t1.y);    fb.put(-t1.x);  fb.put(t1.y);   // 5
        ib.put(0);  ib.put(4);  ib.put(5);
        fb.put(x[0] + t1.x);    fb.put(y[0] + t1.y);    fb.put(t1.x);   fb.put(t1.y);   // 6
        ib.put(0);  ib.put(5);  ib.put(6);
        fb.put(x[0] + n1.x);    fb.put(y[0] + n1.y);    fb.put(n1.x);   fb.put(n1.y);   // 7
        ib.put(0);  ib.put(6);  ib.put(7);
        fb.put(x[1] + n1.x);    fb.put(y[1] + n1.y);    fb.put(n1.x);   fb.put(n1.y);   // 8
        ib.put(0);  ib.put(7);  ib.put(8);
        ib.put(8);  ib.put(1);  ib.put(0);
        Vector2f n2 = new Vector2f(y[1] - y[2], x[2] - x[1]).normalize().mul(lw);
        Vector2f v1 = new Vector2f(n1).add(n2).normalize().mul(lw);
        Vector2f t2 = intersect(n1.x, n1.y, v1.x, v1.y);
        fb.put(x[1] + t2.x);    fb.put(y[1] + t2.y);    fb.put(t2.x);   fb.put(t2.y);   // 9
        ib.put(1);  ib.put(8);  ib.put(9);
        Vector2f t3 = new Vector2f(v1).sub(t2).mul(2).add(t2);
        fb.put(x[1] + t3.x);    fb.put(y[1] + t3.y);    fb.put(t3.x);   fb.put(t3.y);   // 10
        ib.put(1);  ib.put(9);  ib.put(10);
        fb.put(x[1] + n2.x);    fb.put(y[1] + n2.y);    fb.put(n2.x);   fb.put(n2.y);   // 11
        ib.put(1);  ib.put(10); ib.put(11);
        Vector2f t4 = intersect(n2.x, n2.y, -n2.x, n2.y).normalize().mul(lw);
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
        float n1mul = (x[0] * n1.x + (y[0] - cy) * n1.y) / (lw * lw);
        float n2mul = (x[2] * n2.x + (y[2] - cy) * n2.y) / (lw * lw);
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

        int vbo_position = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_position);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);

        int vbo_indices = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_STATIC_DRAW);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);

        float a = 0;

        while (!Display.isCloseRequested()) {
            if (Display.wasResized()) resize();

            glClear(GL_COLOR_BUFFER_BIT);

            shader().setScale(2.0f / base_width, 2.0f / base_height);

            a += 0.005f;

            shader().setPosition((float)Math.sin(Math.PI * a) * 192, (float)Math.cos(Math.PI * a) * 192);
            shader().setRotation(-a * 3f * (float)Math.PI);
            // shader().setRotation((float)Math.PI * 0.838f);

            glBindBuffer(GL_ARRAY_BUFFER, vbo_position);
            glVertexAttribPointer(ATTRIBUTE_POSITION_LINEDIR, 4, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(ATTRIBUTE_POSITION_LINEDIR);
            glVertexAttrib4f(ATTRIBUTE_COLOR, 1.0f, 0.4f, 0.4f, 1);
            // glVertexAttrib4f(ATTRIBUTE_COLOR, 1.0f, 0.0f, 0.0f, 1);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_indices);
            glDrawElements(GL_TRIANGLES, triangle_count * 3, GL_UNSIGNED_INT, 0);

            Display.update();
            Display.sync(60);

            while(Keyboard.next()) {
                int code = Keyboard.getEventKey();
                if(Keyboard.getEventKeyState()) {
                    keyPressed[code] = true;

                    switch(code) {
                        case Keyboard.KEY_ESCAPE:
                            System.exit(0);
                        break;
                    }
                } else {
                    keyPressed[code] = false;
                }
            }
        }
        Display.destroy();
        System.exit(0);
    }

    protected void resize() {
        float scale_x = (float)Display.getWidth() / base_width;
        float scale_y = (float)Display.getHeight() / base_height;
        float scale = scale_x < scale_y ? scale_x : scale_y;
        width = (int)(base_width * scale);
        height = (int)(base_height * scale);
        int offset_x = (Display.getWidth() - width) / 2;
        int offset_y = (Display.getHeight() - height) / 2;
        glViewport(offset_x, offset_y, width, height);
    }

    public static void main(String[] args) {
        System.out.println("LWJGL "+Sys.getVersion());
        try {
            new Main().loop();
        } catch(LWJGLException e) {
            e.printStackTrace();
        }
    }
}