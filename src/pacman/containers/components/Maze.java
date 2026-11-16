package pacman.containers.components;

import pacman.ai.GhostMode;
import pacman.ai.GhostPersonality;
import pacman.ai.ModeController;
import pacman.ai.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
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
    private final ModeController modeController = new ModeController();
    private volatile boolean debugMode = false;
    private int ghostsEatenInRow = 0;



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


        // Inky reuses the red sprite, repainted cyan.
        ImageIcon[] cyanGhostImages = new ImageIcon[redGhostImages.length];
        for (int i = 0; i < redGhostImages.length; i++) {
            cyanGhostImages[i] = Ghosts.recolor(new ImageIcon(redGhostImages[i]), 0.52f, 0.85f);
        }

        // Blinky must stay first: Inky uses its position for flanking.
        Ghosts blinky = new Ghosts(9 * blockSize, 10 * blockSize, blockSize, maze, redGhostImages, ghosts, GhostPersonality.BLINKY);
        Ghosts clyde = new Ghosts(10 * blockSize, 10 * blockSize, blockSize, maze, pinkGhostImages, ghosts, GhostPersonality.CLYDE);
        Ghosts pinky = new Ghosts(12 * blockSize, 11 * blockSize, blockSize, maze, yellowGhostImages, ghosts, GhostPersonality.PINKY);
        Ghosts inky = new Ghosts(11 * blockSize, 11 * blockSize, blockSize, maze, cyanGhostImages, ghosts, GhostPersonality.INKY);

        ghosts.add(blinky);
        ghosts.add(clyde);
        ghosts.add(pinky);
        ghosts.add(inky);
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
                        modeController.tick(ghostsleeptime);
                        GhostMode mode = modeController.currentMode();
                        boolean ending = modeController.isFrightenedEnding();
                        Ghosts blinky = ghosts.get(0);

                        for (Ghosts ghost : ghosts) {
                            // A ghost eaten during this frightened period stays normal until it ends.
                            if (mode != GhostMode.FRIGHTENED) {
                                ghost.setFrightened(false, false);
                            } else if (ghost.isFrightened()) {
                                ghost.setFrightened(true, ending);
                            }
                            GhostMode ghostMode = ghost.isFrightened() ? GhostMode.FRIGHTENED
                                    : (mode == GhostMode.FRIGHTENED ? GhostMode.CHASE : mode);

                            ghost.update(player, ghostMode, blinky);
                            if (ghost.checkCollision(player)) {
                                handleGhostCollision(ghost);
                            }
                        }
                        if (debugMode) {
                            repaint();
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

    private void handleGhostCollision(Ghosts ghost) {
        if (ghost.isFrightened()) {
            // 200, 400, 800, 1600 points for each ghost eaten in a row
            counter += 200 << Math.min(ghostsEatenInRow, 3);
            ghostsEatenInRow++;
            scoreTable.updateCounter(counter);
            ghost.respawn();
        } else {
            handlePlayerDeath();
        }
    }

    /** Turns all ghosts blue: they run away and can be eaten. */
    public void frightenGhosts(long durationMs) {
        modeController.frighten(durationMs);
        ghostsEatenInRow = 0;
        for (Ghosts ghost : ghosts) {
            ghost.setFrightened(true, false);
        }
    }

    /** Shows or hides each ghost's planned route and target tile. */
    public void toggleDebug() {
        debugMode = !debugMode;
        repaint();
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

        if (debugMode) {
            drawDebugOverlay((Graphics2D) g.create());
        }
    }

    private void drawDebugOverlay(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int half = blockSize / 2;

        for (Ghosts ghost : ghosts) {
            Color color = ghost.isFrightened() ? new Color(80, 110, 255) : ghost.getPersonality().getColor();
            List<Tile> path = ghost.getCurrentPath();

            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int prevX = ghost.getX() + half;
            int prevY = ghost.getY() + half;
            for (Tile step : path) {
                int cx = step.col() * blockSize + half;
                int cy = step.row() * blockSize + half;
                g.drawLine(prevX, prevY, cx, cy);
                prevX = cx;
                prevY = cy;
            }

            Tile target = ghost.getCurrentTarget();
            if (target != null) {
                int tx = target.col() * blockSize;
                int ty = target.row() * blockSize;
                g.setStroke(new BasicStroke(2f));
                g.drawRect(tx + 3, ty + 3, blockSize - 6, blockSize - 6);
                g.drawLine(tx + 7, ty + 7, tx + blockSize - 7, ty + blockSize - 7);
                g.drawLine(tx + blockSize - 7, ty + 7, tx + 7, ty + blockSize - 7);
            }
        }

        String label = "AI: " + modeController.currentMode();
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        int w = g.getFontMetrics().stringWidth(label);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRoundRect(6, 6, w + 14, 22, 8, 8);
        g.setColor(Color.WHITE);
        g.drawString(label, 13, 22);
        g.dispose();
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