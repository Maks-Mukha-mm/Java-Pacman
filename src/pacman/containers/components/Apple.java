package pacman.containers.components;

public class Apple extends Boosters {
    private long duration;

    public Apple(int x, int y, int blockSize, String imagePath, long duration) {
        super(x, y, blockSize, imagePath);
        this.duration = duration;
    }

    @Override
    public void collect(Hero player, Maze maze) {

        Thread boosterThread = new Thread(() -> {
            try {
                        for (Coin coin : maze.getCoins()) {
                        coin.setValue(20);
                    }

                Thread.sleep(duration);

                    for (Coin coin : maze.getCoins()) {
                        coin.setValue(10);
                    }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        boosterThread.start();
    }
}