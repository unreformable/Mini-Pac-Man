import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.Dimension;

import javax.swing.*;

import java.awt.Graphics;

import java.awt.Graphics2D;

import java.awt.Image;

public class Pacman extends JPanel implements KeyListener, MouseListener, Runnable
{
    private final int MAZE_TILE_LENGTH = 8;
    private int MAZE_TILE_COUNT_X; // these 3 variables are initalized from maze image
    private int MAZE_TILE_COUNT_Y;
    private int MAZE_TILEMAP[];
    private final int TILE_EMPTY = 0;
    private final int TILE_WALL  = 1;

    private BufferedImage mazeImage;
    private BufferedImage pacmanImage;

    private JFrame window;

    private Thread gameThread;

    private boolean keyUp = false;
    private boolean keyDown = false;
    private boolean keyLeft = false;
    private boolean keyRight = false;

    int pacmanDirectionX = 0; // -1=left, 1=right
    int pacmanDirectionY = 0; // -1=down, 1=up

    float pacmanX; // matches left-top corner of pacman image
    float pacmanY;

    public Pacman()
    {
        loadImages();

        window = new JFrame();
        window.setTitle("Pac-Man");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        this.setPreferredSize(new Dimension(mazeImage.getWidth(), mazeImage.getHeight()));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // draw in back buffer while showing the other one
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.setFocusable(true);
        window.add(this);

        window.pack();
        window.setLocationRelativeTo(null); // always after pack()
        window.setVisible(true);


        initalize_maze_tilemap();

        pacmanX = pacmanImage.getWidth() * 2;
        pacmanY = 117;

        gameThread = new Thread(this);
        gameThread.start();
    }

    private void loadImages()
    {
        try
        {
            mazeImage = ImageIO.read(new File("assets/maze.png"));
            pacmanImage = ImageIO.read(new File("assets/pacman.png"));
        }
        catch(IOException e) {}
    }

    private void initalize_maze_tilemap()
    {
        assert ((mazeImage.getWidth() % MAZE_TILE_LENGTH) == 0);
        assert ((mazeImage.getHeight() % MAZE_TILE_LENGTH) == 0);

        MAZE_TILE_COUNT_X = mazeImage.getWidth() / MAZE_TILE_LENGTH;
        MAZE_TILE_COUNT_Y = mazeImage.getHeight() / MAZE_TILE_LENGTH;

        MAZE_TILEMAP = new int[MAZE_TILE_COUNT_X * MAZE_TILE_COUNT_Y];
        
        int i = 0;
        for(int y = 0; y < MAZE_TILE_COUNT_Y; y++)
        {
            for(int x = 0; x < MAZE_TILE_COUNT_X; x++)
            {
                final int firstX = x * MAZE_TILE_LENGTH;
                final int firstY = y * MAZE_TILE_LENGTH;
                final int lastX = (x + 1) * MAZE_TILE_LENGTH - 1;
                final int lastY = (y + 1) * MAZE_TILE_LENGTH - 1;

                int blueValue = 0;
                for(int yy = firstY; yy <= lastY; yy++)
                {
                    for(int xx = firstX; xx <= lastX; xx++)
                    {
                        blueValue += new Color(mazeImage.getRGB(xx, yy)).getBlue();
                    }
                }

                //System.out.print(blueValue % 3);
                //System.out.print(" ");

                MAZE_TILEMAP[i++] = (blueValue == 0 ? TILE_EMPTY : TILE_WALL);
            }
            //System.out.println(" ");
        }
    }

