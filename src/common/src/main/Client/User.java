package common.src.main.Client;

public class User {
    private final String character;
    private final String name;
    private final int id;
    private boolean leader;
    private int score;

    public User(String character, String name, int id, int score) {
        this.character = character;
        this.name = name;
        this.id = id;
        this.score = score;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
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

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return getId()==((User) obj).getId();
    }
}
