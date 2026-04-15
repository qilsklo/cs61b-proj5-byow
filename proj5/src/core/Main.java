package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;

import static core.World.WIDTH;
import static core.World.HEIGHT;
import java.awt.*;


public class Main {
    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        StdDraw.clear(new Color(0, 0, 0));
        ter.initialize(WIDTH, HEIGHT);
        World worldInst = new World();
        TETile[][] currentWindow = worldInst.currentWindow; // For convenience

        while (true) {
            ter.drawTiles(currentWindow);
            StdDraw.show();
            StdDraw.pause(10);

            worldInst.update();
        }




    }
}
