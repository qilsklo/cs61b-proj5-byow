package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;

import static core.World.WIDTH;
import static core.World.HEIGHT;
import java.awt.*;

/*
Your seeds are:
4733073195478072994
6016122649337190954
6752907471955227960
3440102649857143184
2550606313251798898
 */


public class Main {
    public static void main(String[] args) {
        TERenderer ter = new TERenderer();

        StdDraw.clear(new Color(0, 0, 0));

        ter.initialize(WIDTH, HEIGHT);

        long seed = 4733073195478072994L;

        World worldInst = new World(seed);
        TETile[][] currentWindow = worldInst.currentWindow; // For convenience

        while (true) {
            ter.drawTiles(currentWindow);
            StdDraw.show();
            StdDraw.pause(10);

            worldInst.update();
        }




    }
}
