package pacman.ai;

/**
 * A single cell of the maze grid.
 */
public record Tile(int row, int col) {

    public Tile offset(int dRow, int dCol) {
        return new Tile(row + dRow, col + dCol);
    }

    public int manhattanDistanceTo(Tile other) {
        return Math.abs(row - other.row) + Math.abs(col - other.col);
    }

    public double distanceTo(Tile other) {
        return Math.hypot(row - other.row, col - other.col);
    }
}
