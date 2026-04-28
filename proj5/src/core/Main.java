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
    static Font titleFont = generateCustomFont("Monofett-Regular.ttf");
    static Font monospace = new Font("Monospace", Font.PLAIN, 40);

    public static void main(String[] args) {
        Main game = new Main();
        game.run();
    }
    private void fillBlack() {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.filledRectangle(
                World.WIDTH / 2.0,
                World.HEIGHT / 2.0,
                World.WIDTH / 2.0,
                World.HEIGHT / 2.0
        );
    }

    void run() {
        TERenderer ter = new TERenderer();
        // Initialize with space for HUD
        ter.initialize(World.WIDTH, World.HEIGHT + World.HUD_HEIGHT, 0, 0);

        long seed = 0L;
        StdDraw.setTitle(Main.gameName);
        int menuTime = 0;

        // Menu loop does not need the full TERenderer, but uses StdDraw
        while(seed == 0L) {
            seed = runMainMenu(menuTime);
            menuTime++;
            StdDraw.show();
            StdDraw.pause(10);
        }

        World worldInst = new World(seed);
        HUD hud = new HUD();
        ter.resetFont();

        // Initial render of the world and HUD
        ter.drawTiles(worldInst.currentWindow);
        hud.draw(worldInst);
        StdDraw.show();

        while (true) {
            // Check for player input and update game state
            boolean worldChanged = worldInst.update();

            // The HUD needs to update every frame for smooth mouse tracking
            // So we will redraw both if the world changed OR if the mouse moved.
            // A simple check is to see if the mouse coordinates have changed.
            if (worldChanged || hud.mouseMoved()) {
                ter.drawTiles(worldInst.currentWindow);
                hud.draw(worldInst);
                StdDraw.show();
            }

            StdDraw.pause(20); // A slightly longer pause is fine and reduces CPU usage
        }
    }

    private long runMainMenu(int timeDelta) {
        long seed = 0L;
        double timeDeltaDouble = timeDelta / 9.0;
        double textUpScroll = World.HEIGHT / 10.0 * (Math.min(timeDeltaDouble, 4.0));

        this.fillBlack();
        StdDraw.setPenColor(StdDraw.WHITE);

        StdDraw.setFont(titleFont);
        StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 + textUpScroll, Main.gameName);

        if(timeDeltaDouble >= 4.0) {
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
                        seed = promptForSeed();
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
            this.fillBlack();
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
                            // Fallback
                        }
                    }
                } else if (Character.isDigit(c)) {
                    seedStr += c;
                }
            }
        }
    }

    private static Font generateCustomFont(String ff) {
        Font customFont;
        try {
            File fontFile = new File("proj5/fonts/" + ff);
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont((float) (80f*World.WIDTH/96.0));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            System.out.println("Warning: Custom font not found. Using Serif.");
            customFont = new Font("Serif", Font.BOLD, 80);
        }
        return customFont;
    }
}
