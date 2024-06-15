import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.PriorityQueue;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.awt.Dimension;
import java.util.List;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Font;

public class Game extends JPanel implements Runnable
{
    private JFrame window;
    private Thread game_thread;

    private Input input;

    private Input input2;

    private Pacman pacman;
    private Maze maze;
    private int score = 0;
    private int highScore = 0;
    private AI AI;

    public Game()
    {
        input = new Input();
        input2 = new Input();

        pacman = new Pacman(10, 117, "assets/pacman.png");
        maze = new Maze(8, "assets/maze.png");
        AI = new AI(200, 117, "assets/ghost.png", maze);
        Thread AIThread = new Thread(AI);
        AIThread.start();

        this.addKeyListener(input);
        this.addMouseListener(input);
        this.addKeyListener(input2);
        this.addMouseListener(input2);

        window = new JFrame();
        window.setTitle("Pac-Man");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        this.setPreferredSize(new Dimension(maze.image.getWidth(), maze.image.getHeight()+ 60));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // draw in back buffer while showing the other one
        
        this.setFocusable(true);
        window.add(this);

        window.pack();
        window.setLocationRelativeTo(null); // always after pack()
        window.setVisible(true);

        game_thread = new Thread(this);
        game_thread.start();
    }

    public void run() {
        long lastScoreUpdateTime = System.nanoTime();
        double nsPerSecond = 1_000_000_000.0;

        while (game_thread != null) {
            final long frameStartNano = System.nanoTime();

            update();
            this.repaint();

            final long frameEndNano = System.nanoTime();
            final long sleepTimeNano = 16_666_667 - (frameEndNano - frameStartNano); // 60 FPS
            final long sleepTimeMillis = (long) ((float) sleepTimeNano * 1E-6f);
            try {
                Thread.sleep(sleepTimeMillis);
            } catch (Exception e) {
                e.printStackTrace();
            }

            long now = System.nanoTime();
            double deltaTime = (now - lastScoreUpdateTime) / nsPerSecond;
            if (deltaTime >= 1.0) {
                score += 10;
                lastScoreUpdateTime = now;
            }
        }
    }

    private void update()
    {

        if((input.direction_x != 0 || input.direction_y != 0)
                && (input.direction_x != -pacman.move_direction_x || input.direction_y != -pacman.move_direction_y))
        {
            pacman.last_move_direction_x = pacman.move_direction_x;
            pacman.last_move_direction_y = pacman.move_direction_y;

            pacman.move_direction_x = input.direction_x;
            pacman.move_direction_y = input.direction_y;
        }


        final float move_distance_x = (float)( pacman.move_direction_x * 50) * 0.01667f;
        final float move_distance_y = (float)(-pacman.move_direction_y * 50) * 0.01667f;

        if(!pacmanWillCollideWithWall(pacman.position_x + move_distance_x, pacman.position_y))
        {
            pacman.position_x += move_distance_x;

            if(pacman.position_x + pacman.image.getWidth() > maze.image.getWidth())
            {
                pacman.position_x -= maze.image.getWidth();
            }
            else if(pacman.position_x < 0)
            {
                pacman.position_x += maze.image.getWidth();
            }
        }
        if(!pacmanWillCollideWithWall(pacman.position_x, pacman.position_y + move_distance_y))
        {
            pacman.position_y += move_distance_y;
        }
        if (score > highScore) {
            highScore = score;
        }
    }


