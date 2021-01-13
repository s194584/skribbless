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
            "enjoy"};

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
