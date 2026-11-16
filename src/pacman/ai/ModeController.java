package pacman.ai;

/**
 * Switches ghosts between SCATTER and CHASE on a fixed schedule,
 * with FRIGHTENED mode overriding the schedule while it is active.
 * <p>
 * Time is advanced by the game loop via {@link #tick(long)}, so pausing
 * the game automatically pauses the schedule as well.
 */
public class ModeController {

    /** Alternating phase lengths: scatter, chase, scatter, chase, scatter, then chase forever. */
    private static final long[] PHASES_MS = {7_000, 20_000, 7_000, 20_000, 5_000};
    private static final long FRIGHTENED_WARNING_MS = 2_000;

    private long elapsedMs;
    private long frightenedLeftMs;

    public synchronized void tick(long deltaMs) {
        if (frightenedLeftMs > 0) {
            // The scatter/chase schedule is frozen while ghosts are frightened.
            frightenedLeftMs = Math.max(0, frightenedLeftMs - deltaMs);
            return;
        }
        elapsedMs += deltaMs;
    }

    public synchronized GhostMode currentMode() {
        if (frightenedLeftMs > 0) {
            return GhostMode.FRIGHTENED;
        }
        long t = elapsedMs;
        for (int i = 0; i < PHASES_MS.length; i++) {
            if (t < PHASES_MS[i]) {
                return i % 2 == 0 ? GhostMode.SCATTER : GhostMode.CHASE;
            }
            t -= PHASES_MS[i];
        }
        return GhostMode.CHASE;
    }

    public synchronized void frighten(long durationMs) {
        frightenedLeftMs = durationMs;
    }

    /** True during the last seconds of FRIGHTENED mode, used to make ghosts flash. */
    public synchronized boolean isFrightenedEnding() {
        return frightenedLeftMs > 0 && frightenedLeftMs <= FRIGHTENED_WARNING_MS;
    }
}
