package common.src.main.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class WordUtil {

    private static String words[] = {"delay",
            "vehicle",
            "tribute",
            "balance",
            "replace",
            "auction",
            "hurt",
            "trance",
            "solve",
            "night",
            "wrestle",
            "debt",
            "prefer",
            "glory",
            "endorse",
            "mood",
            "quality",
            "duke",
            "drown",
            "button",
            "slab",
            "concern",
            "ideal",
            "thaw",
            "lake",
            "disco",
            "bother",
            "route",
            "bank",
            "clash",
            "action",
            "shelter",
            "vat",
            "twist",
            "sock",
            "tiptoe",
            "fall",
            "weight",
            "kinship",
            "ritual",
            "pupil",
            "herd",
            "rebel",
            "relate",
            "whisper",
            "record",
            "note",
            "branch",
            "enjoy",
            "butter",
            "mark",
            "ghost",
            "side",
            "move",
            "class",
            "stage",
        "kittens",
        "nation",
        "ink",
        "veil",
        "temper",
        "smash",
        "grain",
        "throat",
        "cake",
        "price",
        "cub",
        "walk",
        "expert",
        "partner",
        "angle",
        "trip",
        "coat",
        "pig",
        "reward",
        "knot",
        "finger",
        "touch",
        "teaching",
        "thumb",
        "stove",
        "brick",
        "shake",
        "night",
        "cover",
        "pets",
        "friction",
        "feeling",
        "person",
        "friend",
        "babies",
        "memory",
        "change",
        "industry",
        "quicksand",
        "scene",
        "blood",
        "berry",
        "stamp",
        "grass",
        "rabbits",
        "oil",
        "hat",
        "year",
        "push",
        "activity",
        "twig",
        "crib",
        "party",
        "pigs",
        "dolls",
        "yak",
        "regret",
        "nut",
        "experience",
        "stem",
        "story",
        "writing",
        "toothbrush",
        "camp",
        "zipper",
        "ants",
        "dust",
        "gun",
        "metal",
        "playground",
        "spoon",
        "soup",
        "cloth",
        "door",
        "reading",
        "insect",
        "birthday",
        "curtain",
        "crowd",
        "cause",
        "question",
        "letters",
        "protest",
        "slave",
        "shame",
        "adjustment",
        "line",
        "driving",
        "mask",
        "animal",
        "minister",
        "snakes"};

    public static String[] generateWords(){
        Random rand = new Random();
        String res[] = new String[3];
        ArrayList<Integer> ints = new ArrayList<>();
        while (ints.size()<3) {
            int newInt = rand.nextInt(words.length);
            if (!ints.contains(newInt)) {
                ints.add(newInt);
            }
        }

        for (int i = 0; i < 3; i++) {
            res[i] = words[ints.get(i)];
        }

        return res;
    }


}
