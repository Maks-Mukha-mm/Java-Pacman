package pacman.ai;

import java.awt.*;

/**
 * Chase strategies of the four ghosts, modelled on the original arcade game.
 */
public enum GhostPersonality {

    /** Red: targets Pac-Man's current tile directly. */
    BLINKY(new Color(255, 70, 70), Corner.TOP_RIGHT) {
        @Override
        public Tile chaseTarget(Tile self, Tile pacman, int dirRow, int dirCol, Tile blinky, Tile home) {
            return pacman;
        }
    },

    /** Pink: aims 4 tiles ahead of Pac-Man to cut him off. */
    PINKY(new Color(250, 110, 255), Corner.TOP_LEFT) {
        @Override
        public Tile chaseTarget(Tile self, Tile pacman, int dirRow, int dirCol, Tile blinky, Tile home) {
            return pacman.offset(4 * dirRow, 4 * dirCol);
        }
    },

    /**
     * Cyan: takes the point 2 tiles ahead of Pac-Man and doubles the vector
     * from Blinky to that point, so it flanks from the opposite side.
     */
    INKY(new Color(70, 220, 255), Corner.BOTTOM_RIGHT) {
        @Override
        public Tile chaseTarget(Tile self, Tile pacman, int dirRow, int dirCol, Tile blinky, Tile home) {
            Tile pivot = pacman.offset(2 * dirRow, 2 * dirCol);
            return pivot.offset(pivot.row() - blinky.row(), pivot.col() - blinky.col());
        }
    },

    /** Orange: chases while far away, but retreats to its corner when close. */
    CLYDE(new Color(255, 170, 40), Corner.BOTTOM_LEFT) {
        private static final double SHY_DISTANCE = 6;

        @Override
        public Tile chaseTarget(Tile self, Tile pacman, int dirRow, int dirCol, Tile blinky, Tile home) {
            return self.distanceTo(pacman) > SHY_DISTANCE ? pacman : home;
        }
    };

    public enum Corner {TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT}

    private final Color color;
    private final Corner homeCorner;

    GhostPersonality(Color color, Corner homeCorner) {
        this.color = color;
        this.homeCorner = homeCorner;
    }

    /**
     * @param self    the ghost's own tile
     * @param pacman  Pac-Man's tile
     * @param dirRow  Pac-Man's current direction (row component: -1, 0 or 1)
     * @param dirCol  Pac-Man's current direction (column component: -1, 0 or 1)
     * @param blinky  Blinky's tile (used by Inky)
     * @param home    this ghost's scatter corner
     */
    public abstract Tile chaseTarget(Tile self, Tile pacman, int dirRow, int dirCol, Tile blinky, Tile home);

    public Color getColor() {
        return color;
    }

    public Tile homeTile(int rows, int cols) {
        return switch (homeCorner) {
            case TOP_LEFT -> new Tile(0, 0);
            case TOP_RIGHT -> new Tile(0, cols - 1);
            case BOTTOM_LEFT -> new Tile(rows - 1, 0);
            case BOTTOM_RIGHT -> new Tile(rows - 1, cols - 1);
        };
    }
}
