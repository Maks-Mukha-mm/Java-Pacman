package pacman.containers.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Maze extends JPanel {
    private int ghostsleeptime=400;
    private int[][] maze;
    private int counter = 0;
    private int blockSize;
    private int sleepTime = 250;

    private Hero player;
    private ArrayList<Ghosts> ghosts;
    private ArrayList<Coin> coins;
    private ArrayList<Boosters> boosters;
    private ScoreTable scoreTable;
    private volatile Thread toStopThread;
    private volatile Thread ghostThread;
    private volatile Thread ghostAnimationThread;
    private boolean isPaused = false;
    private JFrame pauseFrame;
    private JFrame mainPane;
    private Random random = new Random();
    private Thread boosterThread;



    public void setCoins(ArrayList<Coin> coins) {
        this.coins = coins;
    }

    public ArrayList<Coin> getCoins() {
        return coins;
    }
    public Maze(JFrame mainPane, ScoreTable scoreTable, int[][] mazeConfiguration) {
        this.mainPane = mainPane;
        this.scoreTable = scoreTable;
        this.blockSize = 30;
        this.maze = mazeConfiguration;

        setBackground(Color.BLACK);
        setLayout(null);

        player = new Hero(blockSize, maze);
        ghosts = new ArrayList<>();

        String[] redGhostImages = {
                "Images/redghost-1-remake.png",
                "Images/redghost-2-remake.png",
                "Images/redghost-3-remake.png"
        };
        String[] pinkGhostImages = {
                "Images/ghost-1-remake.png",
                "Images/ghost-2-remake.png",
                "Images/ghost-3-remake.png"
        };
        String[] yellowGhostImages = {
                "Images/pink-ghost-1.png",
                "Images/pink-ghost-2.png",
                "Images/pink-ghost-3.png"
        };


        Ghosts redGhost = new Ghosts(9 * blockSize, 10 * blockSize, blockSize, maze, redGhostImages, ghosts);
        Ghosts blueGhost = new Ghosts(10 * blockSize, 10 * blockSize, blockSize, maze, pinkGhostImages, ghosts);
        Ghosts yellowGhost = new Ghosts(12 * blockSize, 11 * blockSize, blockSize, maze, yellowGhostImages, ghosts);

        ghosts.add(redGhost);
        ghosts.add(blueGhost);
        ghosts.add(yellowGhost);
        player = new Hero(blockSize, maze);
        add(player.getPacmanLabel());
        setFocusable(true);

        for (Ghosts ghost : ghosts) {
            add(ghost.getGhostLabel());
        }

        coins = new ArrayList<>();
        boosters = new ArrayList<>();
        initializeCoins();
        initializeBoosters();
        startGhostMovement();
        startGhostAnimation();
        startBoosterThread();

        mainPane.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                scoreTable.pauseTimer();
                stopPlayerMovement();
                stopGhostMovement();
                stopGhostAnimation();
            }
        });
    }
    private void startBoosterThread() {
        boosterThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {

                    if (random.nextInt(100) >45) {
                        spawnRandomBooster();
                    }
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        boosterThread.start();
    }

    private void spawnRandomBooster() {
        int rows = maze.length;
        int cols = maze[0].length;

        int x, y;
        do {
            x = random.nextInt(cols) * blockSize;
            y = random.nextInt(rows) * blockSize;
        } while (maze[y / blockSize][x / blockSize] != 0);

        int boosterType = random.nextInt(5) + 2;
        Boosters booster = null;
        switch (boosterType) {
            case 2:
                booster = new Apple(x, y, blockSize, "Images/booster-apple.png", 5000);
                break;
            case 3:
                booster = new Cherry(x, y, blockSize, "Images/booster-cherry.png", 10);
                break;
            case 4:
                booster = new Orange(x, y, blockSize, "Images/booster-orange.png", 100);
                break;
            case 5:
                booster = new Key(x, y, blockSize, "Images/booster-key.png",  scoreTable);
                break;
            case 6:
                booster = new Slowdown(x, y, blockSize, "Images/booster-slowdown.png", 5000);
                break;
        }

        if (booster != null) {
            boosters.add(booster);
            SwingUtilities.invokeLater(this::repaint); // Обновляем графику в потоке EDT
        }
    }
    private void initializeCoins() {
        coins.clear();
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[row][col] == 0) {
                    Coin coin = new Coin(col * blockSize, row * blockSize, blockSize, 10);
                    coins.add(coin);
                    add(coin.getCoinLabel());
                }
            }
        }
    }

    private void initializeBoosters() {
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[row][col] == 2) {
                    boosters.add(new Apple(col * blockSize, row * blockSize, blockSize, "Images/booster-apple.png", 5000));
                }
                if (maze[row][col] == 3) {
                    boosters.add(new Cherry(col * blockSize, row * blockSize, blockSize, "Images/booster-cherry.png", 10));
                }
                if (maze[row][col] == 4) {
                    boosters.add(new Orange(col * blockSize, row * blockSize, blockSize, "Images/booster-orange.png", 100));
                }
                if (maze[row][col] == 5) {
                    boosters.add(new Key(col * blockSize, row * blockSize, blockSize, "Images/booster-key.png",  scoreTable));
                }
                if (maze[row][col] == 6) {
                    boosters.add(new Slowdown(col * blockSize, row * blockSize, blockSize, "Images/booster-slowdown.png", 5000));
                }
            }
        }
    }

    private void stopGhostMovement() {
        if (ghostThread != null && ghostThread.isAlive()) {
            ghostThread.interrupt();
            try {
                ghostThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void stopGhostAnimation() {
        if (ghostAnimationThread != null && ghostAnimationThread.isAlive()) {
            ghostAnimationThread.interrupt();
            try {
                ghostAnimationThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void startGhostMovement() {
        stopGhostMovement();
        Runnable ghostTask = () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (!isPaused) {
                        for (Ghosts ghost : ghosts) {
                            ghost.moveTowards(player);
                            if (ghost.checkCollision(player)) {
                                handlePlayerDeath();
                            }
                        }
                        Thread.sleep(ghostsleeptime);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        ghostThread = new Thread(ghostTask);
        ghostThread.start();
    }

    public void setGhostsleeptime(int ghostsleeptime) {
        this.ghostsleeptime = ghostsleeptime;
    }

    public int getGhostsleeptime() {
        return ghostsleeptime;
    }

    public void startGhostAnimation() {
        stopGhostAnimation();
        Runnable ghostAnimationTask = () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (!isPaused) {
                        for (Ghosts ghost : ghosts) {
                            ghost.toggleGhostImage();
                        }
                        Thread.sleep(200);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        ghostAnimationThread = new Thread(ghostAnimationTask);
        ghostAnimationThread.start();
    }

    private boolean canMove(int dx, int dy) {
        int nextX = player.getX() + dx;
        int nextY = player.getY() + dy;

        int row = nextY / blockSize;
        int col = nextX / blockSize;

        return !(nextX < 0 || nextX >= maze[0].length * blockSize || nextY < 0 || nextY >= maze.length * blockSize || maze[row][col] == 1);
    }

    public void movePlayer(int dx, int dy, Hero.Direction direction) {
        if (!canMove(dx, dy)) {
            return;
        }

        stopPlayerMovement();
        Runnable task = () -> {
            try {
                int nextX;
                int nextY;

                while (true) {
                    nextX = player.getX() + dx;
                    nextY = player.getY() + dy;

                    int row = nextY / blockSize;
                    int col = nextX / blockSize;

                    if (nextX < 0 || nextX >= maze[0].length * blockSize || nextY < 0 || nextY >= maze.length * blockSize || maze[row][col] == 1) {
                        break;
                    }

                    player.setX(nextX);
                    player.setY(nextY);
                    player.toggleMouth(direction);
                    collectCoin(player.getX(), player.getY());
                    collectBooster(player.getX(), player.getY());
                    repaint();

                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        toStopThread = new Thread(task);
        toStopThread.start();
    }
    private void stopPlayerMovement() {

        if (toStopThread != null && toStopThread.isAlive()) {
            toStopThread.interrupt();
            try {
                toStopThread.join();
            } catch (InterruptedException e) {
            }
        }

        }
    private void collectCoin(int x, int y) {
        for (Coin coin : coins) {
            if (coin.getX() == x && coin.getY() == y) {

                counter += coin.getValue();
                scoreTable.updateCounter(counter);
                coin.collect();
                coins.remove(coin);
                break;
            }
        }

        if (coins.isEmpty()) {
            initializeCoins();
        }
    }



    private boolean collectBooster(int x, int y) {
        for (Boosters booster : boosters) {
            if (booster.getX() == x && booster.getY() == y) {
                counter += 200;
                scoreTable.updateCounter(counter);
                booster.collect(player, this);
                boosters.remove(booster);
                return true;
            }
        }
        return false;
    }

    private void handlePlayerDeath() {
        scoreTable.loseLife();
        if (!scoreTable.isGameEnded()) {
            player.setX(30);
            player.setY(30);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[row][col] == 1) {
                    g.setColor(Color.GREEN);
                    g.fillRect(col * blockSize, row * blockSize, blockSize, blockSize);
                }
            }
        }

        for (Boosters booster : boosters) {
            booster.draw(g);
        }
    }

    public void pauseGame() {
        isPaused = true;
        stopPlayerMovement();
        stopGhostMovement();
        stopGhostAnimation();
        showPauseMenu();
    }

    public void resumeGame() {
        isPaused = false;
        if (pauseFrame != null) {
            pauseFrame.dispose();
            startGhostMovement();
            startGhostAnimation();
        }
        repaint();
    }

    public Hero getHero() {
        return player;
    }

    public ScoreTable getScoreTable() {
        return scoreTable;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void setScoreTable(ScoreTable scoreTable) {
        this.scoreTable = scoreTable;
    }

    private void showPauseMenu() {
        if (pauseFrame == null) {
            pauseFrame = new JFrame("Paused");
            pauseFrame.setSize(300, 200);
            pauseFrame.setLayout(new BorderLayout());

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(2, 1));

            JButton resumeButton = new JButton("Resume");
            resumeButton.addActionListener(e -> {
                resumeGame();
                pauseFrame.setVisible(false);
            });

            JButton mainMenuButton = new JButton("Main Menu");
            mainMenuButton.addActionListener(e -> {
                mainPane.dispose();
                pauseFrame.setVisible(false);
            });

            resumeButton.addActionListener(e -> scoreTable.resumeTimer());

            buttonPanel.add(resumeButton);
            buttonPanel.add(mainMenuButton);

            pauseFrame.add(buttonPanel, BorderLayout.CENTER);
            pauseFrame.setLocationRelativeTo(null);
        }
        pauseFrame.setVisible(true);
    }}