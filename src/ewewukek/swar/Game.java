package ewewukek.swar;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Queue;

import static ewewukek.swar.Util.*;
// import ewewukek.swar.net.*;

public class Game {
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;

    public static final float TIME_STEP = 0.05f;

    private long tick = 0;

    private static List<Particle> particles = new ArrayList<Particle>();
    private static List<ShipPart> shipParts = new ArrayList<ShipPart>();
    public static List<Ship> ships = new ArrayList<Ship>();
    public static List<Shot> shots = new ArrayList<Shot>();

    // private static Client client;
    // private static Server server;
    // private static Map<String, NetworkPlayer> networkPlayers = new HashMap<String, NetworkPlayer>();
    // private static Queue<NetworkEvent> outQueue = new LinkedList<NetworkEvent>();

    private static Ship playerShip;

    public static void reset() {
        particles.clear();
        shipParts.clear();
        ships.clear();
        shots.clear();
        playerShip = null;
    }

    public static Ship findShip(int id) {
        Iterator<Ship> it = ships.iterator();
        while (it.hasNext()) {
            Ship ship = it.next();
            if (ship.id == id) return ship;
        }
        if (playerShip != null && playerShip.id == id) {
            addShip(id, playerShip);
            return playerShip;
        }
        Ship ship = new Ship();
        ship.id = id;
        addShip(id, ship);
        return ship;
    }

    public static void addShip(Ship ship) {
        // if (client != null) return;
        ships.add(ship);
    }

    private static void addShip(Integer id, Ship ship) {
        ship.id = id;
        ships.add(ship);
    }

    public static void addShot(Shot shot) {
        shots.add(shot);
    }

    public static void addParticle(Particle p) {
        particles.add(p);
    }

    public static void addShipPart(ShipPart sp) {
        shipParts.add(sp);
    }

    public static void addBot(int team) {
        Ship bot = new Ship();
        bot.team = team;
        bot.ai = new AISimple();
        bot.placeRandom();
        bot.spawn();
        Game.addShip(bot);
    }

    public static boolean isPlayerShip(Ship ship) {
        return ship == playerShip;
    }

    public static void setPlayerTeam(int team) {
        // if (client != null) {
            // outQueue.add(new EventPlayerTeam(team));
            // return;
        // }
        if (playerShip == null) {
            playerShip = new Ship();
            addShip(playerShip);
        }
        playerShip.team = team;
        playerShip.placeRandom();
        playerShip.spawn();
    }

    public static void setPlayerInput(float turn, boolean throttle, boolean fire) {
        if (playerShip == null) return;
        playerShip.inputTurn = turn;
        playerShip.keyThrottle = throttle;
        playerShip.keyFire = fire;
    }

    private static final float[] markerX = new float[] { 0, -7.5f,  7.5f };
    private static final float[] markerY = new float[] { 0,  7.5f,  7.5f };
    private static final float[] markerL = new float[] { 0, 0, 0 };
    private static final int[] markerTris = new int[] { 0, 1, 2 };

    public static void draw(Batch batch, float delta) {
        batch.clear();
        batch.setDefaults();
        for (Iterator<Particle> it = particles.iterator(); it.hasNext(); it.next().draw(batch, delta));
        batch.draw();

        batch.clear();
        for (Iterator<Shot> it = shots.iterator(); it.hasNext(); it.next().draw(batch, delta));
        batch.draw();

        batch.clear();
        for (Iterator<Ship> it = ships.iterator(); it.hasNext(); it.next().draw(batch, delta));
        batch.draw();

        batch.clear();
        for (Iterator<ShipPart> it = shipParts.iterator(); it.hasNext(); it.next().draw(batch, delta));
        batch.draw();

        batch.clear();
        for (Iterator<Ship> it = ships.iterator(); it.hasNext(); it.next().drawShield(batch, delta));
        batch.draw();

        if (playerShip != null) {
            batch.clear();
            batch.setDefaults();
            batch.setOrigin(
                playerShip.x + playerShip.xv * delta,
                playerShip.y + Ship.size + 5f + playerShip.yv * delta
            );
            batch.addArrays(markerX, markerY, markerL, markerL, null, markerTris);
            batch.draw();
        }
    }

    private static void updateList(List<? extends Entity> list) {
        Iterator<? extends Entity> it = list.iterator();
        while (it.hasNext()) {
            Entity e = (Entity)it.next();
            if (!e.update()) {
                it.remove();
            }
        }
    }

    public static void update() {
        updateList(ships);
        updateList(shots);
        updateList(particles);
        updateList(shipParts);

        // if (client != null) {
            // Connection c = client.getConnection();
            // try {
                // if (playerShip != null) {
                    // c.sendEvent(new EventShipControl(0, playerShip.inputTurn, playerShip.keyThrottle, playerShip.keyFire));
                // }
                // while (!outQueue.isEmpty()) {
                    // c.sendEvent(outQueue.remove());
                // }
                // c.sendPacket();
            // } catch(IOException ex) {
                // client.onError(ex);
            // }
            // return;
        // }

        Iterator<Shot> shotIt = shots.iterator();
        while (shotIt.hasNext()) {
            Shot shot = shotIt.next();
            Iterator<Ship> shipIt = ships.iterator();
            while (shipIt.hasNext()) {
                Ship ship = shipIt.next();
                if (ship.team == shot.team || !ship.alive()) continue;
                if (dist(ship.x, ship.y, shot.x, shot.y) < Ship.size) {
                    ship.hit();
                    shot.hit(ship.isShieldActive());
                    break;
                }
            }
        }

        // if (server != null) {
            // try {
                // while (!outQueue.isEmpty()) {
                    // NetworkEvent event = outQueue.remove();
                    // for (Map.Entry<String, NetworkPlayer> entry: networkPlayers.entrySet()) {
                        // String clientId = entry.getKey();
                        // NetworkPlayer player = entry.getValue();
                        // Connection c = server.getClient(clientId);
                        // c.sendEvent(event);
                    // }
                // }
                // for (Map.Entry<String, NetworkPlayer> entry: networkPlayers.entrySet()) {
                    // String clientId = entry.getKey();
                    // NetworkPlayer player = entry.getValue();
                    // Connection c = server.getClient(clientId);
                    // c.sendPacket();
                // }
            // } catch(IOException ex) {
                // server.onError(ex);
            // }
        // }
    }

