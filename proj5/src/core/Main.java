package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import java.awt.*;
import java.util.Random;

/*
Your seeds are:
4733073195478072994
6016122649337190954
6752907471955227960
3440102649857143184
2550606313251798898
 */


public class Main {
    void main() {
        TERenderer ter = new TERenderer();

        StdDraw.clear(new Color(0, 0, 0));

        ter.initialize(World.WIDTH, World.HEIGHT);

        long seed = 2550606313251798898L;

        //long seed = new Random().nextLong();

        World worldInst = new World(seed);

        while (true) {
            ter.drawTiles(worldInst.currentWindow);
            StdDraw.show();
            StdDraw.pause(10);

            worldInst.update();
        }




    }
}
