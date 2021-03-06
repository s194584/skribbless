package common.src.main.DataTransfer;

/**
 *   Data transfer object made to easily transfer information about a user/client in one object
 */

public class User {
    //This is currently a placeholder for some kind of character the player can choose. Not yet implemented
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

    public void addScore(int i) {
        score+=i;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof User))
            return false;
        return getId()==((User) obj).getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

}
