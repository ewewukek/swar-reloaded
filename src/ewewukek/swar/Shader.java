package ewewukek.swar;

import static org.lwjgl.opengl.GL11.*;
// import static org.lwjgl.opengl.GL13.*;
// import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import org.joml.Matrix3f;

public class Shader {
    public static final int ATTRIBUTE_POSITION_LINEDIR = 0;
    public static final int ATTRIBUTE_COLOR = 1;

    public static Shader shader() {
        if (_instance == null) {
            _instance = new Shader();
        }
        return _instance;
    }

    public void use() {
        glUseProgram(program);
    }

    public static void setPosition(float x, float y) {
        glUniform2f(shader().uniform_translate, x * shader().scale_x, y * shader().scale_y);
    }

    public static void setRotation(float a) {
        fb.clear();
        double s = Math.sin(a);
        double c = Math.cos(a);
        fb.put((float)(c * shader().scale_x));
        fb.put((float)(-s * shader().scale_y));
        fb.put((float)(s * shader().scale_x));
        fb.put((float)(c * shader().scale_y));
        fb.flip();
        glUniformMatrix2(shader().uniform_transform, false, fb);
    }

    public static void setScale(float sx, float sy) {
        shader().scale_x = sx;
        shader().scale_y = sy;
    }

    public static void setLineWidth(float w) {
        glUniform1f(shader().uniform_line_width, w);
    }

    public static void setLineOffset(float o) {
        glUniform1f(shader().uniform_line_offset, o);
    }

    private static final FloatBuffer fb = BufferUtils.createFloatBuffer(4);

    private static Shader _instance;

    private int vs;
    private int fs;
    private int program;
    private int uniform_line_width;
    private int uniform_line_offset;
    private int uniform_transform;
    private int uniform_translate;

    private float scale_x;
    private float scale_y;

    private Shader() {
        vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
            "#version 120\n"+
            "attribute vec4 position_linedir;\n"+
            "attribute vec4 color;\n"+
            "varying vec2 l;\n"+
            "varying vec4 c;\n"+
            "uniform mat2 transform;\n"+
            "uniform vec2 translate;\n"+
            "void main() {\n"+
            "l = position_linedir.zw;\n"+
            "c = color;\n"+
            "gl_Position = vec4(transform * position_linedir.xy + translate, 0.0, 1.0);\n"+
            "}"
        );
        glCompileShader(vs);
        if (glGetShaderi(vs, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("vs: "+glGetShaderInfoLog(vs, glGetShaderi(vs, GL_INFO_LOG_LENGTH)));
            System.exit(1);
        }

        fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
            "#version 120\n"+
            "varying vec2 l;\n"+
            "varying vec4 c;\n"+
            "uniform float line_offst;\n"+
            "uniform float line_width;\n"+
            "void main() {\n"+
            "float a = min(sqrt(l.x * l.x + l.y * l.y) / 25.0, 1.0);\n"+
            // "a = 1.0 - a;\n"+
            "a = (1.2 - a) / (1.0 + 10.0* a);\n"+
            "gl_FragColor = vec4(c.rgb, c.a * a);\n"+
            "}"
        );
        glCompileShader(fs);
        glCompileShader(fs);
        if (glGetShaderi(fs, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("fs: "+glGetShaderInfoLog(fs, glGetShaderi(fs, GL_INFO_LOG_LENGTH)));
            System.exit(1);
        }

        program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glBindAttribLocation(program, ATTRIBUTE_POSITION_LINEDIR, "position_linedir");
        glBindAttribLocation(program, ATTRIBUTE_COLOR, "color");
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE) {
            System.err.println("link: "+glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH)));
            System.exit(1);
        }
        uniform_transform = glGetUniformLocation(program, "transform");
        uniform_translate = glGetUniformLocation(program, "translate");
        uniform_line_width = glGetUniformLocation(program, "line_width");
        uniform_line_offset = glGetUniformLocation(program, "line_offset");
    }
}