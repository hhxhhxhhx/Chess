package util;

import javafx.util.Pair;
import piece.Piece;

import java.util.ArrayList;

public class Parser {

    public static ArrayList<String> readPositionPGN(String str) {
        str = str.replaceAll("\\.", ". ");
        String[] split = str.split("\\s+");

        int start = 0;
        if (str.contains(" 1.") || str.contains("\n1.")) {
            System.out.println("Contains these conditions");
            while (!split[start].startsWith("1."))
                start++;
        } else {
            while (start < split.length) {
                ChessBoard cb = new ChessBoard();
                cb.addPieces(Piece.getStandardGamePieces());
                Object[] arr;
                try {
                    String reformat = reformat(split[start], cb, true, "Pf9f9");
                    assert reformat != null;
                    POS startClickPos = new POS(reformat.substring(0, 2));
                    POS endClickPos = new POS(reformat.substring(2, 4));
                    Piece piece = cb.getPiece(startClickPos);
                    arr = Rule.isValidMove(piece, endClickPos, cb, "Pf9f9", false);
                    if ((boolean)arr[0])
                        break;
                } catch (Exception e) {
                    start++;
                }
            }
        }
        int end = start;
        while (end < split.length && !split[end].contains("1-0") && !split[end].contains("0-1") && !split[end].contains("1/2-1/2"))
            end++;
        ArrayList<String> moves = new ArrayList<>();
        for (int i=start;i<end;i++)
            if (!split[i].contains("."))
                moves.add(split[i]);
        if (end < split.length && (split[end].contains("1-0") || split[end].contains("0-1") || split[end].contains("1/2-1/2")))
            moves.add("Result: " + split[end]);
        return moves;
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
