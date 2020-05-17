package util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SaveGame {
    public static void save(ArrayList<String> moves) {
        try {
            File f = new File("src/util/games.pgn");
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            StringBuilder pgn = new StringBuilder();

            pgn.append("[Date \"").append(new SimpleDateFormat("yyyy.MM.dd").format(Calendar.getInstance().getTime())).append("\"]\n");
            pgn.append("[Time \"").append(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())).append("\"]\n");
            pgn.append("[Result \"").append(moves.get(moves.size() - 1)).append("\"]\n");
            pgn.append("\n");

            int moveNum = 1;
            boolean white = true;

            StringBuilder line = new StringBuilder();
            for (int i=0;i<moves.size() - 1; i++) {
                if (white) {
                    line.append(moveNum).append(". ");
                    moveNum++;
                }
                line.append(moves.get(i)).append(" ");
                if (line.length() >= 72) {
                    pgn.append(line).append("\n");
                    line = new StringBuilder();
                }
                white = !white;
            }
            pgn.append(line);
            pgn.append(moves.get(moves.size() - 1));
            pgn.append("\n");
            pw.println(pgn);
            pw.close();
            System.out.println("Saved to util/games.pgn!");
        } catch (Exception e) {
            System.out.println("Could not save pgn: " + e.getMessage());
        }
    }
}
