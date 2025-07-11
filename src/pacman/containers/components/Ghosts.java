package pacman.containers.components;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Ghosts {
    private int x, y;
    private int blockSize;
    private ImageIcon[] ghostImages;
    private int currentImageIndex;
    private int[][] maze;
    private List<Ghosts> otherGhosts;
    private JLabel ghostLabel;

    public Ghosts(int x, int y, int blockSize, int[][] maze, String[] imagePaths, List<Ghosts> otherGhosts) {
        this.x = x;
        this.y = y;
        this.blockSize = blockSize;
        this.maze = maze;
        this.otherGhosts = otherGhosts;

        ghostImages = new ImageIcon[imagePaths.length];
        for (int i = 0; i < imagePaths.length; i++) {
            ghostImages[i] = new ImageIcon(imagePaths[i]);
        }
        currentImageIndex = 0;

        ghostLabel = new JLabel(ghostImages[currentImageIndex]);
        ghostLabel.setBounds(x, y, blockSize, blockSize);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public JLabel getGhostLabel() {
        return ghostLabel;
    }

    public void moveTowards(Hero player) {
        int playerX = player.getX();
        int playerY = player.getY();

        int diffX = playerX - x;
        int diffY = playerY - y;

        int stepX = Integer.signum(diffX) * blockSize;
        int stepY = Integer.signum(diffY) * blockSize;

        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (isValidMove(x + stepX, y)) {
                x += stepX;
            } else if (isValidMove(x, y + stepY)) {
                y += stepY;
            } else if (isValidMove(x, y - stepY)) {
                y -= stepY;
            } else if (isValidMove(x - stepX, y)) {
                x -= stepX;
            }
        } else {
            if (isValidMove(x, y + stepY)) {
                y += stepY;
            } else if (isValidMove(x + stepX, y)) {
                x += stepX;
            } else if (isValidMove(x - stepX, y)) {
                x -= stepX;
            } else if (isValidMove(x, y - stepY)) {
                y -= stepY;
            }
        }
        updateLabelPosition();
    }

    private void updateLabelPosition() {
        ghostLabel.setLocation(x, y);
    }

    private boolean isValidMove(int x, int y) {
        int row = y / blockSize;
        int col = x / blockSize;

        for (Ghosts otherGhost : otherGhosts) {
            if (otherGhost != this && otherGhost.getX() == x && otherGhost.getY() == y) {
                return false;
            }
        }

        return x >= 0 && y >= 0 && x < maze[0].length * blockSize && y < maze.length * blockSize && maze[row][col] != 1;
    }

    public boolean checkCollision(Hero player) {
        return x == player.getX() && y == player.getY();
    }

    public void toggleGhostImage() {
        currentImageIndex = (currentImageIndex + 1) % ghostImages.length;
        ghostLabel.setIcon(ghostImages[currentImageIndex]);
    }
}