package pacman.ai;

import java.util.*;

/**
 * A* pathfinding on the maze grid. Cells with value {@code 1} are walls,
 * every other value is walkable. Movement is 4-directional.
 */
public class Pathfinder {

    /** Neighbour order: up, left, down, right (the classic arcade tie-break order). */
    private static final int[][] DIRECTIONS = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};

    private final int[][] maze;
    private final int rows;
    private final int cols;

    public Pathfinder(int[][] maze) {
        this.maze = maze;
        this.rows = maze.length;
        this.cols = maze[0].length;
    }

    public boolean isInside(Tile t) {
        return t.row() >= 0 && t.row() < rows && t.col() >= 0 && t.col() < cols;
    }

    public boolean isWalkable(Tile t) {
        return isInside(t) && maze[t.row()][t.col()] != 1;
    }

    public List<Tile> walkableNeighbours(Tile t) {
        List<Tile> result = new ArrayList<>(4);
        for (int[] d : DIRECTIONS) {
            Tile next = t.offset(d[0], d[1]);
            if (isWalkable(next)) {
                result.add(next);
            }
        }
        return result;
    }

    /**
     * Finds the shortest path from {@code start} to {@code goal}.
     *
     * @return tiles to walk through, excluding {@code start} and including {@code goal};
     * an empty list if the ghost is already at the goal or the goal is unreachable
     */
    public List<Tile> findPath(Tile start, Tile goal) {
        if (start.equals(goal) || !isWalkable(goal)) {
            return List.of();
        }

        Map<Tile, Tile> cameFrom = new HashMap<>();
        Map<Tile, Integer> gScore = new HashMap<>();
        PriorityQueue<Node> open = new PriorityQueue<>();

        gScore.put(start, 0);
        open.add(new Node(start, 0, start.manhattanDistanceTo(goal), 0));
        long order = 1;

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.tile.equals(goal)) {
                return reconstruct(cameFrom, goal);
            }
            if (current.g > gScore.getOrDefault(current.tile, Integer.MAX_VALUE)) {
                continue; // stale queue entry
            }
            for (Tile next : walkableNeighbours(current.tile)) {
                int tentativeG = current.g + 1;
                if (tentativeG < gScore.getOrDefault(next, Integer.MAX_VALUE)) {
                    gScore.put(next, tentativeG);
                    cameFrom.put(next, current.tile);
                    int h = next.manhattanDistanceTo(goal);
                    open.add(new Node(next, tentativeG, h, order++));
                }
            }
        }
        return List.of();
    }

    /**
     * Returns the walkable tile closest to {@code target}. Targets can point into
     * a wall or even outside the maze (e.g. "4 tiles ahead of Pac-Man"), so they
     * are projected onto the nearest reachable cell first.
     */
    public Tile nearestWalkable(Tile target) {
        Tile clamped = new Tile(
                Math.max(0, Math.min(rows - 1, target.row())),
                Math.max(0, Math.min(cols - 1, target.col())));
        if (isWalkable(clamped)) {
            return clamped;
        }

        Deque<Tile> queue = new ArrayDeque<>();
        Set<Tile> visited = new HashSet<>();
        queue.add(clamped);
        visited.add(clamped);
        while (!queue.isEmpty()) {
            Tile t = queue.poll();
            for (int[] d : DIRECTIONS) {
                Tile next = t.offset(d[0], d[1]);
                if (!isInside(next) || !visited.add(next)) {
                    continue;
                }
                if (isWalkable(next)) {
                    return next;
                }
                queue.add(next);
            }
        }
        return clamped; // maze without any walkable cell
    }

    private static List<Tile> reconstruct(Map<Tile, Tile> cameFrom, Tile goal) {
        LinkedList<Tile> path = new LinkedList<>();
        Tile step = goal;
        while (cameFrom.containsKey(step)) {
            path.addFirst(step);
            step = cameFrom.get(step);
        }
        return path;
    }

    private record Node(Tile tile, int g, int h, long order) implements Comparable<Node> {
        @Override
        public int compareTo(Node other) {
            int byF = Integer.compare(g + h, other.g + other.h);
            if (byF != 0) return byF;
            int byH = Integer.compare(h, other.h);
            if (byH != 0) return byH;
            return Long.compare(order, other.order);
        }
    }
}
