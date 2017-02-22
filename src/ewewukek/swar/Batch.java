package ewewukek.swar;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Batch {
    public static final int ATTRIBUTE_POSITION_LINEDIR = 0;
    public static final int ATTRIBUTE_COLOR = 1;
    public static final int ATTRIBUTE_LINE_WIDTH = 2;
    public static final int ATTRIBUTE_LINE_OFFSET = 3;
    public static final int ATTRIBUTE_GLOW_RADIUS = 4;
    public static final int ATTRIBUTE_FALLOFF_MULTIPLIER = 5;

    private static int fs;
    private static int vs;
    private static int program;

    private static int fsl;
    private static int vsl;
    private static int programl;

    private int screenWidth = 1024;
    private int screenHeight = 768;

    public static final int vertexCapacity = 1024;
    public static final int triangleCapacity = 1024;
    public static final int lineCapacity = triangleCapacity * 3;

    private FloatBuffer fb = BufferUtils.createFloatBuffer(vertexCapacity * 12);
    private int vertexCount;
    private int vboVertices;

    private IntBuffer ib = BufferUtils.createIntBuffer(triangleCapacity * 3);
    private int triangleCount;
    private int vboIndices;

    private IntBuffer ibl = BufferUtils.createIntBuffer(lineCapacity * 2);
    private int lineCount;
    private int vboIndicesl;

    private float lineWidth = 0.5f;
    private float lineOffset = 0;
    private float glowRadius = 10;
    private float falloffMultiplier = 2;

    private float colorR = 1;
    private float colorG = 1;
    private float colorB = 1;
    private float colorA = 1;

    private float rotSin = 0;
    private float rotCos = 1;

    private float xScale = 1;
    private float yScale = 1;

    private float xOrigin = 0;
    private float yOrigin = 0;

    private float glowShiftX = 0;
    private float glowShiftY = 0;

    public void setColor(float r, float g, float b, float a) {
        colorR = r;
        colorG = g;
        colorB = b;
        colorA = a;
    }

    public void setLineWidth(float lw) {
        lineWidth = lw;
    }

    public void setLineOffset(float lo) {
        lineOffset = lo;
    }

    public void setGlowRadius(float g) {
        glowRadius = g;
    }

    public void setFalloffMultiplier(float f) {
        falloffMultiplier = f;
    }

    public void setGlowShift(float x, float y) {
        glowShiftX = x;
        glowShiftY = y;
    }

    public Batch(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        xScale = 2.0f / screenWidth;
        yScale = 2.0f / screenHeight;
        init();
    }

    private boolean uploaded;

    public void clear() {
        fb.clear();
        vertexCount = 0;
        ib.clear();
        triangleCount = 0;
        ibl.clear();
        lineCount = 0;
        uploaded = false;
    }

    public void setPosition(float x, float y) {
        xOrigin = x;
        yOrigin = y;
    }

    public void setRotation(float rad) {
        rotSin = (float)Math.sin(rad);
        rotCos = (float)Math.cos(rad);
    }

    private void putX(float x) {
        
        fb.put((x + xOrigin) * 2 / screenWidth);
    }

    private void putY(float y) {
        fb.put((y + yOrigin) * 2 / screenHeight);
    }

    private void putPosition(float x, float y, float ox, float oy, float gs) {
        fb.put( ( rotCos * (x + ox) + rotSin * (y + oy) + xOrigin + glowShiftX * gs) * 2 / screenWidth );
        fb.put( (-rotSin * (x + ox) + rotCos * (y + oy) + yOrigin + glowShiftY * gs) * 2 / screenHeight );
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
        fb.put(lineWidth);
        fb.put(lineOffset);
        fb.put(glowRadius);
        fb.put(falloffMultiplier);
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
        final int[] lines = new int[] {
            1, 3,
            2, 4,
            1, 2,
            2, 3,
            3, 4,
            4, 1
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
        for (int i = 0; i != lines.length; ++i) {
            ibl.put(i0 + lines[i]);
        }
        lineCount += lines.length / 2;
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
        final int[] lines = new int[] {
            3, 2,
            4, 1,
            6, 9,
            7, 8,
            3, 0,
            3, 7,
            2, 8,
            0, 2,
            0, 3,
            5, 7,
            5, 8
        };

        if (vertexCount + 10 >= vertexCapacity || triangleCount + tris.length / 3 >= triangleCapacity) {
            draw();
            clear();
        }
        float ex = x2 - x1;
        float ey = y2 - y1;
        float l = (float)Math.sqrt(ex*ex + ey*ey);
        ex /= l;
        ey /= l;
        float nx = -ey;
        float ny = ex;
        float r = lineOffset + lineWidth + glowRadius;
        for (int i = 0; i != 5; ++i) {
            putPosition(
                (ex * ae[i] + nx * an[i]) * r,
                (ey * ae[i] + ny * an[i]) * r,
                x1, y1, gs[i]);
            putLinedir(
                (ex * ae[i] + nx * an[i]) * r,
                (ey * ae[i] + ny * an[i]) * r);
            putProperties();
        }
        for (int i = 0; i != 5; ++i) {
            putPosition(
                (-ex * ae[i] - nx * an[i]) * r,
                (-ey * ae[i] - ny * an[i]) * r,
                x2, y2, gs[i]);
            putLinedir(
                (-ex * ae[i] - nx * an[i]) * r,
                (-ey * ae[i] - ny * an[i]) * r);
            putProperties();
        }
        int i0 = vertexCount;
        vertexCount += 10;
        for (int i = 0; i != tris.length; ++i) {
            ib.put(i0 + tris[i]);
        }
        triangleCount += tris.length / 3;
        for (int i = 0; i != lines.length; ++i) {
            ibl.put(i0 + lines[i]);
        }
        lineCount += lines.length / 2;
    }

    public void addArrays(float[] x, float[] y, float[] lx, float[] ly, int[] tris, float[] gs) {
        if (vertexCount + x.length >= vertexCapacity || triangleCount + tris.length / 3 >= triangleCapacity) {
            draw();
            clear();
        }
        for (int i = 0; i != x.length; ++i) {
            putPosition(x[i], y[i], 0, 0, gs[i]);
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

        if (vboIndicesl == 0) vboIndicesl = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesl);
        ibl.flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ibl, GL_DYNAMIC_DRAW);

        uploaded = true;
    }

    public void draw() {
        if (triangleCount == 0) return;
        upload();

        glUseProgram(program);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);

        glVertexAttribPointer(ATTRIBUTE_POSITION_LINEDIR, 4, GL_FLOAT, false, 12 * 4, 0);
        glEnableVertexAttribArray(ATTRIBUTE_POSITION_LINEDIR);

        glVertexAttribPointer(ATTRIBUTE_COLOR, 4, GL_FLOAT, false, 12 * 4, 4 * 4);
        glEnableVertexAttribArray(ATTRIBUTE_COLOR);

        glVertexAttribPointer(ATTRIBUTE_LINE_WIDTH, 1, GL_FLOAT, false, 12 * 4, 8 * 4);
        glEnableVertexAttribArray(ATTRIBUTE_LINE_WIDTH);

        glVertexAttribPointer(ATTRIBUTE_LINE_OFFSET, 1, GL_FLOAT, false, 12 * 4, 9 * 4);
        glEnableVertexAttribArray(ATTRIBUTE_LINE_OFFSET);

        glVertexAttribPointer(ATTRIBUTE_GLOW_RADIUS, 1, GL_FLOAT, false, 12 * 4, 10 * 4);
        glEnableVertexAttribArray(ATTRIBUTE_GLOW_RADIUS);

        glVertexAttribPointer(ATTRIBUTE_FALLOFF_MULTIPLIER, 1, GL_FLOAT, false, 12 * 4, 11 * 4);
        glEnableVertexAttribArray(ATTRIBUTE_FALLOFF_MULTIPLIER);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndices);

        glDrawElements(GL_TRIANGLES, 3 * triangleCount, GL_UNSIGNED_INT, 0);
    }

    public void drawWireframe() {
        if (lineCount == 0) return;
        upload();

        glUseProgram(programl);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);

        glVertexAttribPointer(ATTRIBUTE_POSITION_LINEDIR, 2, GL_FLOAT, false, 12 * 4, 0);
        glEnableVertexAttribArray(ATTRIBUTE_POSITION_LINEDIR);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesl);

        glDrawElements(GL_LINES, 2 * lineCount, GL_UNSIGNED_INT, 0);
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
            "attribute float line_width;\n"+
            "attribute float line_offset;\n"+
            "attribute float glow_radius;\n"+
            "attribute float falloff_multiplier;\n"+
            "varying vec2 l;\n"+
            "varying vec4 c;\n"+
            "varying float lw;\n"+
            "varying float lo;\n"+
            "varying float g;\n"+
            "varying float f;\n"+
            "void main() {\n"+
            "l = position_linedir.zw;\n"+
            "c = color;\n"+
            "lw = line_width;\n"+
            "lo = line_offset;\n"+
            "g = glow_radius;\n"+
            "f = falloff_multiplier;\n"+
            "gl_Position = vec4(position_linedir.xy, 0.0, 1.0);\n"+
            "}"
        );
        glCompileShader(vs);
        assertShaderCompileStatus(vs);

        fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
            "#version 120\n"+
            "varying vec2 l;\n"+
            "varying vec4 c;\n"+
            "varying float lw;\n"+
            "varying float lo;\n"+
            "varying float g;\n"+
            "varying float f;\n"+
            "void main() {\n"+
            "float a = abs(sqrt(l.x * l.x + l.y * l.y) - lo);\n"+
            "float t = (sign(lw - a) + 1.0) * 0.5;\n"+
            "a = t + (1.0 - t) * (1.0 - (a-lw)/g) / (1.0 + f * (a-lw)/g);\n"+
            "gl_FragColor = vec4(c.rgb, c.a * a);\n"+
            "}"
        );
        glCompileShader(fs);
        assertShaderCompileStatus(fs);

        program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glBindAttribLocation(program, ATTRIBUTE_POSITION_LINEDIR, "position_linedir");
        glBindAttribLocation(program, ATTRIBUTE_COLOR, "color");
        glBindAttribLocation(program, ATTRIBUTE_LINE_WIDTH, "line_width");
        glBindAttribLocation(program, ATTRIBUTE_LINE_OFFSET, "line_offset");
        glBindAttribLocation(program, ATTRIBUTE_GLOW_RADIUS, "glow_radius");
        glBindAttribLocation(program, ATTRIBUTE_FALLOFF_MULTIPLIER, "falloffMultiplier");
        glLinkProgram(program);

        vsl = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vsl,
            "#version 120\n"+
            "attribute vec2 position;\n"+
            "void main() {\n"+
            "gl_Position = vec4(position, 0.0, 1.0);\n"+
            "}"
        );
        glCompileShader(vsl);
        assertShaderCompileStatus(vsl);

        fsl = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fsl,
            "#version 120\n"+
            "void main() {\n"+
            "gl_FragColor = vec4(0.0, 0.5, 0.0, 1.0);\n"+
            "}"
        );
        glCompileShader(fsl);
        assertShaderCompileStatus(fsl);

        programl = glCreateProgram();
        glAttachShader(programl, vsl);
        glAttachShader(programl, fsl);
        glBindAttribLocation(programl, ATTRIBUTE_POSITION_LINEDIR, "position");
        glLinkProgram(programl);
        assertProgramLinkStatus(programl);
    }

    private static void assertShaderCompileStatus(int shader) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("vs: "+glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
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