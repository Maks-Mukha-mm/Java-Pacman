package pacman.containers.components;

public class Key extends Boosters {

    private ScoreTable scoreTable;

    public Key(int x, int y, int blockSize, String imagePath , ScoreTable scoreTable) {
        super(x, y, blockSize, imagePath);

        this.scoreTable = scoreTable;
    }

    @Override
    public void collect(Hero player, Maze maze) {
        scoreTable.increaseLives();
        scoreTable.updateLivesPanel();
    }

}