import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Gives information about input state (keyboard, mouse).
 * 
 * More information about methods in KeyListener and MouseListener interfaces.
 */
public class Input implements KeyListener, MouseListener
{
    public boolean up = false;
    public boolean down = false;
    public boolean left = false;
    public boolean right = false;

    // Can't both be 1 at once
    public int direction_x = 0; // 1=right, -1=left, 0=none
    public int direction_y = 0; // 1=up,    -1=down, 0=none

    public void keyPressed(KeyEvent e)
    {
        final int key = e.getKeyCode();

        if(key == KeyEvent.VK_UP    || key == KeyEvent.VK_W) { up = true;    direction_x =  0; direction_y =  1; }
        if(key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_S) { down = true;  direction_x =  0; direction_y = -1; }
        if(key == KeyEvent.VK_LEFT  || key == KeyEvent.VK_A) { left = true;  direction_x = -1; direction_y =  0; }
        if(key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) { right = true; direction_x =  1; direction_y =  0; }
    }
    public void keyReleased(KeyEvent e)
    {
        final int key = e.getKeyCode();

        if(key == KeyEvent.VK_UP    || key == KeyEvent.VK_W) up = false;
        if(key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_S) down = false;
        if(key == KeyEvent.VK_LEFT  || key == KeyEvent.VK_A) left = false;
        if(key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = false;
    }
    public void keyTyped(KeyEvent e) {}

    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}