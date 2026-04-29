package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.Tileset;

import java.awt.Point;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static String gameName = "Stanislaw and the Mujahideen";
    static Font titleFont = generateCustomFont("Monofett-Regular.ttf");
    static Font monospace = new Font("Monospace", Font.PLAIN, 40);

    private StringBuilder inputHistory = new StringBuilder();
    private boolean colonTyped = false;
    private List<Point> path = null;
    private Point targetTile = null;
    private int mouseCooldown = 0;
    private MusicPlayer musicPlayer = new MusicPlayer();

    private boolean is3DMode = false;
    private Player player;
    private static final double MOVE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 0.05;
    private static final double MOUSE_SENSITIVITY = 0.2; // Reverted to original value
    private int lastMouseX;
    private int lastMouseY;
    private double walkTimer = 0; // For view bobbing

    public static void main(String[] args) {
        Main game = new Main();
        game.run();
    }

    private void fillBlack() {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.filledRectangle(
                World.WIDTH / 2.0,
                (World.HEIGHT + World.HUD_HEIGHT) / 2.0, // Adjust center Y to cover HUD area
                World.WIDTH / 2.0,
                (World.HEIGHT + World.HUD_HEIGHT) / 2.0 // Adjust height to cover HUD area
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

        // Initialize player after world is created
        player = new Player(worldInst.getAvatarPosition().x + 0.5, worldInst.getAvatarPosition().y + 0.5, Math.PI / 2);

        musicPlayer.stopMusic();
        Random random = new Random();
        int musicIndex = random.nextInt(3);
        musicPlayer.playMusic("proj5/music/music_" + musicIndex + ".wav", true);

        HUD hud = new HUD();
        ter.resetFont();

        // Main Game Loop
        while (true) {
            this.fillBlack(); // Clear screen at the beginning of each frame

            // Render the world and HUD
            if (is3DMode) {
                Raycaster.render(worldInst, player, walkTimer);
            } else {
                ter.drawTiles(worldInst.currentWindow);
                hud.draw(worldInst);

                // Visualize the path if it exists
                if (path != null) {
                    visualizePath();
                }

                // Draw the avatar explicitly to ensure it's on top
                Point avatarPos = worldInst.getAvatarPosition();
                Tileset.AVATAR.draw(avatarPos.x, avatarPos.y);
            }

            StdDraw.show();

            // Process single-press keyboard events
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toLowerCase(StdDraw.nextKeyTyped());
                inputHistory.append(c);

                if (handleSaveQuitSequence(c)) {
                    break; // Exit loop to quit
                }

                if (c == 'p') { // Toggle 3D mode
                    is3DMode = !is3DMode;
                    if (is3DMode) {
                        player.x = worldInst.getAvatarPosition().x + 0.5;
                        player.y = worldInst.getAvatarPosition().y + 0.5;
                        player.updateVectors();
                        lastMouseX = (int) StdDraw.mouseX();
                        lastMouseY = (int) StdDraw.mouseY();
                    }
                } else if (!is3DMode) {
                    worldInst.handleKey(c);
                }
            }

            // Process continuous-press keyboard and mouse input in 3D mode
            if (is3DMode) {
                if (StdDraw.isKeyPressed(87)) { // W key
                    player.move(MOVE_SPEED, worldInst, 'w');
                    walkTimer += 1;
                }
                if (StdDraw.isKeyPressed(83)) { // S key
                    player.move(MOVE_SPEED, worldInst, 's');
                    walkTimer += 1;
                }
                if (StdDraw.isKeyPressed(65)) { // A key
                    player.rotate(-ROTATION_SPEED);
                }
                if (StdDraw.isKeyPressed(68)) { // D key
                    player.rotate(ROTATION_SPEED);
                }
                if (StdDraw.isKeyPressed(81)) { // Q key - Look Up
                    player.changePitch(ROTATION_SPEED);
                }
                if (StdDraw.isKeyPressed(69)) { // E key - Look Down
                    player.changePitch(-ROTATION_SPEED);
                }
                handle3DMouseInput();
            } else {
                // Process mouse input in 2D mode
                handleMouseInput(worldInst);
            }

            // Decrement cooldown
            if (mouseCooldown > 0) {
                mouseCooldown--;
            }

            StdDraw.pause(20);
        }
    }

    private void handle3DMouseInput() {
        int currentMouseX = (int) StdDraw.mouseX();
        int currentMouseY = (int) StdDraw.mouseY();

        int dx = currentMouseX - lastMouseX;
        int dy = currentMouseY - lastMouseY;

        // Rotate player horizontally (yaw)
        player.rotate(dx * MOUSE_SENSITIVITY);
        // Adjust player pitch (vertical look)
        player.changePitch(-dy * MOUSE_SENSITIVITY); // Invert dy for natural mouse movement

        lastMouseX = currentMouseX;
        lastMouseY = currentMouseY;
    }

    private void handleMouseInput(World world) {
        if (mouseCooldown == 0 && StdDraw.isMousePressed()) {
            int x = (int) StdDraw.mouseX();
            int y = (int) StdDraw.mouseY();
            Point clickedTile = new Point(x, y);

            if (targetTile != null && targetTile.equals(clickedTile)) {
                // Second click on the same tile, animate movement
                animateAvatar(world);
                path = null;
                targetTile = null;
            } else {
                // First click or new tile click
                Point start = world.getAvatarPosition();
                if (world.isTraversable(x, y)) {
                    path = Pathfinder.findPath(world, start, clickedTile);
                    if (path != null && !path.isEmpty()) {
                        targetTile = clickedTile;
                    } else {
                        path = null;
                        targetTile = null;
                    }
                }
            }
            mouseCooldown = 20; // Set cooldown to prevent immediate re-triggering
        }
    }

    private void visualizePath() {
        StdDraw.setPenColor(Color.YELLOW);
        for (Point p : path) {
            StdDraw.filledCircle(p.x + 0.5, p.y + 0.5, 0.3);
        }
    }

    private void animateAvatar(World world) {
        if (path == null) {
            return;
        }

        TERenderer ter = new TERenderer();
        HUD hud = new HUD();

        for (int i = 0; i < path.size(); i++) {
            Point current = world.getAvatarPosition();
            Point next = path.get(i);

            int dx = next.x - current.x;
            int dy = next.y - current.y;

            char move = 0;
            if (dx == 1) move = 'd';
            else if (dx == -1) move = 'a';
            else if (dy == 1) move = 'w';
            else if (dy == -1) move = 's';

            if (move != 0) {
                world.handleKey(move);
            }

            // Redraw everything each frame of the animation
            ter.drawTiles(world.currentWindow);
            hud.draw(world);

            // Draw remaining path
            if (i < path.size() - 1) {
                StdDraw.setPenColor(Color.YELLOW);
                for (int j = i + 1; j < path.size(); j++) {
                    Point p = path.get(j);
                    StdDraw.filledCircle(p.x + 0.5, p.y + 0.5, 0.3);
                }
            }
            
            // Draw avatar
            Point avatarPos = world.getAvatarPosition();
            Tileset.AVATAR.draw(avatarPos.x, avatarPos.y);

            StdDraw.show();
            StdDraw.pause(100);
        }
    }

    private String runMainMenu() {
        musicPlayer.playMusic("proj5/music/music_0.wav", true);
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
