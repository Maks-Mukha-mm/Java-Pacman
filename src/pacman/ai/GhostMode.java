package pacman.ai;

public enum GhostMode {
    /** Ghosts hunt Pac-Man, each with its own targeting strategy. */
    CHASE,
    /** Ghosts retreat to their home corners for a short time. */
    SCATTER,
    /** Ghosts run away from Pac-Man and can be eaten. */
    FRIGHTENED
}
