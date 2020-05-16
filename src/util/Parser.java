package util;

import game.Game;
import javafx.util.Pair;
import piece.Piece;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static ArrayList<String> readPGN(Game game, ChessBoard board, int gameToLoad) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("src/Tal.pgn"));
            StringBuilder sb = new StringBuilder();
            lines.forEach(e -> sb.append(e.replaceAll("\\.", ". ") + "\n"));

            String content = sb.toString();

            //System.out.println(content);

            String[] split = content.split("\\s+");

            int start = 0;

            for (int i=0;i<=gameToLoad;i++) {
                if (i != 0)
                    start++;
                while (!split[start].equals("1."))
                    start++;
            }

            int resultIndex = start;
            while (!(split[resultIndex].equals("1-0") || split[resultIndex].equals("0-1") || split[resultIndex].equals("1/2-1/2")))
                resultIndex++;

            ArrayList<String> moves = new ArrayList<>();
            for (int i = start; i < resultIndex; i++) {
                if (!split[i].contains("."))
                    moves.add(split[i]);
            }
            moves.add("Result: " + split[resultIndex]);
            return moves;
            //System.out.println("Result: " + split[resultIndex]);
        } catch (Exception e) {
            System.out.println("Caught exception " + e.getMessage());
            return null;
        }
    }

    public static String reformat(String str, ChessBoard board, boolean white, String lastMove) {

        if (str.equals(""))
            return null;

        str = str.substring(str.indexOf(".") + 1);
        final String originalStr = str;

        //boolean checkMate = str.contains("++") || str.contains("#");
        //boolean check = (checkMate ? false : str.contains("+"));

        //Remove spaces, #'s, +'s, and x's.
        str = str.replaceAll(" ", "").replaceAll("#", "").replaceAll("\\+", "").replaceAll("x", "");

        /*
        Castling
         */
        if (str.equals("O-O-O") || str.equals("0-0-0")) {
            if (white)
                return "e1c1";
            else
                return "e8c8";
            /*
            System.out.print("Queen side castle");
            if (check || checkMate) {
                System.out.println(check ? " with check" : " with checkmate");
            }*/
        } else if (str.equals("O-O") || str.equals("0-0")) {
            if (white)
                return "e1g1";
            else
                return "e8g8";
            /*System.out.println("King side castle");
            if (check || checkMate) {
                System.out.println(check ? " with check" : " with checkmate");
            }*/
        }

        /*
        Not castling
         */
        char pieceSymbol;

        if (str.charAt(0) < 'a' || str.charAt(0) > 'h') {
            //First piece has symbol
            pieceSymbol = str.charAt(0);
            str = str.substring(1);
        } else {
            pieceSymbol = 'P';
        }

        String pawnPromo = "";
        if (str.contains("=")) {
            pawnPromo = str.substring(str.length() - 2);
            str = str.substring(0, str.length() - 2);
        }

        if (pawnPromo.length() > 0 && pieceSymbol != 'P')
            return null;

        //System.out.println("pieceSymbol: " + pieceSymbol);

        String lastCoord = getLastCoordinate(str);

        if (lastCoord == null) {
            throw new RuntimeException("Invalid expression: " + originalStr);
        }

        if (!str.endsWith(lastCoord)) {
            pawnPromo = "=" + str.substring(str.length() - 1).toUpperCase();
        }
        //System.out.println("lastCoord: " + lastCoord);

        str = str.substring(0, str.length() - 2);

        //System.out.println("Remaining string: " + str);

        int requiredRank = -1;
        char requiredChar = 'z';

        if (str.length() == 2) {
            requiredChar = str.charAt(0);
            requiredRank = Integer.parseInt(str.substring(1));
        } else if (str.length() == 1) {
            try {
                requiredRank = Integer.parseInt(str);
            } catch (Exception e) {
                requiredRank = -1;
                requiredChar = str.charAt(0);
            }
        }

        String retStr = null;
        ArrayList<Pair<POS, Piece>> pieces = board.getAllPieces();
        for (Pair<POS, Piece> p : pieces) {
            POS pos = p.getKey();
            Piece piece = p.getValue();
            if (piece.isWhite() != white)
                continue;
            if (piece.symbol().charAt(0) != pieceSymbol)
                continue;
            if (requiredRank != -1 && pos.getRank() != requiredRank)
                continue;
            if (requiredChar != 'z' && pos.getFile() != requiredChar)
                continue;
            if ((boolean)Rule.isValidMove(piece, new POS(lastCoord), board, lastMove, false)[0]) {
                if (retStr == null)
                    retStr = pos + lastCoord + pawnPromo.toUpperCase();
                else
                    return null;
            }
        }
        return retStr;
    }

    public static String getLastCoordinate(String str) {
        try {
            for (int i = str.length() - 1; i >= 0; i--) {
                if (str.charAt(i) >= 'a' && str.charAt(i) <= 'h') {
                    return str.substring(i, i+2);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
