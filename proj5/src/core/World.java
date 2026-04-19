package core;

import tileengine.TETile;
import tileengine.Tileset;

import java.util.*;
import java.util.List;


/*
World is not static - you can make a World() object. (many, in fact).
World will contain both static and non static methods
We choose this design because there is important metadata
associated with worlds, such as a list of players and NPCs (Character)


World handles the game state and all game mechanics.
Note that the map is federated, kind of like Pokemon. So there's a Perlin based world
that has images of dungeons, but you walk into a door and get into the dungeon.
 */

/*
Okay so it's inefficient to reassign all the memory every time you move around.
Inside a dungeon the character will not be in the middle since the entire dungeon is drawn in one screen.
So we don't care about redraws.
However when outside the character definitely cares about redraws. Character is in the middle of the screen at this time.
So in that case we need to use a **toroidal array** to keep track of position.
TODO: Consider making toroidal array a new class.
 */

public final class World {

    public final long seed;
    public static final int WIDTH = 50; // Better to make this odd for centering
    public static final int HEIGHT = 50;

    private int avatarX;
    private int avatarY; // Relative to the current screen (only relevant in dungeons)
    private int startX;
    private int startY;
    private int endX;
    private int endY; // Implementing toroidal array
    public long worldX, worldY; // Top left corner of the screen to set a core.World origin

    public TETile[][] currentWindow;

