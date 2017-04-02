package ewewukek.swar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Batch {
    public static final float lineWidth = 0.5f;
    public static final float falloffMultiplier = 10;

    public static final int TEXT_ALIGN_TOP = 1;
    public static final int TEXT_ALIGN_RIGHT = 2;

    private static final int ATTRIBUTE_POSITION = 0;
    private static final int ATTRIBUTE_LINEDIR = 1;
    private static final int ATTRIBUTE_TEXCOORD = 1;
    private static final int ATTRIBUTE_COLOR = 2;

    private static final float POSITION_SCALE = 2;
    private static final float LINEDIR_SCALE = 64;

    private static int program;

    private static int fontTexture;
    private static int fontProgram;

    private static int uLineOffset;
    private static int uGlowRadius;
    private static int uFontTexture;

    private static final int vertexCapacity = 10240;
    private static final int triangleCapacity = 10240;

    private ByteBuffer vb = BufferUtils.createByteBuffer(vertexCapacity * 10);
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

    private float fontScale;

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
        fontScale = 1;
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

    public void setFontScale(float s) {
        fontScale = s;
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
            putColor();
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
            putColor();
        }
        for (int i = 0; i != 5; ++i) {
            putPosition(
                (-ex * ae[i] + ey * an[i]) * r + x2,
                (-ey * ae[i] - ex * an[i]) * r + y2,
                gs[i]);
            putLinedir(
                (-ex * ae[i] + ey * an[i]) * r,
                (-ey * ae[i] - ex * an[i]) * r);
            putColor();
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
            putColor();
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

        glEnableVertexAttribArray(ATTRIBUTE_POSITION);
        glEnableVertexAttribArray(ATTRIBUTE_LINEDIR);
        glEnableVertexAttribArray(ATTRIBUTE_COLOR);

        glVertexAttribPointer(ATTRIBUTE_POSITION, 2, GL_SHORT, true, 10, 0);
        glVertexAttribPointer(ATTRIBUTE_LINEDIR, 2, GL_BYTE, true, 10, 4);
        glVertexAttribPointer(ATTRIBUTE_COLOR, 4, GL_UNSIGNED_BYTE, true, 10, 6);

        glDrawElements(GL_TRIANGLES, 3 * triangleCount, GL_UNSIGNED_SHORT, 0);
    }

    public float stringWidth(String s) {
        float w = 0;
        for (int i = 0; i != s.length(); ++i) {
            char c = s.charAt(i);
            if (c == ' ') {
                w += 20;
                continue;
            }
            Glyph g = Glyph.get(c);
            if (g != null) w += g.w;
        }
        return w * fontScale;
    }

    public float stringHeight(String s) {
        float h = 0;
        for (int i = 0; i != s.length(); ++i) {
            char c = s.charAt(i);
            Glyph g = Glyph.get(c);
            if (g != null && g.h > h) h = g.h;
        }
        return h * fontScale;
    }

    public void drawString(String s) {
        drawString(s, 0);
    }

    public void drawString(String s, int flags) {
        if (vertexCount + 4 >= vertexCapacity || triangleCount + 2 >= triangleCapacity) {
            drawFont();
            clear();
        }
        float x = 0;
        float y = 0;
        if ((flags & TEXT_ALIGN_TOP) != 0) y -= stringHeight(s);
        if ((flags & TEXT_ALIGN_RIGHT) != 0) x -= stringWidth(s);
        for (int i = 0; i != s.length(); ++i) {
            char c = s.charAt(i);
            if (c == ' ') {
                x += 20 * fontScale;
                continue;
            }
            Glyph g = Glyph.get(c);
            if (g == null) continue;

            putPosition(x, y, 0);
            vb.put((byte)g.x);
            vb.put((byte)g.y);
            putColor();

            putPosition(x, y + g.h * fontScale, 0);
            vb.put((byte)g.x);
            vb.put((byte)(g.y + g.h));
            putColor();

            putPosition(x + g.w * fontScale, y + g.h * fontScale, 0);
            vb.put((byte)(g.x + g.w));
            vb.put((byte)(g.y + g.h));
            putColor();

            putPosition(x + g.w * fontScale, y, 0);
            vb.put((byte)(g.x + g.w));
            vb.put((byte)g.y);
            putColor();

            short i0 = vertexCount;

            ib.put((short)i0);
            ib.put((short)(i0 + 1));
            ib.put((short)(i0 + 2));
            ib.put((short)i0);
            ib.put((short)(i0 + 2));
            ib.put((short)(i0 + 3));

            vertexCount += 4;
            triangleCount += 2;

            x += g.w * fontScale;
        }
    }

    public void drawFont() {
        if (triangleCount == 0) return;
        upload();

        glBindTexture(GL_TEXTURE_2D, fontTexture);

        glUseProgram(fontProgram);

        glUniform1i(uFontTexture, 0);

        glEnableVertexAttribArray(ATTRIBUTE_POSITION);
        glEnableVertexAttribArray(ATTRIBUTE_TEXCOORD);
        glEnableVertexAttribArray(ATTRIBUTE_COLOR);

        glVertexAttribPointer(ATTRIBUTE_POSITION, 2, GL_SHORT, true, 10, 0);
        glVertexAttribPointer(ATTRIBUTE_TEXCOORD, 2, GL_UNSIGNED_BYTE, true, 10, 4);
        glVertexAttribPointer(ATTRIBUTE_COLOR, 4, GL_UNSIGNED_BYTE, true, 10, 6);

        glDrawElements(GL_TRIANGLES, 3 * triangleCount, GL_UNSIGNED_SHORT, 0);
    }

    private float normalizeFloat(float value, float scale) {
        if (value < -scale) value = scale;
        if (value > scale) value = scale;
        return value / scale;
    }

    private float normalizeFloatU(float value, float scale) {
        if (value < 0) value = 0;
        if (value > scale) value = scale;
        return value / scale;
    }

    private void putFloat16(float value, float scale) {
        vb.putShort((short)(normalizeFloat(value, scale) * Short.MAX_VALUE));
    }

    private void putFloat8(float value, float scale) {
        vb.put((byte)(normalizeFloat(value, scale) * Byte.MAX_VALUE));
    }

    private void putFloatU8(float value, float scale) {
        vb.put((byte)(normalizeFloatU(value, scale) * 255));
    }

    private void putPosition(float x, float y, float gs) {
        putFloat16( ( rotCos * x + rotSin * y + xOrigin + glowShiftX * gs), POSITION_SCALE * Game.WIDTH / 2 );
        putFloat16( (-rotSin * x + rotCos * y + yOrigin + glowShiftY * gs), POSITION_SCALE * Game.HEIGHT / 2 );
    }

    private void putLinedir(float x, float y) {
        putFloat8( rotCos * x + rotSin * y, LINEDIR_SCALE );
        putFloat8(-rotSin * x + rotCos * y, LINEDIR_SCALE );
    }

    private void putColor() {
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

        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
            "#version 120\n"+
            "attribute vec2 position;\n"+
            "attribute vec2 linedir;\n"+
            "attribute vec4 color;\n"+
            "varying vec2 l;\n"+
            "varying vec4 c;\n"+
            "void main() {\n"+
            "const float ps = "+String.format("%.1f", POSITION_SCALE)+";\n"+
            "const float ls = "+String.format("%.1f", LINEDIR_SCALE)+";\n"+
            "l = linedir * ls;\n"+
            "c = color;\n"+
            "gl_Position = vec4(position * ps, 0.0, 1.0);\n"+
            "}"
        );
        glCompileShader(vs);
        assertShaderCompileStatus(vs, "vertex");

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
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

        glBindAttribLocation(program, ATTRIBUTE_POSITION, "position");
        glBindAttribLocation(program, ATTRIBUTE_LINEDIR, "linedir");
        glBindAttribLocation(program, ATTRIBUTE_COLOR, "color");

        glLinkProgram(program);
        assertProgramLinkStatus(program);

        uLineOffset = glGetUniformLocation(program, "lo");
        uGlowRadius = glGetUniformLocation(program, "gr");

        initFont();
    }

    private static void initFont() {
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
            "#version 120\n"+
            "attribute vec2 position;\n"+
            "attribute vec2 texcoord;\n"+
            "attribute vec4 color;\n"+
            "varying vec2 t;\n"+
            "varying vec4 c;\n"+
            "void main() {\n"+
            "const float ps = "+String.format("%.1f", POSITION_SCALE)+";\n"+
            "t = texcoord;\n"+
            "c = color;\n"+
            "gl_Position = vec4(position * ps, 0.0, 1.0);\n"+
            "}"
        );
        glCompileShader(vs);
        assertShaderCompileStatus(vs, "vertex");

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
            "#version 120\n"+
            "varying vec2 t;\n"+
            "varying vec4 c;\n"+
            "uniform sampler2D texture;\n"+
            "void main() {\n"+
            "vec4 p = texture2D(texture, t);\n"+
            "gl_FragColor = vec4(p * c);\n"+
            "}"
        );
        glCompileShader(fs);
        assertShaderCompileStatus(fs, "fragment");

        fontProgram = glCreateProgram();
        glAttachShader(fontProgram, vs);
        glAttachShader(fontProgram, fs);

        glBindAttribLocation(fontProgram, ATTRIBUTE_POSITION, "position");
        glBindAttribLocation(fontProgram, ATTRIBUTE_TEXCOORD, "texcoord");
        glBindAttribLocation(fontProgram, ATTRIBUTE_COLOR, "color");

        glLinkProgram(fontProgram);
        assertProgramLinkStatus(fontProgram);

        uFontTexture = glGetUniformLocation(fontProgram, "texture");

        BufferedImage image = null;
        try {
            File f = new File("res/font.png");
            if (f.exists()) {
                image = ImageIO.read(new FileInputStream(f));
            } else {
                image = ImageIO.read(Batch.class.getResourceAsStream("/res/font.png"));
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (image == null) {
            System.err.println("could not load texture");
            System.exit(1);
        }

        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(4 * width * height);
        for(int y=height-1; y>=0; --y) {
            for(int x=0; x!=width; ++x) {
                int pixel = pixels[y*width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 8) & 0xFF)); // G
                buffer.put((byte) (pixel & 0xFF)); // B
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }
        buffer.flip();

        fontTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, fontTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
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