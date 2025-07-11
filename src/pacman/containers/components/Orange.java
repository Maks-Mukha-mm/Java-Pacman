package pacman.containers.components;

public class Orange extends Boosters {
    private int newSleepTime;

    public Orange(int x, int y, int blockSize, String imagePath, int newSleepTime) {
        super(x, y, blockSize, imagePath);
        this.newSleepTime = newSleepTime;
    }

    @Override
    public void collect(Hero player, Maze maze) {




        Thread sleepTimeThread = new Thread(() -> {



                maze.setSleepTime(maze.getSleepTime()/2);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            maze.setSleepTime(maze.getSleepTime()*2);
        });

              sleepTimeThread.start();
    }

    public int getNewSleepTime() {
        return newSleepTime;
    }
}