    public World(long seed) {
        this.seed = seed;
        currentWindow = new TETile[WIDTH][HEIGHT];
        startX = startY = 0;
        endX = WIDTH;
        endY = HEIGHT;
        avatarX = (WIDTH - 1) / 2;
        avatarY = (HEIGHT - 1) / 2;
        worldX = worldY = 0;

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                currentWindow[x][y] = Tileset.NOTHING;
            }
        }
        currentWindow[avatarX][avatarY] = Tileset.UNLOCKED_DOOR;
    }

    /**
     * Used when entering a dungeon. Note: in the highest level of federation, a dungeon need not
     * be its drawn size; you can have a 1x1 dungeon icon, for instance (though this would suck)
     * @param dungeonWorldX absolute worldX coordinate of the dungeon DOOR with respect to the highest level of world federation
     * @param dungeonWorldY absolute worldY coord of the dungeon door.
     *          X and Y ensure that all dungeons are unique.
     */
    public void enterDungeon(long dungeonWorldX, long dungeonWorldY) {
        currentWindow = generateDungeon(dungeonWorldX, dungeonWorldY);

        // TODO: Maybe a nice transition.
    }

    // Generates a dungeon given a seed, X, Y, HEIGHT, WIDTH.

    /**
     * Generates a dungeon in reasonable time via a binary space partitioning algorithm.
     * @param dungeonWorldX Same as enterDungeon
     * @param dungeonWorldY Same as enterDungeon
     * @return Returns 2D array of a dungeon
     */
    // Room height and width represent a number of floor tiles.
    private TETile[][] generateDungeon(long dungeonWorldX, long dungeonWorldY) {
        Random random = new Random(Objects.hash(seed, dungeonWorldX, dungeonWorldY)); // Random generator for a given dungeon, so that the world won't have 2 same dungeons
        int numberOfRooms = random.nextInt(9) + 7; // 7...15

        TETile[][] toReturn = new TETile[WIDTH][HEIGHT];
        for (TETile[] row : toReturn) {
            java.util.Arrays.fill(row, Tileset.NOTHING);
        }

        Queue<Room> roomQueue = new LinkedList<>(); // addAll
        roomQueue.add(new Room(1, 1, WIDTH - 2, HEIGHT - 2)); // note: FORCES DUNGEONS TO BE ONE SCREEN
        List<Room> leaves = new ArrayList<>();

        while (!roomQueue.isEmpty()) { // Generates a queue containing the rooms
            Room r = roomQueue.poll();

            if (r.WIDTH() > 13 && r.HEIGHT() > 13 && (leaves.size() + roomQueue.size() + 1) < numberOfRooms) {
                List<Room> children = splitRoom(r, random);
                roomQueue.addAll(children);
                connectRooms(toReturn, children.get(0), children.get(1));
            } else { // Throw away the room
                leaves.add(r);
            }
        }

        for (Room r : leaves) {
            drawRoomInsidePartition(toReturn, r, random);
        }

        buildWalls(toReturn);

        int startRoomId = random.nextInt(leaves.size());

        Room avatarStartRoom = leaves.get(startRoomId);
        int[] tmp = utils.RandomUtils.randomBoundedCoord(random, avatarStartRoom.x(), avatarStartRoom.y(), avatarStartRoom.WIDTH(), avatarStartRoom.HEIGHT());
        avatarX = tmp[0];
        avatarY = tmp[1];

        return toReturn;

    }

    private List<Room> splitRoom(Room toSplit, Random random) {

        boolean verticalSplit = random.nextBoolean();
        if(verticalSplit) {
            int xSplit = toSplit.x() + random.nextInt( (int)(toSplit.WIDTH() * 0.4)) + toSplit.WIDTH() / 3; // Between 33% and 70%
            return new ArrayList<>(List.of(
                    new Room(toSplit.x(), toSplit.y(), (xSplit - toSplit.x()), toSplit.HEIGHT()),
                    new Room(xSplit, toSplit.y(), (toSplit.WIDTH() - (xSplit - toSplit.x())), toSplit.HEIGHT())
            ));
        } else {
            int ySplit = toSplit.y() + random.nextInt( (int)(toSplit.HEIGHT() * 0.4)) + toSplit.HEIGHT() / 3; // Between 33% and 70%
            return new ArrayList<>(List.of(
                    new Room(toSplit.x(), toSplit.y(), toSplit.WIDTH(), (ySplit - toSplit.y())),
                    new Room(toSplit.x(), ySplit, toSplit.WIDTH(), (toSplit.HEIGHT() - (ySplit - toSplit.y())))
            ));
        }
    }

    private void drawRoomInsidePartition(TETile[][] world, Room partition, Random random) {
        int centerX = partition.x() + partition.WIDTH() / 2;
        int centerY = partition.y() + partition.HEIGHT() / 2;

        // Random size markers. We must guarantee a safe upper bound.
        int halfW = random.nextInt(1, Math.max(2, partition.WIDTH() / 2));
        int halfH = random.nextInt(1, Math.max(2, partition.HEIGHT() / 2));

        for(int x = centerX - halfW ; x <= centerX + halfW ; x++) {
            for(int y = centerY - halfH; y <= centerY + halfH ; y++) {
                world[x][y] = Tileset.FLOOR;
            }
        }
        partition.x = centerX - halfW;
        partition.y = centerY - halfH;
        partition.WIDTH = halfW * 2;
        partition.HEIGHT = halfH * 2;

    }

    private void connectRooms(TETile[][] world, Room r1, Room r2)  {
        int x1 = r1.x() + r1.WIDTH() / 2;
        int y1 = r1.y() + r1.HEIGHT() / 2;
        int x2 = r2.x() + r2.WIDTH() / 2;
        int y2 = r2.y() + r2.HEIGHT() / 2;

        // Horizontal portion
        for(int x = Math.min(x1, x2); x <= Math.max(x1, x2) ; x++) {
            world[x][y1] = Tileset.FLOOR;
        }
        // Vertical
        for(int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            world[x2][y] = Tileset.FLOOR;
        }
    }

    /**
     * buildWalls iterates through every tile. If the tile is a floor, we check the surrounding tiles
     * and turn it into a wall if it is NOTHING.
     * @param world the world
     */
    private void buildWalls(TETile[][] world) {
        for(int x = 0 ; x < WIDTH ; x++) {
            for(int y = 0 ; y < HEIGHT ; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    for(int dx = -1; dx <= 1; dx++) { // CITATION: dx and dy idea came from
                        // Google AI
                        for(int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;

                            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT) {
                                if(world[nx][ny] == Tileset.NOTHING) {
                                    world[nx][ny] = Tileset.WALL;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void update() {
        if(currentWindow[avatarX][avatarY] == Tileset.UNLOCKED_DOOR) {
            enterDungeon(worldX, worldY);
        }

        // At the end.
        currentWindow[avatarX][avatarY] = Tileset.AVATAR;
    }
}
