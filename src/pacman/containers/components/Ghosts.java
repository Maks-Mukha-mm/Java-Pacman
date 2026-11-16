package pacman.containers.components;

import pacman.ai.GhostMode;
import pacman.ai.GhostPersonality;
import pacman.ai.Pathfinder;
import pacman.ai.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ghosts {
    private static final float FRIGHTENED_HUE = 0.64f;

    private int x, y;
    private final int startX, startY;
    private final int blockSize;
    private final ImageIcon[] ghostImages;
    private final ImageIcon[] frightenedImages;
    private final ImageIcon[] flashImages;
    private int currentImageIndex;
    private final List<Ghosts> otherGhosts;
    private final JLabel ghostLabel;

    private final GhostPersonality personality;
    private final Pathfinder pathfinder;
    private final Tile homeTile;
    private final Random random = new Random();

    private volatile boolean frightened;
    private volatile boolean frightenedEnding;
    private int tickCounter;

    /** Last planned route and target, read by the debug overlay. */
    private volatile List<Tile> currentPath = List.of();
    private volatile Tile currentTarget;

    public Ghosts(int x, int y, int blockSize, int[][] maze, String[] imagePaths,
                  List<Ghosts> otherGhosts, GhostPersonality personality) {
        this(x, y, blockSize, maze, loadImages(imagePaths), otherGhosts, personality);
    }

    public Ghosts(int x, int y, int blockSize, int[][] maze, ImageIcon[] images,
                  List<Ghosts> otherGhosts, GhostPersonality personality) {
        this.blockSize = blockSize;
        this.otherGhosts = otherGhosts;
        this.personality = personality;
        this.pathfinder = new Pathfinder(maze);
        this.homeTile = pathfinder.nearestWalkable(personality.homeTile(maze.length, maze[0].length));

        // Spawn points may fall inside a wall on some maze layouts.
        Tile spawn = pathfinder.nearestWalkable(new Tile(y / blockSize, x / blockSize));
        this.startX = this.x = spawn.col() * blockSize;
        this.startY = this.y = spawn.row() * blockSize;

        this.ghostImages = images;
        this.frightenedImages = new ImageIcon[images.length];
        this.flashImages = new ImageIcon[images.length];
        for (int i = 0; i < images.length; i++) {
            frightenedImages[i] = recolor(images[i], FRIGHTENED_HUE, 0.9f);
            flashImages[i] = recolor(images[i], 0f, 0f);
        }
        currentImageIndex = 0;

        ghostLabel = new JLabel(ghostImages[currentImageIndex]);
        ghostLabel.setBounds(this.x, this.y, blockSize, blockSize);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Tile getTile() {
        return new Tile(y / blockSize, x / blockSize);
    }

    public JLabel getGhostLabel() {
        return ghostLabel;
    }

    public GhostPersonality getPersonality() {
        return personality;
    }

    public List<Tile> getCurrentPath() {
        return currentPath;
    }

    public Tile getCurrentTarget() {
        return currentTarget;
    }

    public boolean isFrightened() {
        return frightened;
    }

    public void setFrightened(boolean frightened, boolean ending) {
        this.frightened = frightened;
        this.frightenedEnding = ending;
    }

    /**
     * Makes one step according to the current mode.
     *
     * @param blinky the red ghost, whose position Inky uses for flanking
     */
    public void update(Hero player, GhostMode mode, Ghosts blinky) {
        tickCounter++;
        Tile me = getTile();
        Tile pacman = new Tile(player.getY() / blockSize, player.getX() / blockSize);

        if (mode == GhostMode.FRIGHTENED) {
            currentTarget = null;
            // Frightened ghosts move at half speed.
            if (tickCounter % 2 == 0) {
                Tile next = fleeStep(me, pacman);
                currentPath = next == null ? List.of() : List.of(next);
                if (next != null) {
                    moveTo(next);
                }
            }
            return;
        }

        Tile rawTarget;
        if (mode == GhostMode.SCATTER) {
            rawTarget = homeTile;
        } else {
            Hero.Direction dir = player.getDirection();
            Tile blinkyTile = blinky != null ? blinky.getTile() : pacman;
            rawTarget = personality.chaseTarget(me, pacman, dir.dRow, dir.dCol, blinkyTile, homeTile);
        }
        Tile target = pathfinder.nearestWalkable(rawTarget);

        List<Tile> path = pathfinder.findPath(me, target);
        if (path.isEmpty() && mode == GhostMode.CHASE) {
            // Already standing on the ambush point: close in on Pac-Man directly.
            path = pathfinder.findPath(me, pacman);
        }
        currentTarget = target;
        currentPath = path;

        if (!path.isEmpty() && !isOccupied(path.get(0))) {
            moveTo(path.get(0));
        }
    }

    /** Picks the free neighbouring tile that is farthest from Pac-Man, breaking ties randomly. */
    private Tile fleeStep(Tile me, Tile pacman) {
        List<Tile> best = new ArrayList<>();
        double bestDistance = -1;
        for (Tile next : pathfinder.walkableNeighbours(me)) {
            if (isOccupied(next)) {
                continue;
            }
            double d = next.distanceTo(pacman);
            if (d > bestDistance + 1e-9) {
                best.clear();
                best.add(next);
                bestDistance = d;
            } else if (Math.abs(d - bestDistance) < 1e-9) {
                best.add(next);
            }
        }
        return best.isEmpty() ? null : best.get(random.nextInt(best.size()));
    }

    private boolean isOccupied(Tile tile) {
        for (Ghosts other : otherGhosts) {
            if (other != this && other.getTile().equals(tile)) {
                return true;
            }
        }
        return false;
    }

    private void moveTo(Tile tile) {
        x = tile.col() * blockSize;
        y = tile.row() * blockSize;
        updateLabelPosition();
    }

    /** Sends the ghost back to its spawn point after Pac-Man eats it. */
    public void respawn() {
        x = startX;
        y = startY;
        frightened = false;
        currentPath = List.of();
        updateLabelPosition();
    }

    private void updateLabelPosition() {
        ghostLabel.setLocation(x, y);
    }

    public boolean checkCollision(Hero player) {
        return x == player.getX() && y == player.getY();
    }

    public void toggleGhostImage() {
        currentImageIndex = (currentImageIndex + 1) % ghostImages.length;
        ImageIcon[] set = ghostImages;
        if (frightened) {
            set = frightenedEnding && currentImageIndex % 2 == 0 ? flashImages : frightenedImages;
        }
        ghostLabel.setIcon(set[currentImageIndex]);
    }

    private static ImageIcon[] loadImages(String[] paths) {
        ImageIcon[] icons = new ImageIcon[paths.length];
        for (int i = 0; i < paths.length; i++) {
            icons[i] = new ImageIcon(paths[i]);
        }
        return icons;
    }

    /**
     * Repaints the coloured body of a ghost sprite in another hue while keeping
     * its eyes (white/black pixels) untouched.
     *
     * @param saturation 0 produces a white body, used for the flashing effect
     */
    public static ImageIcon recolor(ImageIcon source, float hue, float saturation) {
        int w = Math.max(1, source.getIconWidth());
        int h = Math.max(1, source.getIconHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(source.getImage(), 0, 0, null);
        g.dispose();

        float[] hsb = new float[3];
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int argb = img.getRGB(px, py);
                int alpha = argb >>> 24;
                if (alpha == 0) continue;
                Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, hsb);
                if (hsb[1] < 0.35f || hsb[2] < 0.2f) continue; // eyes, outline
                int rgb = Color.HSBtoRGB(hue, saturation, hsb[2]);
                img.setRGB(px, py, (alpha << 24) | (rgb & 0xFFFFFF));
            }
        }
        return new ImageIcon(img);
    }
}