    public void keyPressed(KeyEvent e)
    {
        final int key = e.getKeyCode();

        if(key == KeyEvent.VK_UP    || key == KeyEvent.VK_W) { keyUp = true;    pacmanDirectionX =  0; pacmanDirectionY =  1; }
        if(key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_S) { keyDown = true;  pacmanDirectionX =  0; pacmanDirectionY = -1; }
        if(key == KeyEvent.VK_LEFT  || key == KeyEvent.VK_A) { keyLeft = true;  pacmanDirectionX = -1; pacmanDirectionY =  0; }
        if(key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) { keyRight = true; pacmanDirectionX =  1; pacmanDirectionY =  0; }
    }
    public void keyReleased(KeyEvent e)
    {
        final int key = e.getKeyCode();

        if(key == KeyEvent.VK_UP    || key == KeyEvent.VK_W) keyUp = false;
        if(key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_S) keyDown = false;
        if(key == KeyEvent.VK_LEFT  || key == KeyEvent.VK_A) keyLeft = false;
        if(key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) keyRight = false;
    }
    public void keyTyped(KeyEvent e) {}

    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void run()
    {
        while(gameThread != null)
        {
            final long frameStartNano = System.nanoTime();

            update();
            this.repaint(); // call paintComponent()

            final long frameEndNano = System.nanoTime();
            final long sleepTimeNano = 16666667 - (frameEndNano - frameStartNano);
            final long sleepTimeMillis = (long)((float)sleepTimeNano * 1E-6f);
            try
            {
                Thread.sleep(sleepTimeMillis);
            }
            catch (Exception e) {}
        }
    }

    private void update()
    {
        final float moveDistanceX = (float)( pacmanDirectionX * 50) * 0.01667f;
        final float moveDistanceY = (float)(-pacmanDirectionY * 50) * 0.01667f;

        if(!pacmanWillCollideWithWall(pacmanX + moveDistanceX, pacmanY))
        {
            pacmanX += moveDistanceX;

            if(pacmanX > mazeImage.getWidth() + 1)
            {
                pacmanX -= mazeImage.getWidth();
            }
            else if(pacmanX < -1)
            {
                pacmanX += mazeImage.getWidth();
            }
        }
        if(!pacmanWillCollideWithWall(pacmanX, pacmanY + moveDistanceY))
        {
            pacmanY += moveDistanceY;
        }
    }

    private boolean pacmanWillCollideWithWall(float targetX, float targetY)
    {
        targetX += (pacmanImage.getWidth() / 2);
        targetY += (pacmanImage.getHeight() / 2);

        final int tiledTargetX = (int)targetX / MAZE_TILE_LENGTH;
        final int tiledTargetY = (int)targetY / MAZE_TILE_LENGTH;
        
        if(tiledTargetX <= 0 || tiledTargetX >= MAZE_TILE_COUNT_X
        || tiledTargetY <= 0 || tiledTargetY >= MAZE_TILE_COUNT_Y)
            return false;
        
        final int tileIdx = tiledTargetX + (tiledTargetY * MAZE_TILE_COUNT_X);

        /*
        System.out.print(tiledTargetX);
        System.out.print(" ");
        System.out.println(tiledTargetY);
        System.out.println(MAZE_TILEMAP[tileIdx]);
        System.out.println("");
        */

        return (MAZE_TILEMAP[tileIdx] == TILE_WALL);
    }

    public void paintComponent(Graphics _g)
    {
        super.paintComponent(_g);

        Graphics2D g = (Graphics2D)_g;

        //g.setColor(Color.WHITE);
        //g.fillRect(0, 0, 300, 200);

        g.drawImage(mazeImage, 0, 0, mazeImage.getWidth(), mazeImage.getHeight(), null);

        double pacmanRotation = 0;
        if(     pacmanDirectionX ==  1) pacmanRotation = Math.toRadians(180);
        else if(pacmanDirectionX == -1) pacmanRotation = Math.toRadians(0);
        else if(pacmanDirectionY ==  1) pacmanRotation = Math.toRadians(90);
        else if(pacmanDirectionY == -1) pacmanRotation = Math.toRadians(270);
        g.rotate(pacmanRotation, pacmanX + pacmanImage.getWidth()/2, pacmanY + pacmanImage.getHeight()/2);
        g.drawImage(pacmanImage, (int)pacmanX, (int)pacmanY, pacmanImage.getWidth(), pacmanImage.getHeight(), null);
        g.drawImage(pacmanImage, (int)pacmanX - mazeImage.getWidth(), (int)pacmanY, pacmanImage.getWidth(), pacmanImage.getHeight(), null);
        g.drawImage(pacmanImage, (int)pacmanX + mazeImage.getWidth(), (int)pacmanY, pacmanImage.getWidth(), pacmanImage.getHeight(), null);

        g.dispose(); // always at the end
    }
}