package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TETile;

import java.awt.Color;
import java.awt.Font;

public class HUD {
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private static final Font HUD_FONT = new Font("Monospace", Font.BOLD, 20);

    /**
     * Checks if the mouse has moved since the last frame.
     * @return true if the mouse has moved, false otherwise.
     */
    public boolean mouseMoved() {
        double currentMouseX = StdDraw.mouseX();
        double currentMouseY = StdDraw.mouseY();
        if (currentMouseX != lastMouseX || currentMouseY != lastMouseY) {
            lastMouseX = currentMouseX;
            lastMouseY = currentMouseY;
            return true;
        }
        return false;
    }

    /**
     * Draws the HUD on the screen.
     * @param world The world object to get information from.
     */
    public void draw(World world) {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();

        String tileDescription = "Out of bounds";
        if (mouseX >= 0 && mouseX < World.WIDTH && mouseY >= 0 && mouseY < World.HEIGHT) {
            TETile tile = world.currentWindow[mouseX][mouseY];
            if (tile != null) {
                tileDescription = tile.description();
            }
        }
        
        // Clear the HUD area first
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(World.WIDTH / 2.0, World.HEIGHT + (World.HUD_HEIGHT / 2.0), World.WIDTH / 2.0, World.HUD_HEIGHT / 2.0);

        // Draw the text
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(HUD_FONT);
        StdDraw.textLeft(1, World.HEIGHT + World.HUD_HEIGHT - 2, "Tile: " + tileDescription);
    }
}
