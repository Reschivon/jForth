package forth;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Aggressor {
    static List<String> profanity = new ArrayList<>();

    static {
        Scanner scan = null;
        try {
            scan = new Scanner(new File(System.getProperty("user.dir") + "/src/forth/aggression.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (scan.hasNext()){
            profanity.add(scan.next());
        }
    }

    static String getOffensiveSlur(){
        return profanity.get((int) (Math.random() * profanity.size()));
    }
}
