package pacman.containers.components;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HighScoresWindow extends JFrame {
    public HighScoresWindow() {
        setTitle("High Scores");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        List<ScoreEntry> scores = EndGameDialog.getScores();
        DefaultListModel<ScoreEntry> listModel = new DefaultListModel<>();
        for (ScoreEntry score : scores) {
            listModel.addElement(score);
        }

        JList<ScoreEntry> scoreList = new JList<>(listModel);
        scoreList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (c instanceof JLabel && value instanceof ScoreEntry) {
                    ((JLabel) c).setText(((ScoreEntry) value).toString());
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(scoreList);
        add(scrollPane, BorderLayout.CENTER);
    }
}