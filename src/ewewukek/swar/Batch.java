package ewewukek.swar;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Batch {
    public static final float lineWidth = 0.5f;
    public static final float falloffMultiplier = 10;

    private static final int ATTRIBUTE_POSITION_LINEDIR = 0;
    private static final int ATTRIBUTE_COLOR = 1;

    private static int fs;
    private static int vs;
    private static int program;

    private static int uLineOffset;
    private static int uGlowRadius;

    private static final int vertexCapacity = 10240;
    private static final int triangleCapacity = 10240;

    private FloatBuffer fb = BufferUtils.createFloatBuffer(vertexCapacity * 12);
    private int vertexCount;
    private int vboVertices;

    private IntBuffer ib = BufferUtils.createIntBuffer(triangleCapacity * 3);
    private int triangleCount;
    private int vboIndices;

    private boolean uploaded;

    private float lineOffset;
    private float glowRadius;

    private float colorR;
    private float colorG;
    private float colorB;
    private float colorA;

    private float xOrigin;
    private float yOrigin;

    private float xScale;
    private float yScale;

    private float rotSin;
    private float rotCos;

    private float glowShiftX;
    private float glowShiftY;

    public Batch(int width, int height) {
        init();
        xScale = 2.0f / width;
        yScale = 2.0f / height;
        setDefaults();
    }

    public void setDefaults() {
        xOrigin = 0;
        yOrigin = 0;
        rotSin = 0;
        rotCos = 1;
        colorR = 1;
        colorG = 1;
        colorB = 1;
        colorA = 1;
        lineOffset = 0;
        glowRadius = 10;
        glowShiftX = 0;
        glowShiftY = 0;
    }

    public void setOrigin(float x, float y) {
        xOrigin = x;
        yOrigin = y;
    }

    public void setRotation(float rad) {
        rotSin = (float)Math.sin(rad);
        rotCos = (float)Math.cos(rad);
    }

    public void setColor(float r, float g, float b, float a) {
        colorR = r;
        colorG = g;
        colorB = b;
        colorA = a;
    }

    public void setLineOffset(float lo) {
        lineOffset = lo;
    }

    public void setGlowRadius(float g) {
        glowRadius = g;
    }

    public void setGlowShift(float x, float y) {
        glowShiftX = x;
        glowShiftY = y;
    }

    public void clear() {
        fb.clear();
        vertexCount = 0;
        ib.clear();
        triangleCount = 0;
        uploaded = false;
    }

    public void addPoint(float x, float y) {
        final float[] ax = new float[] { 0, -1,  1,  1, -1 };
        final float[] ay = new float[] { 0,  1,  1, -1, -1 };
        final float[] gs = new float[] { 0,  1,  1,  1,  1 };
        final int[] tris = new int[] {
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 1
        };
        if (vertexCount + 5 >= vertexCapacity || triangleCount + tris.length / 3 >= triangleCapacity) {
            draw();
            clear();
        }
        float r = lineOffset + lineWidth + glowRadius;
        for (int i = 0; i != 5; ++i) {
            putPosition(ax[i] * r, ay[i] * r, x, y, gs[i]);
            putLinedir(ax[i] * r, ay[i] * r);
            putProperties();
        }
        int i0 = vertexCount;
        vertexCount += 5;
        for (int i = 0; i != tris.length; ++i) {
            ib.put(i0 + tris[i]);
        }
        triangleCount += tris.length / 3;
    }

    public void addLine(float x1, float y1, float x2, float y2) {
        final float[] ae = new float[] { 0,  0, -1, -1,  0 };
        final float[] an = new float[] { 0, -1, -1,  1,  1 };
        final float[] gs = new float[] { 0,  1,  1,  1,  1 };
        final int[] tris = new int[] {
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 6,
            0, 6, 5,
            5, 6, 7,
            5, 7, 8,
            5, 8, 9,
            5, 9, 1,
            5, 1, 0
        };
        if (vertexCount + 10 >= vertexCapacity || triangleCount + tris.length / 3 >= triangleCapacity) {
            draw();
            clear();
        }
        float ex = x2 - x1;
        float ey = y2 - y1;
        float len = (float)Math.sqrt(ex*ex + ey*ey);
        ex /= len;
        ey /= len;
        float r = lineOffset + lineWidth + glowRadius;
        for (int i = 0; i != 5; ++i) {
            putPosition(
                (ex * ae[i] - ey * an[i]) * r,
                (ey * ae[i] + ex * an[i]) * r,
                x1, y1, gs[i]);
            putLinedir(
                (ex * ae[i] - ey * an[i]) * r,
                (ey * ae[i] + ex * an[i]) * r);
            putProperties();
        }
        for (int i = 0; i != 5; ++i) {
            putPosition(
                (-ex * ae[i] + ey * an[i]) * r,
                (-ey * ae[i] - ex * an[i]) * r,
                x2, y2, gs[i]);
            putLinedir(
                (-ex * ae[i] + ey * an[i]) * r,
                (-ey * ae[i] - ex * an[i]) * r);
            putProperties();
        }
        int i0 = vertexCount;
        vertexCount += 10;
        for (int i = 0; i != tris.length; ++i) {
            ib.put(i0 + tris[i]);
        }
        triangleCount += tris.length / 3;
    }

    public void addArrays(float[] x, float[] y, float[] lx, float[] ly, float[] gs, int[] tris) {
        if (vertexCount + x.length >= vertexCapacity || triangleCount + tris.length / 3 >= triangleCapacity) {
            draw();
            clear();
        }
        for (int i = 0; i != x.length; ++i) {
            putPosition(x[i], y[i], 0, 0, gs != null ? gs[i] : 0);
            putLinedir(lx[i], ly[i]);
            putProperties();
        }
        int i0 = vertexCount;
        vertexCount += x.length;
        for (int i = 0; i != tris.length; ++i) {
            ib.put(i0 + tris[i]);
        }
        triangleCount += tris.length / 3;
    }

    public void draw() {
        if (triangleCount == 0) return;
        upload();

        glUseProgram(program);

        glUniform1f(uLineOffset, lineOffset);
        glUniform1f(uGlowRadius, glowRadius);

        glEnableVertexAttribArray(ATTRIBUTE_POSITION_LINEDIR);
        glEnableVertexAttribArray(ATTRIBUTE_COLOR);

        glVertexAttribPointer(ATTRIBUTE_POSITION_LINEDIR, 4, GL_FLOAT, false, 8 * 4, 0);
        glVertexAttribPointer(ATTRIBUTE_COLOR, 4, GL_FLOAT, false, 8 * 4, 4 * 4);

        glDrawElements(GL_TRIANGLES, 3 * triangleCount, GL_UNSIGNED_INT, 0);
    }

    private void putPosition(float x, float y, float ox, float oy, float gs) {
        fb.put( ( rotCos * (x + ox) + rotSin * (y + oy) + xOrigin + glowShiftX * gs) * xScale );
        fb.put( (-rotSin * (x + ox) + rotCos * (y + oy) + yOrigin + glowShiftY * gs) * yScale );
    }

    private void putLinedir(float x, float y) {
        fb.put( rotCos * x + rotSin * y );
        fb.put(-rotSin * x + rotCos * y );
    }

    private void putProperties() {
        fb.put(colorR);
        fb.put(colorG);
        fb.put(colorB);
        fb.put(colorA);
    }

    private void upload() {
        if (uploaded) return;

        if (vboVertices == 0) vboVertices = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        fb.flip();
        glBufferData(GL_ARRAY_BUFFER, fb, GL_DYNAMIC_DRAW);

        if (vboIndices == 0) vboIndices = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndices);
        ib.flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_DYNAMIC_DRAW);

        uploaded = true;
    }

    private static boolean _init;
    private static void init() {
        if (_init) return;
        _init = true;

        vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
            "#version 120\n"+
            "attribute vec4 position_linedir;\n"+
            "attribute vec4 color;\n"+
            "varying vec2 l;\n"+
            "varying vec4 c;\n"+
            "void main() {\n"+
            "l = position_linedir.zw;\n"+
            "c = color;\n"+
            "gl_Position = vec4(position_linedir.xy, 0.0, 1.0);\n"+
            "}"
        );
        glCompileShader(vs);
        assertShaderCompileStatus(vs, "vertex");

        fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
            "#version 120\n"+
            "varying vec2 l;\n"+
            "varying vec4 c;\n"+
            "uniform float lo;\n"+
            "uniform float gr;\n"+
            "void main() {\n"+
            "const float lw = "+String.format("%.1f", lineWidth)+";\n"+
            "const float fm = "+String.format("%.1f", falloffMultiplier)+";\n"+
            "float d = abs(sqrt(l.x * l.x + l.y * l.y) - lo);\n"+
            "float x = (d - lw) / gr;\n"+
            "float t = sign(1.0 + sign(x));\n"+
            "float v = (1.0 - t) + t * (1.0 - x) / (1.0 + fm * x);\n"+
            "gl_FragColor = vec4(c.rgb * v, c.a);\n"+
            "}"
        );
        glCompileShader(fs);
        assertShaderCompileStatus(fs, "fragment");

        program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);

        glBindAttribLocation(program, ATTRIBUTE_POSITION_LINEDIR, "position_linedir");
        glBindAttribLocation(program, ATTRIBUTE_COLOR, "color");

        glLinkProgram(program);
        assertProgramLinkStatus(program);

        uLineOffset = glGetUniformLocation(program, "lo");
        uGlowRadius = glGetUniformLocation(program, "gr");
    }

    private static void assertShaderCompileStatus(int shader, String name) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println(name+": "+glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
            System.exit(1);
        }
    }

    private static void assertProgramLinkStatus(int program) {
        if (glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE) {
            System.err.println("link: "+glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH)));
            System.exit(1);
        }
    }
}