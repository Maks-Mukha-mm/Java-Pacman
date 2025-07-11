package pacman.containers.components;


public class Slowdown extends Boosters {
    private int duration;

    public Slowdown(int x, int y, int blockSize, String imagePath, int duration) {
        super(x, y, blockSize, imagePath);
        this.duration = duration;
    }

    @Override
    public void collect(Hero player, Maze maze) {
               new Thread(() -> {

            maze.setGhostsleeptime(maze.getGhostsleeptime() * 2);
                   try {
                       Thread.sleep(duration);
                   } catch (InterruptedException e) {
                       throw new RuntimeException(e);
                   }
                   maze.setGhostsleeptime(maze.getGhostsleeptime() / 2);
        }).start();
    }
}
