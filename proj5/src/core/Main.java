package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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
    static String gameName = "Stanislaw and the Mujahideen";
    static Font customFont = generateCustomFont("Monofett-Regular.ttf");
    static Font monospace = new Font("Monospace", Font.PLAIN, 40);

    void main() {
        TERenderer ter = new TERenderer();
        ter.initialize(World.WIDTH, World.HEIGHT);

        long seed = 0L;
        //long seed = new Random().nextLong();
        StdDraw.setTitle(Main.gameName);
        int menuTime = 0;

        while(seed == 0L) {
            seed = runMainMenu(customFont, menuTime);
            menuTime++;
            StdDraw.show();
            StdDraw.pause(10);
        }

        World worldInst = new World(seed);
        while (true) {
            ter.resetFont();
            worldInst.update();

            ter.drawTiles(worldInst.currentWindow);
            StdDraw.show();
            StdDraw.pause(10);
            //timeDelta++; // TODO: Make a CPU-agnostic timing system
        }
    }

    private long runMainMenu(Font titleFont, int timeDelta) {
        long seed = 0L;

        timeDelta /= 9; // Slow time for the title
        double textUpScroll = World.HEIGHT / 10.0 * (Math.min(timeDelta, 4));
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);

        StdDraw.setFont(titleFont);
        StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 + textUpScroll, Main.gameName);

        if(timeDelta >= 4) { // Only load the rest of the text once the title is scrolled up
            StdDraw.setFont(monospace);
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0, "(N) New Game");
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 - World.HEIGHT / 10.0, "(L) Load Game");
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 - 2 * World.HEIGHT / 10.0, "(Q) Quit Game");

            if(StdDraw.hasNextKeyTyped()) {
                switch(Character.toLowerCase(StdDraw.nextKeyTyped())) {
                    case 'n':
                        break;
                    case 'l':
                        break;
                    case 'q':
                        System.exit(0);
                }
            }
        }
        return seed;
    }

    // Credits to Gemini for assistance in fonts
    private static Font generateCustomFont(String ff) { // fontFile is in the fonts/ directory

        Font customFont;
        try { // Font selection code was partly provided by Gemini 3.1 Flash-Lite
            File fontFile = new File("proj5/fonts/" + ff); // ff could be, e.g. Monofett-Regular.ttf
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(80f);

            // Register the font with the Graphics Environment
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

        } catch (IOException | FontFormatException e) {
            // Fallback font if the file isn't found or is corrupted
            System.out.println("Warning: Custom font not found. Using Serif.");
            customFont = new Font("Serif", Font.BOLD, 80);
        }

        return customFont;
    }
}
