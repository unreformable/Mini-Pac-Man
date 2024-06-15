import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class AI implements Runnable {
    public float position_x;
    public float position_y;

    public int move_direction_x = 0;
    public int move_direction_y = 0;

    public BufferedImage image;

    private Random random;
    private int changeDirectionTimer;
    private int changeDirectionTimeThreshold;

    private Maze maze;

    public AI(float position_x, float position_y, String image_path, Maze maze) {
        this.position_x = position_x;
        this.position_y = position_y;
        this.maze = maze;

        random = new Random();
        changeDirectionTimeThreshold = 100;

        try {
            image = ImageIO.read(new File(image_path));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            final long frameStartNano = System.nanoTime();
            update();
            try {
                final long frameEndNano = System.nanoTime();
                final long sleepTimeNano = 16666667 - (frameEndNano - frameStartNano);
                final long sleepTimeMillis = (long)((float)sleepTimeNano * 1E-6f);
                Thread.sleep(sleepTimeMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        changeDirectionTimer--;

        if (changeDirectionTimer <= 0) {
            chooseRandomDirection();
            changeDirectionTimer = random.nextInt(changeDirectionTimeThreshold) + 50;
        }

        final float move_distance_x = (float) (move_direction_x * 50) * 0.01667f;
        final float move_distance_y = (float) (-move_direction_y * 50) * 0.01667f;

        if (willCollideWithWall(position_x + move_distance_x, position_y + move_distance_y)) {
            chooseRandomDirection();
        } else {
            position_x += move_distance_x;
            position_y += move_distance_y;

            if (position_x + image.getWidth() > maze.image.getWidth()) {
                position_x -= maze.image.getWidth();
            } else if (position_x < 0) {
                position_x += maze.image.getWidth();
            }
        }
    }

    private void chooseRandomDirection() {
        int randomDirection = random.nextInt(5);

        switch (randomDirection) {
            case 0:
                move_direction_x = 0;
                move_direction_y = -1;
                break;
            case 1:
                move_direction_x = 0;
                move_direction_y = 1;
                break;
            case 2:
                move_direction_x = -1;
                move_direction_y = 0;
                break;
            case 3:
                move_direction_x = 1;
                move_direction_y = 0;
                break;
        }
    }

    private boolean willCollideWithWall(float targetX, float targetY) {
        targetX += (image.getWidth() / 2);
        targetY += (image.getHeight() / 2);

        int tiledTargetX = (int) targetX / maze.tile_length;
        int tiledTargetY = (int) targetY / maze.tile_length;

        if (tiledTargetX < 0 || tiledTargetX >= maze.tile_count_x || tiledTargetY < 0 || tiledTargetY >= maze.tile_count_y)
            return true;

        int tileIdx = tiledTargetX + (tiledTargetY * maze.tile_count_x);
        return maze.tilemap[tileIdx] == Tile.WALL;
    }
}