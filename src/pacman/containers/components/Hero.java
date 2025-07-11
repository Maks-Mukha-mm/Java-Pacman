package pacman.containers.components;

import javax.swing.*;
import java.awt.*;

public class Hero {
    private int x, y;
       private ImageIcon[] pacmanRightImages;
    private ImageIcon[] pacmanLeftImages;
    private ImageIcon[] pacmanUpImages;
    private ImageIcon[] pacmanDownImages;
    private int currentImageIndex;
    private ImageIcon currentImage;
      private JLabel pacmanLabel;

    public Hero(int blockSize, int[][] maze) {

        x = blockSize;
        y = blockSize;


        pacmanRightImages = new ImageIcon[]{
                new ImageIcon(new ImageIcon("Images/open.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT)),
                new ImageIcon(new ImageIcon("Images/2close.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT)),
                new ImageIcon(new ImageIcon("Images/close.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT))
        };

        pacmanLeftImages = new ImageIcon[]{
                new ImageIcon(new ImageIcon("Images/left-open.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT)),
                new ImageIcon(new ImageIcon("Images/left-2close.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT)),
                new ImageIcon(new ImageIcon("Images/left-close.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT))
        };

        pacmanUpImages = new ImageIcon[]{
                new ImageIcon(new ImageIcon("Images/top-open.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT)),
                new ImageIcon(new ImageIcon("Images/top-2close.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT)),
                new ImageIcon(new ImageIcon("Images/top-close.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT))
        };

        pacmanDownImages = new ImageIcon[]{
                new ImageIcon(new ImageIcon("Images/bot-open.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT)),
                new ImageIcon(new ImageIcon("Images/bot-2close.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT)),
                new ImageIcon(new ImageIcon("Images/bot-close.png").getImage().getScaledInstance(blockSize, blockSize, Image.SCALE_DEFAULT))
        };

        currentImageIndex = 0;
        currentImage = pacmanRightImages[currentImageIndex];

        pacmanLabel = new JLabel(currentImage);
        pacmanLabel.setBounds(x, y, blockSize, blockSize);
    }

    public JLabel getPacmanLabel() {
        return pacmanLabel;
    }


    public void toggleMouth(Direction direction) {
        switch (direction) {
            case RIGHT:
                currentImageIndex = (currentImageIndex + 1) % pacmanRightImages.length;
                currentImage = pacmanRightImages[currentImageIndex];
                break;
            case LEFT:
                currentImageIndex = (currentImageIndex + 1) % pacmanLeftImages.length;
                currentImage = pacmanLeftImages[currentImageIndex];
                break;
            case UP:
                currentImageIndex = (currentImageIndex + 1) % pacmanUpImages.length;
                currentImage = pacmanUpImages[currentImageIndex];
                break;
            case DOWN:
                currentImageIndex = (currentImageIndex + 1) % pacmanDownImages.length;
                currentImage = pacmanDownImages[currentImageIndex];
                break;
        }
        pacmanLabel.setIcon(currentImage);
    }

    public void setX(int x) {
        this.x = x;
        pacmanLabel.setLocation(x, y);
    }

    public void setY(int y) {
        this.y = y;
        pacmanLabel.setLocation(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }
}