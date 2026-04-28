package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;

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

    public static void main(String[] args) {
        Main game = new Main();
        game.run();
    }

    private void fillBlack() {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.filledRectangle(World.WIDTH / 2.0, World.HEIGHT / 2.0, World.WIDTH / 2.0, World.HEIGHT / 2.0);
    }

    void run() {
        TERenderer ter = new TERenderer();
        ter.initialize(World.WIDTH, World.HEIGHT);

        long seed = 0L;
        StdDraw.setTitle(Main.gameName);
        int menuTime = 0;

        while(seed == 0L) {
            seed = runMainMenu(customFont, menuTime);
            menuTime++;
            StdDraw.show();
            StdDraw.pause(10);
        }

        World worldInst = new World(seed);
        ter.resetFont();
        
        // Initial render
        worldInst.update();
        ter.drawTiles(worldInst.currentWindow);
        StdDraw.show();

        while (true) {
            boolean dirty = worldInst.update(); // dirty improves efficiency, since you don't have to redraw the whole screen.
            if (dirty) {
                ter.drawTiles(worldInst.currentWindow);
                StdDraw.show();
            }

            StdDraw.pause(10);
        }
    }

    private long runMainMenu(Font titleFont, int timeDelta) {
        long seed = 0L;

        double timeDeltaDouble = timeDelta / 9.0; // Smooth time for the title
        double textUpScroll = World.HEIGHT / 10.0 * (Math.min(timeDeltaDouble, 4.0));
        
        fillBlack();
        StdDraw.setPenColor(StdDraw.WHITE);

        StdDraw.setFont(titleFont);
        StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 + textUpScroll, Main.gameName);

        if(timeDeltaDouble >= 4.0) { // Only load the rest of the text once the title is scrolled up
            StdDraw.setFont(monospace);
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0, "(N) New Game");
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 - World.HEIGHT / 10.0, "(L) Load Game");
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 - 2 * World.HEIGHT / 10.0, "(Q) Quit Game");

            if(StdDraw.hasNextKeyTyped()) {
                switch(Character.toLowerCase(StdDraw.nextKeyTyped())) {
                    case 'n':
                        seed = promptForSeed();
                        break;
                    case 'l':
                        seed = promptForSeed(); // Do the same as generating from seed for now
                        break;
                    case 'q':
                        System.exit(0);
                }
            }
        }
        return seed;
    }

    private long promptForSeed() {
        String seedStr = "";
        while (true) {
            fillBlack();
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.setFont(monospace);
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 + 5, "Enter Seed:");
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0, seedStr);
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 - 5, "(Press S to start)");
            StdDraw.show();
            StdDraw.pause(20);

            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (Character.toLowerCase(c) == 's') {
                    if (!seedStr.isEmpty()) {
                        try {
                            return Long.parseLong(seedStr);
                        } catch (NumberFormatException e) {
                            // Fallback if parsing fails
                        }
                    }
                } else if (Character.isDigit(c)) {
                    seedStr += c;
                }
            }
        }
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
