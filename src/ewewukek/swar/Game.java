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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static ewewukek.swar.Util.*;

public class Game {
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;

    public static final double timeStep = 0.05;

    private double tickTime;
    private long tick = 0;

    private final String title = "swar";

    private boolean[] keyPressed = new boolean[1024];

    private final int starCount = 512;
    private float[] starX = new float[starCount];
    private float[] starY = new float[starCount];
    private float[] starLuminosity = new float[starCount];

    private static List<Entity> entities = new ArrayList<Entity>();
    private static List<Entity> newEntities = new ArrayList<Entity>();

    public static void addEntity(Entity e) {
        newEntities.add(e);
    }

    private double getTime() {
        return (double)Sys.getTime() / Sys.getTimerResolution();
    }

    private Game() {
        for (int i = 0; i != starCount; ++i) {
            starX[i] = (rand() - 0.5f) * WIDTH;
            starY[i] = (rand() - 0.5f) * HEIGHT;
            starLuminosity[i] = 0.125f + rand() * 0.875f;
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

        Ship ship = new Ship(0);
        ship.spawnAt(0, 0);
        addEntity(ship);

        tickTime = getTime();

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

            double delta = (getTime() - tickTime) / timeStep;
            drawEntities(batch, (float)delta);

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
                        case Keyboard.KEY_LCONTROL:
                        case Keyboard.KEY_RCONTROL:
                            ship.shoot();
                        break;
                        case Keyboard.KEY_1:
                            ship.setTeam(0);
                        break;
                        case Keyboard.KEY_2:
                            ship.setTeam(1);
                        break;
                        case Keyboard.KEY_3:
                            ship.setTeam(2);
                        break;
                        case Keyboard.KEY_4:
                            ship.setTeam(3);
                        break;
                        case Keyboard.KEY_K:
                            ship.kill();
                        break;
                        case Keyboard.KEY_SPACE:
                            ship.spawnAt((rand() - 0.5f) * WIDTH, (rand() - 0.5f) * HEIGHT);
                        break;
                    }
                } else {
                    keyPressed[code] = false;
                }
            }
            if (keyPressed[Keyboard.KEY_UP]) {
                ship.throttle();
            }
            if (keyPressed[Keyboard.KEY_LEFT]) {
                ship.turnLeft();
            }
            if (keyPressed[Keyboard.KEY_RIGHT]) {
                ship.turnRight();
            }

            entities.addAll(newEntities);
            newEntities.clear();

            while (tickTime + timeStep < getTime()) {
                tickTime += timeStep;
                tick++;
                updateEntities();
            }
        }
        Display.destroy();
        System.exit(0);
    }

    private static void drawEntities(Batch batch, float delta) {
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = (Entity)it.next();
            e.draw(batch, delta);
        }
    }

    private static void updateEntities() {
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = (Entity)it.next();
            if (!e.update()) {
                it.remove();
            }
        }
    }

    private void resize() {
        float scale_x = (float)Display.getWidth() / WIDTH;
        float scale_y = (float)Display.getHeight() / HEIGHT;
        float scale = scale_x < scale_y ? scale_x : scale_y;
        int width = (int)(WIDTH * scale);
        int height = (int)(HEIGHT * scale);
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