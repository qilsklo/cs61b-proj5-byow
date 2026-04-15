package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.*;

/*
World is not static - you can make a World() object.
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

    public static final int WIDTH = 128; // Better to make this odd for centering
    public static final int HEIGHT = 128;

    private int avatarX, avatarY; // Relative to the current screen (only relevant in dungeons)
    private int startX, startY, endX, endY; // Implementing toroidal array
    public long worldX, worldY; // Top left corner of the screen to set a core.World origin

    public TETile[][] currentWindow;

    public World() {
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
    }

    /**
     * Used when entering a dungeon. Note: in the highest level of federation, a dungeon need not
     * be its drawn size; you can have a 1x1 dungeon icon, for instance (though this would suck)
     * @param seed just the world seed, nothing special
     * @param X absolute X coordinate of the dungeon DOOR with respect to the highest level of world federation
     * @param Y absolute Y coord of the dungeon door.
     *          X and Y ensure that all dungeons are unique.
     */
    public void enterDungeon(long seed, long X, long Y) {

        currentWindow = generateDungeon(seed, X, Y);

        // TODO: Maybe a nice transition.
    }
    private TETile[][] generateDungeon(long seed, long X, long Y) {
        // Generates a dungeon given a seed, X, Y, HEIGHT, WIDTH.
        return null;
    }

    public void update() {
    }
}
