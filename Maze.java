import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;

enum Tile
{
    EMPTY,
    WALL
}

public class Maze
{
    public BufferedImage image;

    public int tile_length;
    public int tile_count_x;
    public int tile_count_y;
    public Tile tilemap[];

    public Maze(int tile_length, String image_path)
    {
        this.tile_length = tile_length;
        
        try
        {
            image = ImageIO.read(new File(image_path));
        }
        catch(IOException e) {}

        assert ((image.getWidth() % tile_length) == 0);
        assert ((image.getHeight() % tile_length) == 0);

        tile_count_x = image.getWidth() / tile_length;
        tile_count_y = image.getHeight() / tile_length;

        tilemap = new Tile[tile_count_x * tile_count_y];
        
        int i = 0;
        for(int y = 0; y < tile_count_y; y++)
        {
            for(int x = 0; x < tile_count_x; x++)
            {
                final int firstX = x * tile_length;
                final int firstY = y * tile_length;
                final int lastX = (x + 1) * tile_length - 1;
                final int lastY = (y + 1) * tile_length - 1;

                int blueValue = 0;
                for(int yy = firstY; yy <= lastY; yy++)
                {
                    for(int xx = firstX; xx <= lastX; xx++)
                    {
                        blueValue += new Color(image.getRGB(xx, yy)).getBlue();
                    }
                }

                //System.out.print(blueValue % 3);
                //System.out.print(" ");

                tilemap[i++] = (blueValue == 0 ? Tile.EMPTY : Tile.WALL);
            }
            //System.out.println(" ");
        }
    }

    // Helper functions
    public int getTileIdx(int x, int y)
    {
        return x + y * tile_count_x;
    }
    public Tile getTile(int x, int y)
    {
        return tilemap[getTileIdx(x, y)];
    }
    public void setTile(int x, int y, Tile tile)
    {
        tilemap[getTileIdx(x, y)] = tile;
    }
}