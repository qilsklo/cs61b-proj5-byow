package core;

// This class is a room in a dungeon

import java.util.Objects;

public final class Room {
    public int x;
    public int y;
    public int WIDTH;
    public int HEIGHT;

    public Room(int x, int y, int WIDTH, int HEIGHT) {
        this.x = x;
        this.y = y;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int WIDTH() {
        return WIDTH;
    }

    public int HEIGHT() {
        return HEIGHT;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Room) obj;
        return this.x == that.x &&
                this.y == that.y &&
                this.WIDTH == that.WIDTH &&
                this.HEIGHT == that.HEIGHT;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, WIDTH, HEIGHT);
    }

    @Override
    public String toString() {
        return "Room[" +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "WIDTH=" + WIDTH + ", " +
                "HEIGHT=" + HEIGHT + ']';
    }


}
