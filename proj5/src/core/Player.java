package core;

public class Player {
    public double x, y;
    public double angle;
    public double pitch = 0; // Vertical look angle
    public double dirX, dirY;
    public double planeX, planeY;

    private static final double FOV = Math.toRadians(66); // 66-degree FOV

    public Player(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        updateVectors();
    }

    public void updateVectors() {
        dirX = Math.cos(angle);
        dirY = Math.sin(angle);
        
        double planeScale = Math.tan(FOV / 2.0);
        planeX = -dirY * planeScale;
        planeY = dirX * planeScale;
    }

    public void rotate(double rotationSpeed) {
        angle += rotationSpeed;
        updateVectors();
    }

    public void changePitch(double amount) {
        pitch += amount;
        // Clamp pitch to prevent looking too far up or down
        pitch = Math.max(-Math.PI / 4, Math.min(Math.PI / 4, pitch));
    }

    public void move(double moveSpeed, World world, char direction) {
        double moveX = 0;
        double moveY = 0;

        if (direction == 'w') {
            moveX = dirX * moveSpeed;
            moveY = dirY * moveSpeed;
        } else if (direction == 's') {
            moveX = -dirX * moveSpeed;
            moveY = -dirY * moveSpeed;
        }

        double newX = x + moveX;
        double newY = y + moveY;

        if (!world.isWall((int) newX, (int) y)) {
            x = newX;
        }
        if (!world.isWall((int) x, (int) newY)) {
            y = newY;
        }
    }
}
