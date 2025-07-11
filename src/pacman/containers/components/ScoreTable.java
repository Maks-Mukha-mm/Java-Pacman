package pacman.containers.components;

import javax.swing.*;
import java.awt.*;


public class ScoreTable extends JPanel {
    private JLabel counterLabel;
    private JLabel timerLabel;
    private boolean isPaused;
    private JLabel livesPanel;
    private int remainingTime;
    private boolean gameEnded;
    private int lives = 3;
    private JLabel[] lifeIcons;
    private final Thread pauseLock = new Thread();


    public ScoreTable() {
        setLayout(new BorderLayout());


        JPanel scoreTimerPanel = new JPanel(new GridLayout(1, 3));
        scoreTimerPanel.setPreferredSize(new Dimension(465, 30));
        scoreTimerPanel.setBackground(Color.BLACK);

        counterLabel = new JLabel("Score: 0");
        counterLabel.setHorizontalAlignment(JLabel.CENTER);
        counterLabel.setForeground(Color.WHITE);
        counterLabel.setBackground(Color.BLACK);
        counterLabel.setOpaque(true);

        timerLabel = new JLabel("Time: ");
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBackground(Color.BLACK);
        timerLabel.setOpaque(true);

        livesPanel = new JLabel();
        livesPanel.setPreferredSize(new Dimension(465, 30));
        livesPanel.setBackground(Color.BLACK);
        livesPanel.setLayout(new GridLayout(1, 3));

        lifeIcons = new JLabel[lives];
        for (int i = 0; i < lives; i++) {
            ImageIcon lifeIcon = new ImageIcon("Images/heart.png");
            Image scaledImage = lifeIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            lifeIcon = new ImageIcon(scaledImage);
            lifeIcons[i] = new JLabel(lifeIcon);
            livesPanel.add(lifeIcons[i]);
        }

        scoreTimerPanel.add(counterLabel);
        scoreTimerPanel.add(timerLabel);
        scoreTimerPanel.add(livesPanel);

        add(scoreTimerPanel, BorderLayout.NORTH);


        remainingTime = 200;
        gameEnded = false;

        startTimer();
    }

    public JLabel getCounterLabel() {
        return counterLabel;
    }

    public void updateCounter(int counter) {
        counterLabel.setText("Score: " + counter);
    }

    public void loseLife() {
        if (lives > 0) {
            lives--;
            updateLivesPanel();
        }

        if (lives == 0) {

            gameEnded = true;
            EndGameDialog endGameDialog = new EndGameDialog((JFrame) SwingUtilities.getWindowAncestor(this), getScore());
            endGameDialog.setVisible(true);
            System.exit(0);
        }
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    private void startTimer() {
        Thread timerThread = new Thread(() -> {
            while (remainingTime > 0 && !gameEnded) {
                synchronized (pauseLock) {
                    while (isPaused) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                remainingTime--;
                timerLabel.setText("Time: " + remainingTime);

                if (remainingTime == 0 && !gameEnded) {
                    JOptionPane.showMessageDialog(this, "time is up");
                    gameEnded = true;
                    EndGameDialog endGameDialog = new EndGameDialog((JFrame) SwingUtilities.getWindowAncestor(this), getScore());
                    endGameDialog.setVisible(true);
                    System.exit(0);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        timerThread.start();
    }

    public void pauseTimer() {
        isPaused = true;
    }

    public void resumeTimer() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    public JLabel getTimerLabel() {
        return timerLabel;
    }

    public void increaseLives() {
        lives++;
    }

    public void updateLivesPanel() {
        livesPanel.removeAll();
        for (int i = 0; i < lives; i++) {
            ImageIcon lifeIcon = new ImageIcon("Images/heart.png");
            Image scaledImage = lifeIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            lifeIcon = new ImageIcon(scaledImage);
            JLabel lifeLabel = new JLabel(lifeIcon);
            livesPanel.add(lifeLabel);
        }
        livesPanel.revalidate();
        livesPanel.repaint();
    }

    private int getScore() {
        String scoreText = counterLabel.getText();
        return Integer.parseInt(scoreText.replaceAll("[^0-9]", ""));
    }


}