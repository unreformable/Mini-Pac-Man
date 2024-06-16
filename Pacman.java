import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
/**
 * Title character - the Pacman!
 */
public class Pacman
{
    public float position_x;
    public float position_y;

    public int move_direction_x = 0;
    public int move_direction_y = 0;

    public int last_move_direction_x = 0;
    public int last_move_direction_y = 0;

    public BufferedImage image;

    public Pacman(float position_x, float position_y, String image_path)
    {
        this.position_x = position_x;
        this.position_y = position_y;
        
        try
        {
            image = ImageIO.read(new File(image_path));
        }
        catch(IOException e) {}
    }
}