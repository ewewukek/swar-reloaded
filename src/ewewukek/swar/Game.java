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

import java.util.Random;

public class Game {
    final String title = "swar";
    private int width = 1024;
    private int height = 768;

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;

    private boolean[] keyPressed = new boolean[1024];

    public final int starCount = 512;
    private float[] starX = new float[starCount];
    private float[] starY = new float[starCount];
    private float[] starLuminosity = new float[starCount];

    public static final Random rnd = new Random();

    public Game() {
        for (int i = 0; i != starCount; ++i) {
            starX[i] = (rnd.nextFloat() - 0.5f) * WIDTH;
            starY[i] = (rnd.nextFloat() - 0.5f) * HEIGHT;
            starLuminosity[i] = 0.125f + rnd.nextFloat() * 0.875f;
        }
    }

    private void loop() throws LWJGLException {
        Display.setTitle(title);
        Display.setResizable(true);
        Display.setVSyncEnabled(true);
        Display.setFullscreen(false);
        Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
        Display.create(
            new PixelFormat(),
            new ContextAttribs(2, 1)
        );

        System.out.println("GL_VENDOR: "+glGetString(GL_VENDOR));
        System.out.println("GL_RENDERER: "+glGetString(GL_RENDERER));
        System.out.println("GL_VERSION: "+glGetString(GL_VERSION));
        System.out.println("GL_SHADING_LANGUAGE_VERSION: "+glGetString(GL_SHADING_LANGUAGE_VERSION));

        resize();

        Batch batch = new Batch(WIDTH, HEIGHT);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);

        Ship ship = new Ship();

        while (!Display.isCloseRequested()) {
            if (Display.wasResized()) resize();

            glClear(GL_COLOR_BUFFER_BIT);

            batch.clear();

            batch.setPosition(0, 0);
            batch.setRotation(0);
            batch.setLineWidth(0);
            batch.setLineOffset(0);
            batch.setGlowRadius(10);
            batch.setFalloffMultiplier(10);
            batch.setGlowShift(0, 0);
            for (int i = 0; i != starCount; ++i) {
                float c = starLuminosity[i] * (rnd.nextFloat() * 0.2f + 0.8f);
                float redShift = rnd.nextFloat() * 0.3f + 0.7f;
                float greenShift = rnd.nextFloat() * 0.1f + 0.9f;
                float blueShift = rnd.nextFloat() * 0.2f + 0.8f;
                batch.setColor(redShift * c, greenShift * c, blueShift * c, 1);
                batch.addPoint(starX[i], starY[i]);
            }

            ship.draw(batch);

            batch.draw();
            // batch.drawWireframe();

            Display.update();
            Display.sync(60);

            // float delta = 1.0f / 60;
            float delta = 1.0f;

            while (Keyboard.next()) {
                int code = Keyboard.getEventKey();
                if (Keyboard.getEventKeyState()) {
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
            if (keyPressed[Keyboard.KEY_UP]) {
                ship.throttle(delta);
            }
            if (keyPressed[Keyboard.KEY_LEFT]) {
                ship.turn_left(delta);
            }
            if (keyPressed[Keyboard.KEY_RIGHT]) {
                ship.turn_right(delta);
            }

            ship.update(delta);
        }
        Display.destroy();
        System.exit(0);
    }

    protected void resize() {
        float scale_x = (float)Display.getWidth() / WIDTH;
        float scale_y = (float)Display.getHeight() / HEIGHT;
        float scale = scale_x < scale_y ? scale_x : scale_y;
        width = (int)(WIDTH * scale);
        height = (int)(HEIGHT * scale);
        int offset_x = (Display.getWidth() - width) / 2;
        int offset_y = (Display.getHeight() - height) / 2;
        glViewport(offset_x, offset_y, width, height);
    }

    public static void main(String[] args) {
        System.out.println("LWJGL "+Sys.getVersion());
        try {
            new Game().loop();
        } catch(LWJGLException e) {
            e.printStackTrace();
        }
    }
}