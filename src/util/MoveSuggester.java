package util;

import java.io.IOException;
import java.util.HashMap;

public class MoveSuggester {

    public static HashMap<String, String> suggestMove(String fen) {

        try {
            Process proc = new ProcessBuilder("python3", "src/util/python.py", fen).start();
            proc.waitFor();
            String str = new String(proc.getInputStream().readAllBytes());
            HashMap<String, String> ret = new HashMap<>();
            ret.put("Score", str.substring(str.indexOf(":") + 2, str.indexOf("Mo") - 1));
            ret.put("Move", str.substring(str.indexOf("Move:") + 6, str.length()-1));
            return ret;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

}
