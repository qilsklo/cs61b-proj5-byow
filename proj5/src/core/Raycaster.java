package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.Color;
import java.util.Random;

// Citation: Gemini 2.5 Pro helped design the Raycaster,
// but the human team wrote over 80% of the code in this file
// Debugging help was provided by a model, but the model was
// specifically instructed to provide hints rather than actual
// code.
public class Raycaster {

    private static final int STRIDE = 1; // Set to 1 for smoothest rendering
    private static final double BOB_AMPLITUDE = .5; // Controls how much the view bobs
    private static final double BOB_FREQUENCY = 1; // Controls the speed of the bobbing
    private static final double PULSE_SPEED = 0.002; // Controls the speed of the scanner pulse
    private static final double PULSE_WIDTH = 2.0; // Controls the width of the scanner pulse

    public static void render(World world, Player player, double walkTimer) {
        int screenWidth = World.WIDTH;
        int screenHeight = World.HEIGHT;

        // Adjust horizon based on player's pitch (vertical look)
        // Invert the pitch effect so looking down (negative pitch) raises the horizon, showing more floor.
        double yOffset = Math.sin(walkTimer * BOB_FREQUENCY) * BOB_AMPLITUDE;
        double horizon = screenHeight / 2.0 + yOffset - player.pitch * screenHeight; // Changed + to -

        if (horizon < 0) horizon = 0; //clamp 'em
        if (horizon > screenHeight) horizon = screenHeight;

        // Environment Prep: Draw Floor and Ceiling
        // Draw floor from bottom of screen to horizon
        StdDraw.setPenColor(Color.DARK_GRAY); // Floor color
        StdDraw.filledRectangle(screenWidth / 2.0, horizon / 2.0, screenWidth / 2.0, horizon / 2.0);

        // Draw ceiling from horizon to top of screen
        StdDraw.setPenColor(Color.BLACK); // Ceiling color
        StdDraw.filledRectangle(screenWidth / 2.0, (horizon + screenHeight) / 2.0, screenWidth / 2.0, (screenHeight - horizon) / 2.0);

        for (int x = 0; x < screenWidth; x += STRIDE) {
            double cameraX = 2 * x / (double) screenWidth - 1;
            double rayDirX = player.dirX + player.planeX * cameraX;
            double rayDirY = player.dirY + player.planeY * cameraX;

            int mapX = (int) player.x;
            int mapY = (int) player.y;

            double sideDistX, sideDistY;
            double deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1 / rayDirX);
            double deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1 / rayDirY);
            double perpWallDist;

            int stepX, stepY;
            boolean hit = false;
            int side = 0;

            if (rayDirX < 0) {
                stepX = -1;
                sideDistX = (player.x - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - player.x) * deltaDistX;
            }
            if (rayDirY < 0) {
                stepY = -1;
                sideDistY = (player.y - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - player.y) * deltaDistY;
            }

            TETile wallTile = null;
            while (!hit) {
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                if (world.isWall(mapX, mapY)) {
                    hit = true;
                    wallTile = world.getTile(mapX, mapY);
                }
            }

            if (side == 0) {
                perpWallDist = (sideDistX - deltaDistX);
            } else {
                perpWallDist = (sideDistY - deltaDistY);
            }
            if (perpWallDist <= 0) perpWallDist = 0.1; // Preventing div by zero

            int lineHeight = (int) (screenHeight / perpWallDist);

            if (wallTile == null) {
                wallTile = Tileset.WALL;
            }
            Color baseColor = wallTile.getBackgroundColor();

            // --- Shading and Effects ---
            double brightness = Math.max(0, Math.min(1, 10.0 / perpWallDist));
            if (side == 1) {
                brightness *= 0.8;
            }

            // 1. "Scanner" or "Pulse" Effect
            long time = System.currentTimeMillis();
            double pulseDist = (time * PULSE_SPEED) % 20; // Pulse travels 20 units and resets
            double pulseFactor = Math.abs(perpWallDist - pulseDist);
            if (pulseFactor < PULSE_WIDTH) {
                brightness += (1.0 - pulseFactor / PULSE_WIDTH) * 0.3; // Additive brightness boost
            }

            int red = (int) (baseColor.getRed() * brightness);
            int green = (int) (baseColor.getGreen() * brightness);
            int blue = (int) (baseColor.getBlue() * brightness);

            long tileNoiseSeed = world.seed + (long) mapX * 31 + (long) mapY * 17;
            Random tileRandom = new Random(tileNoiseSeed);
            int noiseAmount = 10;
            red += tileRandom.nextInt(noiseAmount * 2) - noiseAmount;
            green += tileRandom.nextInt(noiseAmount * 2) - noiseAmount;
            blue += tileRandom.nextInt(noiseAmount * 2) - noiseAmount;

            red = Math.min(255, Math.max(0, red));
            green = Math.min(255, Math.max(0, green));
            blue = Math.min(255, Math.max(0, blue));
            Color finalColor = new Color(red, green, blue);

            // --- Drawing ---
            // Main wall slice
            StdDraw.setPenColor(finalColor);
            StdDraw.filledRectangle(x + (STRIDE / 2.0), horizon, STRIDE / 2.0, lineHeight / 2.0);

            // 3. Dynamic "Wall Decals" or Trim
            double trimHeight = Math.max(1, lineHeight * 0.02); // 2% of wall height, at least 1 pixel
            StdDraw.setPenColor(Color.BLACK);
            // Bottom Trim ("Baseboard")
            StdDraw.filledRectangle(x + (STRIDE / 2.0), horizon - lineHeight / 2.0 + trimHeight / 2.0, STRIDE / 2.0, trimHeight / 2.0);
            // Top Trim ("Crown Molding")
            StdDraw.filledRectangle(x + (STRIDE / 2.0), horizon + lineHeight / 2.0 - trimHeight / 2.0, STRIDE / 2.0, trimHeight / 2.0);
        }
    }
}
