package ewewukek.swar;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Batch {
    public static final float lineWidth = 0.5f;
    public static final float falloffMultiplier = 10;

    private static final int ATTRIBUTE_POSITION_LINEDIR = 0;
    private static final int ATTRIBUTE_COLOR = 1;

    private static final float POSITION_SCALE = 2;
    private static final float LINEDIR_SCALE = 64;

    private static int fs;
    private static int vs;
    private static int program;

    private static int uLineOffset;
    private static int uGlowRadius;

    private static final int vertexCapacity = 10240;
    private static final int triangleCapacity = 10240;

    private ByteBuffer vb = BufferUtils.createByteBuffer(vertexCapacity * 12);
    private short vertexCount;
    private int vboVertices;

    private ShortBuffer ib = BufferUtils.createShortBuffer(triangleCapacity * 3);
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

    private float rotSin;
    private float rotCos;

    private float glowShiftX;
    private float glowShiftY;

    public Batch() {
        init();
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
        vb.clear();
        vertexCount = 0;
        ib.clear();
        triangleCount = 0;
        uploaded = false;
    }

    public void addPoint(float x, float y) {
        final float[] ax = new float[] { 0, -1,  1,  1, -1 };
        final float[] ay = new float[] { 0,  1,  1, -1, -1 };
        final float[] gs = new float[] { 0,  1,  1,  1,  1 };
        final short[] tris = new short[] {
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
            putPosition(ax[i] * r + x, ay[i] * r + y, gs[i]);
            putLinedir(ax[i] * r, ay[i] * r);
            putProperties();
        }
        short i0 = vertexCount;
        vertexCount += 5;
        for (int i = 0; i != tris.length; ++i) {
            ib.put((short)(i0 + tris[i]));
        }
        triangleCount += tris.length / 3;
    }

    public void addLine(float x1, float y1, float x2, float y2) {
        final float[] ae = new float[] { 0,  0, -1, -1,  0 };
        final float[] an = new float[] { 0, -1, -1,  1,  1 };
        final float[] gs = new float[] { 0,  1,  1,  1,  1 };
        final short[] tris = new short[] {
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
                (ex * ae[i] - ey * an[i]) * r + x1,
                (ey * ae[i] + ex * an[i]) * r + y1,
                gs[i]);
            putLinedir(
                (ex * ae[i] - ey * an[i]) * r,
                (ey * ae[i] + ex * an[i]) * r);
            putProperties();
        }
        for (int i = 0; i != 5; ++i) {
            putPosition(
                (-ex * ae[i] + ey * an[i]) * r + x2,
                (-ey * ae[i] - ex * an[i]) * r + y2,
                gs[i]);
            putLinedir(
                (-ex * ae[i] + ey * an[i]) * r,
                (-ey * ae[i] - ex * an[i]) * r);
            putProperties();
        }
        short i0 = vertexCount;
        vertexCount += 10;
        for (int i = 0; i != tris.length; ++i) {
            ib.put((short)(i0 + tris[i]));
        }
        triangleCount += tris.length / 3;
    }

    public void addArrays(float[] x, float[] y, float[] lx, float[] ly, float[] gs, short[] tris) {
        if (vertexCount + x.length >= vertexCapacity || triangleCount + tris.length / 3 >= triangleCapacity) {
            draw();
            clear();
        }
        for (int i = 0; i != x.length; ++i) {
            putPosition(x[i], y[i], gs != null ? gs[i] : 0);
            putLinedir(lx[i], ly[i]);
            putProperties();
        }
        short i0 = vertexCount;
        vertexCount += x.length;
        for (int i = 0; i != tris.length; ++i) {
            ib.put((short)(i0 + tris[i]));
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

        glVertexAttribPointer(ATTRIBUTE_POSITION_LINEDIR, 4, GL_SHORT, true, 12, 0);
        glVertexAttribPointer(ATTRIBUTE_COLOR, 4, GL_UNSIGNED_BYTE, true, 12, 8);

        glDrawElements(GL_TRIANGLES, 3 * triangleCount, GL_UNSIGNED_SHORT, 0);
    }

    private void putFloat16(float fvalue, float scale) {
        if (fvalue < -scale) fvalue = scale;
        if (fvalue > scale) fvalue = scale;
        fvalue /= scale;
        vb.putShort((short)(fvalue * Short.MAX_VALUE));
    }

    private void putFloatU8(float fvalue, float scale) {
        if (fvalue < 0) fvalue = 0;
        if (fvalue > scale) fvalue = scale;
        fvalue /= scale;
        vb.put((byte)(fvalue * 255));
    }

    private void putPosition(float x, float y, float gs) {
        putFloat16( ( rotCos * x + rotSin * y + xOrigin + glowShiftX * gs), POSITION_SCALE * Game.WIDTH / 2 );
        putFloat16( (-rotSin * x + rotCos * y + yOrigin + glowShiftY * gs), POSITION_SCALE * Game.HEIGHT / 2 );
    }

    private void putLinedir(float x, float y) {
        putFloat16( rotCos * x + rotSin * y, LINEDIR_SCALE );
        putFloat16(-rotSin * x + rotCos * y, LINEDIR_SCALE );
    }

    private void putProperties() {
        putFloatU8(colorR, 1);
        putFloatU8(colorG, 1);
        putFloatU8(colorB, 1);
        putFloatU8(colorA, 1);
    }

    private void upload() {
        if (uploaded) return;

        if (vboVertices == 0) vboVertices = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        vb.flip();
        glBufferData(GL_ARRAY_BUFFER, vb, GL_DYNAMIC_DRAW);

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
            "const float ps = "+String.format("%.1f", POSITION_SCALE)+";\n"+
            "const float ls = "+String.format("%.1f", LINEDIR_SCALE)+";\n"+
            "l = position_linedir.zw * ls;\n"+
            "c = color;\n"+
            "gl_Position = vec4(position_linedir.xy * ps, 0.0, 1.0);\n"+
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