    private boolean pacmanWillCollideWithWall(float targetX, float targetY)
    {
        targetX += (pacman.image.getWidth() / 2);
        targetY += (pacman.image.getHeight() / 2);

        final int tiledTargetX = (int)targetX / maze.tile_length;
        final int tiledTargetY = (int)targetY / maze.tile_length;

        if(tiledTargetX <= 0 || tiledTargetX >= maze.tile_count_x
        || tiledTargetY <= 0 || tiledTargetY >= maze.tile_count_y)
            return false;

        final int tileIdx = tiledTargetX + (tiledTargetY * maze.tile_count_x);


        //System.out.print(tiledTargetX);
        //System.out.print(" ");
        //System.out.println(tiledTargetY);
        //System.out.println(maze.tilemap[tileIdx]);
        //System.out.println("");


        return (maze.tilemap[tileIdx] == Tile.WALL);
    }
    private boolean AIWillCollideWithWall(float targetX, float targetY)
    {
        targetX += (AI.image.getWidth() / 2);
        targetY += (AI.image.getHeight() / 2);

        final int tiledTargetX = (int)targetX / maze.tile_length;
        final int tiledTargetY = (int)targetY / maze.tile_length;

        if(tiledTargetX <= 0 || tiledTargetX >= maze.tile_count_x
                || tiledTargetY <= 0 || tiledTargetY >= maze.tile_count_y)
            return false;

        final int tileIdx = tiledTargetX + (tiledTargetY * maze.tile_count_x);


        //System.out.print(tiledTargetX);
        //System.out.print(" ");
        //System.out.println(tiledTargetY);
        //System.out.println(maze.tilemap[tileIdx]);
        //System.out.println("");


        return (maze.tilemap[tileIdx] == Tile.WALL);
    }

    public void paintComponent(Graphics _g)
    {
        super.paintComponent(_g);

        Graphics2D g = (Graphics2D)_g;

        //g.setColor(Color.WHITE);
        //g.fillRect(0, 0, 300, 200);

        g.drawImage(maze.image, 0, 0, maze.image.getWidth(), maze.image.getHeight(), null);

        double pacmanRotation = 0;
        if(     pacman.move_direction_x ==  1) pacmanRotation = Math.toRadians(180);
        else if(pacman.move_direction_x == -1) pacmanRotation = Math.toRadians(0);
        else if(pacman.move_direction_y ==  1) pacmanRotation = Math.toRadians(90);
        else if(pacman.move_direction_y == -1) pacmanRotation = Math.toRadians(270);
        //g.rotate(pacmanRotation, pacman.position_x + pacman.image.getWidth()/2, pacman.position_y + pacman.image.getHeight()/2);
        g.drawImage(pacman.image, (int)pacman.position_x, (int)pacman.position_y, pacman.image.getWidth(), pacman.image.getHeight(), null);
        if(pacman.position_x <= 0)
            g.drawImage(pacman.image, (int)pacman.position_x + maze.image.getWidth(), (int)pacman.position_y, pacman.image.getWidth(), pacman.image.getHeight(), null);
        if(pacman.position_x + pacman.image.getWidth() >= maze.image.getWidth())
            g.drawImage(pacman.image, (int)pacman.position_x - maze.image.getWidth(), (int)pacman.position_y, pacman.image.getWidth(), pacman.image.getHeight(), null);

        double AIRotation = 0;
        //if(     AI.move_direction_x ==  1) AIRotation = Math.toRadians(180);
        //else if(AI.move_direction_x == -1) AIRotation = Math.toRadians(0);
        //else if(AI.move_direction_y ==  1) AIRotation = Math.toRadians(90);
        //else if(AI.move_direction_y == -1) AIRotation = Math.toRadians(270);
        //g.rotate(AIRotation, AI.position_x + AI.image.getWidth()/2, AI.position_y + AI.image.getHeight()/2);
        g.drawImage(AI.image, (int)AI.position_x, (int)AI.position_y, AI.image.getWidth(), AI.image.getHeight(), null);
        if(AI.position_x <= 0)
            g.drawImage(AI.image, (int)AI.position_x + maze.image.getWidth(), (int)AI.position_y, AI.image.getWidth(), AI.image.getHeight(), null);
        if(AI.position_x + AI.image.getWidth() >= maze.image.getWidth())
            g.drawImage(AI.image, (int)AI.position_x - maze.image.getWidth(), (int)AI.position_y, AI.image.getWidth(), AI.image.getHeight(), null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, maze.image.getHeight() + 20);
        g.drawString("High Score: " + highScore, 10, maze.image.getHeight() + 40);

        g.dispose();// always at the end
    }
}