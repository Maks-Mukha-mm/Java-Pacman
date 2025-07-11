package pacman.containers.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EndGameDialog extends JDialog {
    private JTextField nameField;
    private int score;
    private List<ScoreEntry> scores;

    public EndGameDialog(JFrame parent, int score) {
        super(parent, "game over", true);

        this.score = score;
        this.scores = new ArrayList<>();

        loadScores();

        setLayout(new GridLayout(3, 1));

        JLabel messageLabel = new JLabel("Enter your name:");
        nameField = new JTextField();

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveScore();
dispose();
            }
        });

        add(messageLabel);
        add(nameField);
        add(submitButton);

        setSize(300, 150);
        setLocationRelativeTo(parent);
    }

    private void loadScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("scores.dat"))) {
            scores = (List<ScoreEntry>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {

        }
    }

    private void saveScore() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            scores.add(new ScoreEntry(name, score));
            Collections.sort(scores);

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("scores.dat"))) {
                oos.writeObject(scores);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ScoreEntry> getScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("scores.dat"))) {
            scores = (List<ScoreEntry>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
        }
        return scores;
    }
}