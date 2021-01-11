package common.src.main.Client;

public class User {
    private final String character;
    private final String name;
    private int score;

    public User(String character, String name, int score) {
        this.character = character;
        this.name = name;
        this.score = score;
    }

    public String getCharacter() {
        return character;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
