package pacman.containers.components;

import javax.swing.*;
import java.awt.*;

public class Coin {
    private int x, y;
    private int blockSize;
    private int coinSize;
    private int value;
    private boolean collected;
    private ImageIcon coinPicture;
    private JLabel coinLabel;

    public Coin(int x, int y, int blockSize, int coinSize) {
        this.x = x;
        this.y = y;
        this.blockSize = blockSize;
        this.coinSize = coinSize;
        this.value = 10;
        this.collected = false;
        coinPicture = new ImageIcon(new ImageIcon("C:\\Users\\me\\Downloads\\coins.png").getImage().getScaledInstance(coinSize, coinSize, Image.SCALE_DEFAULT));

        coinLabel = new JLabel(coinPicture);
        int coinX = x + (blockSize - coinSize) / 2;
        int coinY = y + (blockSize - coinSize) / 2;
        coinLabel.setBounds(coinX, coinY, coinSize, coinSize);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getValue() {
        return value;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
        coinLabel.setVisible(false);
    }
    public void setValue(int newValue) {
        this.value = newValue;
    }
    public JLabel getCoinLabel() {
        return coinLabel;
    }
}