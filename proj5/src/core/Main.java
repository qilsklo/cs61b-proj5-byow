package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {
    static String gameName = "Stanislaw and the Mujahideen";
    static Font titleFont = generateCustomFont("Monofett-Regular.ttf");
    static Font monospace = new Font("Monospace", Font.PLAIN, 40);

    private StringBuilder inputHistory = new StringBuilder();
    private boolean colonTyped = false;

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
        ter.initialize(World.WIDTH, World.HEIGHT + World.HUD_HEIGHT, 0, 0);
        StdDraw.setTitle(Main.gameName);

        String startChoice = runMainMenu();
        World worldInst;

        if (startChoice.equals("quit")) {
            return;
        }

        if (startChoice.equals("load")) {
            loadState();
            worldInst = createWorldFromHistory(inputHistory.toString());
        } else { // "new"
            String seedString = promptForSeed();
            inputHistory.append("n").append(seedString).append("s");
            worldInst = new World(Long.parseLong(seedString));
        }

        HUD hud = new HUD();
        ter.resetFont();

        // Main Game Loop
        while (true) {
            // Always render first
            ter.drawTiles(worldInst.currentWindow);
            hud.draw(worldInst);
            StdDraw.show();

            // Process new keyboard input
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toLowerCase(StdDraw.nextKeyTyped());
                inputHistory.append(c);

                if (handleSaveQuitSequence(c)) {
                    break; // Exit loop to quit
                }

                worldInst.handleKey(c); // Use handleKey instead of update loop
            }
            StdDraw.pause(10);
        }
    }

    private String runMainMenu() {
        int menuTime = 0;
        while (true) {
            double timeDeltaDouble = menuTime / 9.0;
            double textUpScroll = World.HEIGHT / 10.0 * (Math.min(timeDeltaDouble, 4.0));

            this.fillBlack();
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.setFont(titleFont);
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 + textUpScroll, Main.gameName);

            if (timeDeltaDouble >= 4.0) {
                StdDraw.setFont(monospace);
                StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0, "(N) New Game");
                StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 - World.HEIGHT / 10.0, "(L) Load Game");
                StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 - 2 * World.HEIGHT / 10.0, "(Q) Quit Game");
            }

            if (StdDraw.hasNextKeyTyped()) {
                char choice = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (choice == 'n') return "new";
                if (choice == 'l') return "load";
                if (choice == 'q') {
                    System.exit(0);
                }
            }
            menuTime++;
            StdDraw.show();
            StdDraw.pause(10);
        }
    }

    private String promptForSeed() {
        StringBuilder seedStr = new StringBuilder();
        while (true) {
            this.fillBlack();
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.setFont(monospace);
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 + 5, "Enter Seed:");
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0, seedStr.toString());
            StdDraw.text(World.WIDTH / 2.0, World.HEIGHT / 2.0 - 5, "(Press S to start)");
            StdDraw.show();
            StdDraw.pause(20);

            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (Character.toLowerCase(c) == 's') {
                    if (!seedStr.isEmpty()) {
                        return seedStr.toString();
                    }
                } else if (Character.isDigit(c)) {
                    seedStr.append(c);
                }
            }
        }
    }
    
    private World createWorldFromHistory(String history) {
        int sIndex = history.toLowerCase().indexOf('s');
        if (sIndex == -1) {
            // Should not happen with valid save files
            return new World(12345); 
        }
        String seedStr = history.substring(1, sIndex);
        long seed = Long.parseLong(seedStr);
        String moves = history.substring(sIndex + 1);

        World newWorld = new World(seed);
        for (char move : moves.toCharArray()) {
            newWorld.handleKey(move);
        }
        return newWorld;
    }

    private boolean handleSaveQuitSequence(char c) {
        if (colonTyped && c == 'q') {
            saveState();
            System.exit(0);
            return true;
        }
        colonTyped = (c == ':');
        return false;
    }

    private void saveState() {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream("save.txt"))) {
            writer.print(inputHistory.toString());
        } catch (FileNotFoundException e) {
            System.out.println("Error: Could not save game state. " + e.getMessage());
        }
    }

    private void loadState() {
        File f = new File("save.txt");
        if (f.exists()) {
            try (Scanner fs = new Scanner(f)) {
                if (fs.hasNext()) {
                    inputHistory = new StringBuilder(fs.next());
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error: Could not load game state. " + e.getMessage());
            }
        }
    }

    // Gemini helped make generateCustomFont
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
