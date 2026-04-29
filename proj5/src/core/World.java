package core;

import tileengine.TETile;
import tileengine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.Point;
import java.util.*;
import java.util.List;
import java.awt.Color; // Import Color

public final class World {

    public final long seed;
    public static final int WIDTH = 96;
    public static final int HEIGHT = 30;
    public static final int HUD_HEIGHT = 10;

    private int avatarX;
    private int avatarY;
    private TETile tileUnderAvatar = Tileset.FLOOR;
    public long worldX, worldY;

    public TETile[][] currentWindow;

    public World(long seed) {
        this.seed = seed;
        currentWindow = new TETile[WIDTH][HEIGHT];
        worldX = worldY = 0;
        enterDungeon(worldX, worldY);
    }

    public void enterDungeon(long dungeonWorldX, long dungeonWorldY) {
        currentWindow = generateDungeon(dungeonWorldX, dungeonWorldY);
        currentWindow[avatarX][avatarY] = Tileset.AVATAR;
    }

    private TETile[][] generateDungeon(long dungeonWorldX, long dungeonWorldY) {
        Random random = new Random(Objects.hash(seed, dungeonWorldX, dungeonWorldY));
        int numberOfRooms = random.nextInt(9) + 7;

        TETile[][] toReturn = new TETile[WIDTH][HEIGHT];
        for (TETile[] row : toReturn) {
            java.util.Arrays.fill(row, Tileset.NOTHING);
        }

        Queue<Room> roomQueue = new LinkedList<>();
        roomQueue.add(new Room(1, 1, WIDTH - 2, HEIGHT - 2));
        List<Room> leaves = new ArrayList<>();

        while (!roomQueue.isEmpty()) {
            Room r = roomQueue.poll();
            if (r.WIDTH() > 13 && r.HEIGHT() > 13 && (leaves.size() + roomQueue.size() + 1) < numberOfRooms) {
                List<Room> children = splitRoom(r, random);
                roomQueue.addAll(children);
                connectRooms(toReturn, children.get(0), children.get(1));
            } else {
                leaves.add(r);
            }
        }

        for (Room r : leaves) {
            drawRoomInsidePartition(toReturn, r, random);
        }

        buildWalls(toReturn, random); // Pass random to buildWalls

        int startRoomId = random.nextInt(leaves.size());
        Room avatarStartRoom = leaves.get(startRoomId);
        int[] tmp = utils.RandomUtils.randomBoundedCoord(random, avatarStartRoom.x(), avatarStartRoom.y(), avatarStartRoom.WIDTH(), avatarStartRoom.HEIGHT());
        avatarX = tmp[0];
        avatarY = tmp[1];
        tileUnderAvatar = Tileset.FLOOR;

        return toReturn;
    }

    private List<Room> splitRoom(Room toSplit, Random random) {
        boolean verticalSplit = random.nextBoolean();
        if (verticalSplit) {
            int xSplit = toSplit.x() + random.nextInt((int) (toSplit.WIDTH() * 0.4)) + toSplit.WIDTH() / 3;
            return new ArrayList<>(List.of(
                    new Room(toSplit.x(), toSplit.y(), (xSplit - toSplit.x()), toSplit.HEIGHT()),
                    new Room(xSplit, toSplit.y(), (toSplit.WIDTH() - (xSplit - toSplit.x())), toSplit.HEIGHT())
            ));
        } else {
            int ySplit = toSplit.y() + random.nextInt((int) (toSplit.HEIGHT() * 0.4)) + toSplit.HEIGHT() / 3;
            return new ArrayList<>(List.of(
                    new Room(toSplit.x(), toSplit.y(), toSplit.WIDTH(), (ySplit - toSplit.y())),
                    new Room(toSplit.x(), ySplit, toSplit.WIDTH(), (toSplit.HEIGHT() - (ySplit - toSplit.y())))
            ));
        }
    }

    private void drawRoomInsidePartition(TETile[][] world, Room partition, Random random) {
        int centerX = partition.x() + partition.WIDTH() / 2;
        int centerY = partition.y() + partition.HEIGHT() / 2;
        int halfW = random.nextInt(1, Math.max(2, partition.WIDTH() / 2));
        int halfH = random.nextInt(1, Math.max(2, partition.HEIGHT() / 2));

        for (int x = centerX - halfW; x <= centerX + halfW; x++) {
            for (int y = centerY - halfH; y <= centerY + halfH; y++) {
                world[x][y] = Tileset.FLOOR;
            }
        }
        partition.x = centerX - halfW;
        partition.y = centerY - halfH;
        partition.WIDTH = halfW * 2;
        partition.HEIGHT = halfH * 2;
    }

    private void connectRooms(TETile[][] world, Room r1, Room r2) {
        int x1 = r1.x() + r1.WIDTH() / 2;
        int y1 = r1.y() + r1.HEIGHT() / 2;
        int x2 = r2.x() + r2.WIDTH() / 2;
        int y2 = r2.y() + r2.HEIGHT() / 2;

        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            world[x][y1] = Tileset.FLOOR;
        }
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            world[x2][y] = Tileset.FLOOR;
        }
    }

    private void buildWalls(TETile[][] world, Random random) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT && world[nx][ny] == Tileset.NOTHING) {
                                // Assign a random color to the wall
                                Color randomColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                                world[nx][ny] = new TETile(Tileset.WALL.character(), Tileset.WALL.textColor(), randomColor, Tileset.WALL.description(), Tileset.WALL.id());
                            }
                        }
                    }
                }
            }
        }
    }

    public void handleKey(char c) {
        int nextX = avatarX;
        int nextY = avatarY;

        switch (c) {
            case 'w': nextY++; break;
            case 's': nextY--; break;
            case 'a': nextX--; break;
            case 'd': nextX++; break;
            default: return;
        }

        if (isValidMove(nextX, nextY)) {
            currentWindow[avatarX][avatarY] = tileUnderAvatar;
            avatarX = nextX;
            avatarY = nextY;
            tileUnderAvatar = currentWindow[avatarX][avatarY];
            currentWindow[avatarX][avatarY] = Tileset.AVATAR;
        }
    }

    private boolean isValidMove(int x, int y) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            TETile targetTile = currentWindow[x][y];
            return !targetTile.equals(Tileset.WALL) && !targetTile.equals(Tileset.NOTHING);
        }
        return false;
    }

    public Point getAvatarPosition() {
        return new Point(avatarX, avatarY);
    }

    public boolean isTraversable(int x, int y) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            return currentWindow[x][y].isTraversable();
        }
        return false;
    }

    public TETile[][] getTiles() {
        return currentWindow;
    }

    public boolean isWall(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return true; // Treat out of bounds as walls
        }
        return currentWindow[x][y].equals(Tileset.WALL) || currentWindow[x][y].description().equals(Tileset.WALL.description());
    }

    public TETile getTile(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return Tileset.NOTHING; // Or a specific "out of bounds" tile
        }
        return currentWindow[x][y];
    }
}
