package ewewukek.swar;

import org.lwjgl.Sys;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

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

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);

        float a = 0;

        Ship ship = new Ship();
        ship.cr = 1;
        ship.cg = 0.4f;
        ship.cb = 0.4f;

        Ship ship2 = new Ship();
        ship2.cr = 0.4f;
        ship2.cg = 0.4f;
        ship2.cb = 1;

        Ship ship3 = new Ship();
        ship3.cr = 0.4f;
        ship3.cg = 1;
        ship3.cb = 0.4f;

        Ship.init();
        Stars.init(base_width, base_height);

        while (!Display.isCloseRequested()) {
            if (Display.wasResized()) resize();

            glClear(GL_COLOR_BUFFER_BIT);

            shader().setScale(2.0f / base_width, 2.0f / base_height);

            Stars.draw();

            a += 0.005f;

            ship.x = (float)Math.sin(Math.PI * a) * 192;
            ship.y = (float)Math.cos(Math.PI * a) * 192;
            ship.a = -a * 3f * (float)Math.PI;
            ship.draw();

            ship2.x = (float)Math.sin(-Math.PI * 1.2f * (a + 2.0f/3)) * 91;
            ship2.y = (float)Math.cos(-Math.PI * 1.2f * (a + 2.0f/3)) * 91;
            ship2.a = a * 2f * (float)Math.PI;
            ship2.draw();

            ship3.x = (float)Math.sin(Math.PI * 1.5f * (a + 4.0f/3)) * 283;
            ship3.y = (float)Math.cos(Math.PI * 1.5f * (a + 4.0f/3)) * 238;
            ship3.a = -a * 4f * (float)Math.PI;
            ship3.draw();

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