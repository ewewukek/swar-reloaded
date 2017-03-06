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

import static ewewukek.swar.Util.*;

public class Main {
    private static final String title = "swar";
    private static boolean[] keyPressed = new boolean[1024];

    private static final int starCount = 512;
    private static float[] starX = new float[starCount];
    private static float[] starY = new float[starCount];
    private static float[] starLuminosity = new float[starCount];

    private static void start() throws LWJGLException {
        for (int i = 0; i != starCount; ++i) {
            starX[i] = (rand() - 0.5f) * Game.WIDTH;
            starY[i] = (rand() - 0.5f) * Game.HEIGHT;
            starLuminosity[i] = 0.125f + rand() * 0.875f;
        }

        Display.setTitle(title);
        Display.setResizable(true);
        Display.setVSyncEnabled(true);
        Display.setFullscreen(false);
        Display.setDisplayMode(new DisplayMode(Game.WIDTH, Game.HEIGHT));
        Display.create(
            new PixelFormat(),
            new ContextAttribs(2, 1)
        );

        System.out.println("GL_VENDOR: "+glGetString(GL_VENDOR));
        System.out.println("GL_RENDERER: "+glGetString(GL_RENDERER));
        System.out.println("GL_VERSION: "+glGetString(GL_VERSION));
        System.out.println("GL_SHADING_LANGUAGE_VERSION: "+glGetString(GL_SHADING_LANGUAGE_VERSION));

        resize();

        Batch batch = new Batch(Game.WIDTH, Game.HEIGHT);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);

        float tickTime = time();

        Ship someShip = new Ship();
        someShip.team = 1;
        someShip.ai = new AISimple();
        someShip.x = Game.WIDTH * 0.25f;
        someShip.spawn();
        Game.addShip(someShip);

        while (!Display.isCloseRequested()) {
            if (Display.wasResized()) resize();

            glClear(GL_COLOR_BUFFER_BIT);

            batch.clear();

            batch.setDefaults();
            for (int i = 0; i != starCount; ++i) {
                float c = starLuminosity[i] * (rand() * 0.2f + 0.8f);
                float redShift = rand() * 0.3f + 0.7f;
                float greenShift = rand() * 0.1f + 0.9f;
                float blueShift = rand() * 0.2f + 0.8f;
                batch.setColor(redShift * c, greenShift * c, blueShift * c, 1);
                batch.addPoint(starX[i], starY[i]);
            }

            float delta = (time() - tickTime) / Game.TIME_STEP;
            Game.draw(batch, delta);

            batch.draw();

            Display.update();
            Display.sync(60);

            while (Keyboard.next()) {
                int code = Keyboard.getEventKey();
                if (Keyboard.getEventKeyState()) {
                    keyPressed[code] = true;

                    switch(code) {
                        case Keyboard.KEY_ESCAPE:
                            System.exit(0);
                        break;
                        case Keyboard.KEY_1:
                            Game.setPlayerTeam(0);
                        break;
                        case Keyboard.KEY_2:
                            Game.setPlayerTeam(1);
                        break;
                        case Keyboard.KEY_3:
                            Game.setPlayerTeam(2);
                        break;
                        case Keyboard.KEY_4:
                            Game.setPlayerTeam(3);
                        break;
                    }
                } else {
                    keyPressed[code] = false;
                }
            }

            Game.setPlayerKeys(
                keyPressed[Keyboard.KEY_LEFT],
                keyPressed[Keyboard.KEY_RIGHT],
                keyPressed[Keyboard.KEY_UP],
                keyPressed[Keyboard.KEY_LCONTROL] || keyPressed[Keyboard.KEY_RCONTROL]
            );

            while (tickTime + Game.TIME_STEP < time()) {
                tickTime += Game.TIME_STEP;
                Game.update();
            }
        }
    }

    private static void resize() {
        float scale_x = (float)Display.getWidth() / Game.WIDTH;
        float scale_y = (float)Display.getHeight() / Game.HEIGHT;
        float scale = scale_x < scale_y ? scale_x : scale_y;
        int width = (int)(Game.WIDTH * scale);
        int height = (int)(Game.HEIGHT * scale);
        int offset_x = (Display.getWidth() - width) / 2;
        int offset_y = (Display.getHeight() - height) / 2;
        glViewport(offset_x, offset_y, width, height);
    }

    public static void main(String[] args) {
        System.out.println("LWJGL "+Sys.getVersion());
        try {
            start();
            Display.destroy();
            System.exit(0);
        } catch(LWJGLException e) {
            e.printStackTrace();
        }
    }
}