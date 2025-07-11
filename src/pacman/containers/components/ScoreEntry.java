package pacman.containers.components;

import java.io.Serializable;

public class ScoreEntry implements Serializable, Comparable<ScoreEntry> {

    private String name;
    private int score;

    public ScoreEntry(String name, int score) {
        this.name = name;
        this.score = score;
    }
    @Override
    public int compareTo(ScoreEntry other) {
        return Integer.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return name + ": " + score;
    }
}