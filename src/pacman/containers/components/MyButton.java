package pacman.containers.components;

import pacman.containers.pickLevelComponent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class MyButton {
    JFrame mainPane;
    private JButton startgame = new JButton("Start the game");
    private JButton settings = new JButton("Scores");
    private JButton end = new JButton("Exit");
    private JButton back = new JButton("back");


    JButton firstType = new JButton("465x520");
    JButton secondType = new JButton("495x550");
    JButton thirdType = new JButton("910x640");
    JButton forthType = new JButton("620x530");
    JButton fifthType = new JButton("910x430");

    private ArrayList<JButton> mainMenuBtnList = new ArrayList<>();
    private ArrayList<JButton> boardsBtnList = new ArrayList<>();

    public MyButton(JFrame mainPane) {
        this.mainPane = mainPane;

        mainMenuBtnList.add(startgame);
        mainMenuBtnList.add(settings);
        mainMenuBtnList.add(end);

        boardsBtnList.add(firstType);
        boardsBtnList.add(secondType);
        boardsBtnList.add(thirdType);
        boardsBtnList.add(forthType);
        boardsBtnList.add(fifthType);
        makeAliveBtn();
    }

    private void makeAliveBtn() {
        for (JButton btn : mainMenuBtnList) {
            btn.addActionListener(new ButtonEventListener());
            btn.setMinimumSize(new Dimension(150, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        for (JButton btn : boardsBtnList) {
            btn.addActionListener(new ButtonEventListener());
            btn.setMinimumSize(new Dimension(150, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
    }

    class ButtonEventListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == end) {
                System.exit(0);
            } else if (e.getSource() == startgame) {
                mainPane.getContentPane().removeAll();
                mainPane.getContentPane().add(new pickLevelComponent(mainPane).getContainer());
                mainPane.revalidate();
                mainPane.repaint();
            } else if (e.getSource() == settings) {
                SwingUtilities.invokeLater(() -> {
                    HighScoresWindow highScoresWindow = new HighScoresWindow();
                    highScoresWindow.setVisible(true);
                });
            } else if (e.getSource() == firstType) {
                createGameWindow(465, 520, MazeConfigurations.FIRST_MAZE);
            } else if (e.getSource() == secondType) {
                createGameWindow(495, 550, MazeConfigurations.SECOND_MAZE);
            } else if (e.getSource() == thirdType) {
                createGameWindow(910, 640, MazeConfigurations.THIRD_MAZE);
            } else if (e.getSource() == forthType) {
                createGameWindow(620, 530, MazeConfigurations.FOURTH_MAZE);
            } else if (e.getSource() == fifthType) {
                createGameWindow(910, 430, MazeConfigurations.FIFTH_MAZE);
            }
        }
    }
    public void closeGameWindow(JFrame gameFrame) {
        gameFrame.dispose();
    }
    private void createGameWindow(int width, int height, int[][] mazeConfiguration) {
        JFrame gameFrame = new JFrame("Pac-Man Game");
        gameFrame.setSize(width, height);
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ScoreTable scoreTable = new ScoreTable();
        Maze maze = new Maze(gameFrame, scoreTable,mazeConfiguration);

        gameFrame.add(scoreTable, BorderLayout.NORTH);
        gameFrame.add(maze, BorderLayout.CENTER);

        gameFrame.setVisible(true);
        gameFrame.setFocusable(true);
        gameFrame.requestFocusInWindow();

        Hero player = maze.getHero();
        gameFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                if (key == KeyEvent.VK_ESCAPE) {
                    maze.pauseGame();
                    scoreTable.pauseTimer();
                }

                if (key == KeyEvent.VK_LEFT) {
                    maze.movePlayer(-30, 0, Hero.Direction.LEFT);

                } else if (key == KeyEvent.VK_RIGHT) {
                    maze.movePlayer(30, 0,Hero.Direction.RIGHT);

                } else if (key == KeyEvent.VK_UP) {
                    maze.movePlayer(0, -30,Hero.Direction.UP);

                } else if (key == KeyEvent.VK_DOWN) {
                    maze.movePlayer(0, 30,Hero.Direction.DOWN);

                } else if (key == KeyEvent.VK_D) {
                    maze.toggleDebug();
                }
            }
        });

    }

    public ArrayList<JButton> getMainMenuBtnList() {
        return mainMenuBtnList;
    }

    public ArrayList<JButton> getBoardsBtnList() {
        return boardsBtnList;
    }


}