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

public class Main {
    final String title = "swar";
    final int base_width = 1024;
    final int base_height = 768;
    int width = 1024;
    int height = 768;

    final int ATTRIBUTE_POSITION = 0;
    final int ATTRIBUTE_LINEDIR = 1;

    boolean[] keyPressed = new boolean[1024];

    private void intersect(Vector2f v, Vector2f l1, Vector2f l2) {
        float d = l1.x * l2.y - l2.x * l1.y;
        v.set( (l2.y - l1.y) / d, (l1.x - l2.x) / d );
    }

    private void intersect(Vector2f v, float l1x, float l1y, float l2x, float l2y) {
        float d = l1x*l2y - l2x*l1y;
        float a1 = l1x*l1x + l1y*l1y;
        float a2 = l2x*l2x + l2y*l2y;
        v.set( (a1*l2y - a2*l1y) / d, (l1x*a1 - l2x*a2) / d );
    }

    private static final Vector2f z = new Vector2f(0, 0);

    private int put_segment(FloatBuffer fb, float lw, Vector2f v0, Vector2f v1, Vector2f v2, Vector2f v3) {
        Vector2f t1 = new Vector2f();
        Vector2f t2 = new Vector2f();
        Vector2f t3 = new Vector2f();
        Vector2f n12 = new Vector2f(v1.y - v2.y, v2.x - v1.x).normalize();
        Vector2f n1 = new Vector2f();
        Vector2f n2 = new Vector2f();
        Vector2f n3 = new Vector2f();
        int count = 0;
        boolean t2t3 = false;
        boolean v1n12 = false;
        boolean v2n12 = false;
        boolean t1n12 = false;
        boolean t2n12 = false;
        if (v1.equals(v0)) {
            t1.set(-n12.y, n12.x).add(n12);
            v1n12 = true;
        } else {
            n1.set(v0.y - v1.y, v1.x - v0.x).normalize();
            if (v0.dot(n12) - v1.dot(n12) < 0) {
                v1n12 = true;
                if (n1.dot(n12) < 0) {
                    n1.add(n12).normalize();
                }
            } else {
                t1n12 = true;
            }
            intersect(t1, n1, n12);
        }
        if (v2.equals(v3)) {
            t2.set(n12.y, -n12.x).add(n12);
            t3.set(n12.y, -n12.x).sub(n12);
            t2t3 = true;
            v2n12 = true;
        } else {
            n2.set(v2.y - v3.y, v3.x - v2.x).normalize();
            n3.set(n2);
            if (v3.dot(n12) - v2.dot(n12) < 0) {
                v2n12 = true;
                if (n12.dot(n3) < 0) {
                    n2.add(n12).normalize();
                    intersect(t3, n2, n3);
                    t2t3 = true;
                }
            } else {
                t2n12 = true;
            }
            intersect(t2, n2, n12);
        }
        if (t2t3) {
            fb.put(v2.x);
            fb.put(v2.y);
            fb.put(0);
            fb.put(0);
            fb.put(v2.x + lw * t2.x);
            fb.put(v2.y + lw * t2.y);
            fb.put(t2.x);
            fb.put(t2.y);
            fb.put(v2.x + lw * t3.x);
            fb.put(v2.y + lw * t3.y);
            fb.put(t3.x);
            fb.put(t3.y);
            count += 3;
        }
        if (v1n12) {
            fb.put(v1.x);
            fb.put(v1.y);
            fb.put(0);
            fb.put(0);
            fb.put(v1.x + lw * t1.x);
            fb.put(v1.y + lw * t1.y);
            fb.put(t1.x);
            fb.put(t1.y);
            fb.put(v1.x + lw * n12.x);
            fb.put(v1.y + lw * n12.y);
            fb.put(n12.x);
            fb.put(n12.y);
            count += 3;
            t1.set(n12);
        }
        if (v2n12) {
            fb.put(v2.x);
            fb.put(v2.y);
            fb.put(0);
            fb.put(0);
            fb.put(v2.x + lw * n12.x);
            fb.put(v2.y + lw * n12.y);
            fb.put(n12.x);
            fb.put(n12.y);
            fb.put(v2.x + lw * t2.x);
            fb.put(v2.y + lw * t2.y);
            fb.put(t2.x);
            fb.put(t2.y);
            count += 3;
            t2.set(n12);
        }
        fb.put(v1.x);
        fb.put(v1.y);
        fb.put(0);
        fb.put(0);
        fb.put(v1.x + lw * t1.x);
        fb.put(v1.y + lw * t1.y);
        fb.put(t1n12 ? n12.x : t1.x);
        fb.put(t1n12 ? n12.y : t1.y);
        fb.put(v2.x + lw * t2.x);
        fb.put(v2.y + lw * t2.y);
        fb.put(t2n12 ? n12.x : t2.x);
        fb.put(t2n12 ? n12.y : t2.y);

        fb.put(v2.x + lw * t2.x);
        fb.put(v2.y + lw * t2.y);
        fb.put(t2n12 ? n12.x : t2.x);
        fb.put(t2n12 ? n12.y : t2.y);
        fb.put(v2.x);
        fb.put(v2.y);
        fb.put(0);
        fb.put(0);
        fb.put(v1.x);
        fb.put(v1.y);
        fb.put(0);
        fb.put(0);

        count += 6;

        // count += put_triangle(fb, lw, v1, z, v1, t1, v2, t2);
        // count += put_triangle(fb, lw, v2, t2, v2, z, v1, z);

        // fb.put(v1.x);
        // fb.put(v1.y);
        // fb.put(0);
        // fb.put(0);
        // fb.put(v2.x);
        // fb.put(v2.y);
        // fb.put(0);
        // fb.put(0);
        // count += 2;
        // if (v1n12) {
            // fb.put(v1.x);
            // fb.put(v1.y);
            // fb.put(0);
            // fb.put(0);
            // fb.put(v1.x + lw * n12.x);
            // fb.put(v1.y + lw * n12.y);
            // fb.put(1);
            // fb.put(0);
            // count += 2;
        // }
        // if (v2n12) {
            // fb.put(v2.x);
            // fb.put(v2.y);
            // fb.put(0);
            // fb.put(0);
            // fb.put(v2.x + lw * n12.x);
            // fb.put(v2.y + lw * n12.y);
            // fb.put(1);
            // fb.put(0);
            // count += 2;
        // }
        // if (t2t3) {
            // fb.put(v2.x + lw * t2.x);
            // fb.put(v2.y + lw * t2.y);
            // fb.put(1);
            // fb.put(0);
            // fb.put(v2.x + lw * t3.x);
            // fb.put(v2.y + lw * t3.y);
            // fb.put(1);
            // fb.put(0);
            // fb.put(v2.x);
            // fb.put(v2.y);
            // fb.put(0);
            // fb.put(0);
            // fb.put(v2.x + lw * t3.x);
            // fb.put(v2.y + lw * t3.y);
            // fb.put(1);
            // fb.put(0);
            // count += 4;
        // }
        // fb.put(v1.x + lw * t1.x);
        // fb.put(v1.y + lw * t1.y);
        // fb.put(1);
        // fb.put(0);
        // fb.put(v2.x + lw * t2.x);
        // fb.put(v2.y + lw * t2.y);
        // fb.put(1);
        // fb.put(0);
        // fb.put(v2.x);
        // fb.put(v2.y);
        // fb.put(0);
        // fb.put(0);
        // fb.put(v2.x + lw * t2.x);
        // fb.put(v2.y + lw * t2.y);
        // fb.put(1);
        // fb.put(0);
        // count += 4;

        return count;
    }

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

        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
            "#version 120\n"+
            "attribute vec4 position;\n"+
            "varying vec2 l;\n"+
            "uniform vec2 camera_position;\n"+
            "uniform vec2 camera_scale;\n"+
            "void main() {\n"+
            "l = position.zw;\n"+
            "gl_Position = vec4((position.xy - camera_position) * camera_scale, 0.0, 1.0);\n"+
            "}"
        );
        glCompileShader(vs);

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
            "#version 120\n"+
            "varying vec2 l;\n"+
            "void main() {\n"+
            "float a = min(sqrt(l.x * l.x + l.y * l.y), 1.0);\n"+
            "a = min(1.0 / (1.0 + a * 15.0), 1.0 - a);\n"+
            // "a = 1.0 - a;\n"+
            "gl_FragColor = vec4(1.0, 1.0, 1.0, a);\n"+
            "}"
        );
        glCompileShader(fs);

        int shader = glCreateProgram();
        glAttachShader(shader, vs);
        glAttachShader(shader, fs);
        glBindAttribLocation(shader, ATTRIBUTE_POSITION, "position");
        glLinkProgram(shader);
        glUseProgram(shader);

        int vs2 = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs2,
            "#version 120\n"+
            "attribute vec4 position;\n"+
            "varying vec2 l;\n"+
            "uniform vec2 camera_position;\n"+
            "uniform vec2 camera_scale;\n"+
            "void main() {\n"+
            "l = position.zw;\n"+
            "gl_Position = vec4((position.xy - camera_position) * camera_scale, 0.0, 1.0);\n"+
            "}"
        );
        glCompileShader(vs2);

        int fs2 = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs2,
            "#version 120\n"+
            "varying vec2 l;\n"+
            "void main() {\n"+
            "float a = clamp(sqrt(l.x * l.x + l.y * l.y), 0.05, 1.0);\n"+
            // "float a = 0.5 * (1.0 - a);\n"+
            "float s = 1.0 - a * a * a * a;\n"+
            // "float s = 1.0;\n"+
            "a = s * 0.5 / (0.25 + 10.0 * a);\n"+
            "gl_FragColor = vec4(1.0, 1.0, 1.0, a);\n"+
            "}"
        );
        glCompileShader(fs2);

        int shader2 = glCreateProgram();
        glAttachShader(shader2, vs2);
        glAttachShader(shader2, fs2);
        glBindAttribLocation(shader2, ATTRIBUTE_POSITION, "position");
        glLinkProgram(shader2);
        glUseProgram(shader2);

        int uniform_camera_scale = glGetUniformLocation(shader, "camera_scale");
        int uniform_camera_position = glGetUniformLocation(shader, "camera_position");

        float s = 15.0f;
        float lw = 20.0f;

        float sin = (float)Math.sin(Math.PI*0.75);
        float cos = (float)Math.cos(Math.PI*0.75);

        Vector2f[] v = new Vector2f[] {
            new Vector2f(0,         s),
            new Vector2f(sin * s,   cos * s),
            new Vector2f(0,         -s * 0.5f),
            new Vector2f(-sin * s,  cos * s)
        };

        FloatBuffer fb = BufferUtils.createFloatBuffer(1000);

        int count = 0;

        for (int i = 0; i != v.length; ++i) {
            count += put_segment(fb, lw,
                v[(i+v.length-1)%v.length],
                v[i],
                v[(i+1)%v.length],
                v[(i+2)%v.length]
            );
            count += put_segment(fb, lw * 0.2f,
                v[(i+2)%v.length],
                v[(i+1)%v.length],
                v[i],
                v[(i+v.length-1)%v.length]
            );
        }

        // Vector2f[] vv = new Vector2f[] {
            // new Vector2f(-5, 10),
            // new Vector2f(-15, 0),
            // new Vector2f(-5, 0),
            // new Vector2f(0, -5),
            // new Vector2f(5, 0),
            // new Vector2f(5, 10),
            // new Vector2f(15, 10),
            // new Vector2f(10, 5)
        // };

        // for (int i = 0; i < vv.length - 1; ++i) {
            // count += put_segment(fb, lw,
                // vv[i > 0 ? i - 1 : 0],
                // vv[i],
                // vv[i + 1],
                // vv[i < vv.length - 2 ? i + 2 : vv.length - 1]
            // );
            // count += put_segment(fb, lw,
                // vv[i < vv.length - 2 ? i + 2 : vv.length - 1],
                // vv[i + 1],
                // vv[i],
                // vv[i > 0 ? i - 1 : 0]
            // );
        // }

        fb.flip();

        int vbo_position = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_position);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(ATTRIBUTE_POSITION, 4, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(ATTRIBUTE_POSITION);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);

        while (!Display.isCloseRequested()) {
            if (Display.wasResized()) resize();

            glClear(GL_COLOR_BUFFER_BIT);

            glUseProgram(shader);
            glEnableVertexAttribArray(ATTRIBUTE_POSITION);
            glUniform2f(uniform_camera_scale, 2.0f/base_width, 2.0f/base_height);
            glUniform2f(uniform_camera_position, 35, 0);
            glBindBuffer(GL_ARRAY_BUFFER, vbo_position);
            glVertexAttribPointer(ATTRIBUTE_POSITION, 4, GL_FLOAT, false, 0, 0);
            if (count > 0) glDrawArrays(GL_TRIANGLES, 0, count);

            glUseProgram(shader2);
            glEnableVertexAttribArray(ATTRIBUTE_POSITION);
            glUniform2f(uniform_camera_scale, 2.0f/base_width, 2.0f/base_height);
            glUniform2f(uniform_camera_position, -35, 0);
            glBindBuffer(GL_ARRAY_BUFFER, vbo_position);
            glVertexAttribPointer(ATTRIBUTE_POSITION, 4, GL_FLOAT, false, 0, 0);
            if (count > 0) glDrawArrays(GL_TRIANGLES, 0, count);

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