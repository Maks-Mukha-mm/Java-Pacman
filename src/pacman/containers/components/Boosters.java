package pacman.containers.components;

import javax.swing.*;
import java.awt.*;

public abstract class Boosters {
    protected int x, y;
    protected int blockSize;
    protected Image image;

    public Boosters(int x, int y, int blockSize, String imagePath) {
        this.x = x;
        this.y = y;
        this.blockSize = blockSize;
        ImageIcon icon = new ImageIcon(imagePath);
        image = icon.getImage();


    }

    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, blockSize, blockSize, null);
        }
    }

    public abstract void collect(Hero player, Maze maze);

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}