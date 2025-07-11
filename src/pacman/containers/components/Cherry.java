package pacman.containers.components;

public class Cherry extends Boosters {
    private int timeIncrease;

    public Cherry(int x, int y, int blockSize, String imagePath, int timeIncrease) {
        super(x, y, blockSize, imagePath);
        this.timeIncrease = timeIncrease;
    }

    @Override
    public void collect(Hero player, Maze maze) {
        maze.getScoreTable().setRemainingTime(maze.getScoreTable().getRemainingTime() + timeIncrease);
        maze.getScoreTable().getTimerLabel().setText("Time: " + maze.getScoreTable().getRemainingTime());
    }

}