    // public static void startServer(InetAddress address, int port) {
        // if (server != null || client != null) {
            // System.out.println("already a server");
            // return;
        // }
        // server = new Server(address, port);
        // server.setErrorHandler(new ErrorHandler() {
            // @Override
            // public void onError(Throwable cause) {
                // cause.printStackTrace();
                // System.out.println("server stopped");
                // disconnect all
                // networkPlayers.clear();
                // server = null;
            // }
        // });
        // server.registerEventHandler(NetworkEvent.class, new EventHandler() {
            // @Override
            // public void onEvent(Connection c, Event e) {
                // inQueue.add(e);
            // }
        // });
        // server.registerEventHandler(EventTimeSync.class, new EventHandler() {
            // @Override
            // public void onEvent(Connection c, Event e) {
                // try {
                    // c.sendEvent(new EventTimeSync(time()));
                    // c.sendPacket();
                    // System.out.println(time());
                // } catch(IOException ex) {
                    // server.onError(ex);
                // }
            // }
        // });
        // server.registerEventHandler(EventPlayerTeam.class, new EventHandler() {
            // public void onEvent(Connection c, Event _e) {
                // EventPlayerTeam e = (EventPlayerTeam)_e;
                // NetworkPlayer player = networkPlayers.get(c.getId());
                // if (player == null) {
                    // player = new NetworkPlayer(c);
                    // networkPlayers.put(c.getId(), player);
                // }
                // Ship ship = player.ship;
                // if (ship == null) {
                    // ship = new Ship();
                    // player.ship = ship;
                    // addShip(ship);
                    // outQueue.add(new EventPlayerShip(ship.id));
                // }
                // ship.team = e.team;
                // outQueue.add(new EventShipTeam(ship.id, e.team));
                // ship.placeRandom();
                // outQueue.add(new EventShipPosition(ship.id, ship.x, ship.y, ship.a, ship.xv, ship.yv, ship.av));
                // ship.spawn();
                // outQueue.add(new EventShipSpawn(ship.id));
            // }
        // });
        // server.registerEventHandler(EventShipControl.class, new EventHandler() {
            // public void onEvent(Connection c, Event _e) {
                // EventShipControl e = (EventShipControl)_e;
                // NetworkPlayer player = networkPlayers.get(c.getId());
                // if (player == null || player.ship == null) return;
                // player.ship.inputTurn = e.turn;
                // player.ship.keyThrottle = e.throttle;
                // player.ship.keyFire = e.fire;
            // }
        // });
        // server.start();
        // System.out.println("server started");
    // }

    // private static float sendTime;

    // public static void startClient(InetAddress address, int port) {
        // if (server != null || client != null) {
            // System.out.println("already a client");
            // return;
        // }
        // client = new Client(address, port);
        // client.setErrorHandler(new ErrorHandler() {
            // @Override
            // public void onError(Throwable cause) {
                // cause.printStackTrace();
                // System.out.println("client disconnected");
                // send disconnected
                // client = null;
            // }
        // });
        // client.registerEventHandler(NetworkEvent.class, new EventHandler() {
            // @Override
            // public void onEvent(Connection c, Event e) {
                // inQueue.add(e);
            // }
        // });
        // client.registerEventHandler(EventTimeSync.class, new EventHandler() {
            // @Override
            // public void onEvent(Connection c, Event _e) {
                // EventTimeSync e = (EventTimeSync)_e;
                // setTime(e.time + (time() - sendTime) / 2);
                // System.out.println(time());
            // }
        // });
        // client.registerEventHandler(EventPlayerShip.class, new EventHandler() {
            // public void onEvent(Connection c, Event _e) {
                // EventPlayerShip e = (EventPlayerShip)_e;
                // if (playerShip == null) {
                    // playerShip = new Ship();
                    // playerShip.id = e.id;
                // }
            // }
        // });
        // client.registerEventHandler(EventShipTeam.class, new EventHandler() {
            // public void onEvent(Connection c, Event _e) {
                // EventShipTeam e = (EventShipTeam)_e;
                // Ship ship = findShip(e.id);
                // ship.team = e.team;
            // }
        // });
        // client.registerEventHandler(EventShipPosition.class, new EventHandler() {
            // public void onEvent(Connection c, Event _e) {
                // EventShipPosition e = (EventShipPosition)_e;
                // Ship ship = findShip(e.id);
                // ship.x = e.x;
                // ship.y = e.y;
                // ship.a = e.a;
                // ship.xv = e.xv;
                // ship.yv = e.yv;
                // ship.av = e.av;
            // }
        // });
        // client.registerEventHandler(EventShipSpawn.class, new EventHandler() {
            // public void onEvent(Connection c, Event _e) {
                // EventShipSpawn e = (EventShipSpawn)_e;
                // Ship ship = findShip(e.id);
                // ship.spawn();
            // }
        // });
        // client.connect();
        // try {
            // client.getConnection().sendEvent(new EventTimeSync(0));
            // client.getConnection().sendPacket();
        // } catch(IOException ex) {
            // client.onError(ex);
            // return;
        // }
        // sendTime = time();
        // reset();
        // System.out.println("client started");
    // }
}