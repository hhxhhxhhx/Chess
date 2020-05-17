package piece;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import util.POS;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Piece {

    protected boolean white;
    protected char pieceName;
    protected Node sprite;

    private static final HashMap<Character, String> pieceNames = new HashMap<>();
    static {
        pieceNames.put('P', "Pawn");
        pieceNames.put('Q', "Queen");
        pieceNames.put('K', "King");
        pieceNames.put('B', "Bishop");
        pieceNames.put('R', "Rook");
        pieceNames.put('N', "Knight");
    }

    protected Piece(boolean white, char pieceName) {
        this.white = white;
        this.pieceName = pieceName;
        ImageView imv = new ImageView();
        imv.setImage(new Image("images/" + (white ? "W" : "B") + pieceName + ".png"));
        imv.setFitWidth(75);
        imv.setFitHeight(75);
        sprite = imv;
    }

    @Override
    public String toString() {
        return (white ? "White " : "Black ") + pieceNames.get(pieceName) + " @" + Integer.toHexString(super.hashCode());
    }


    public String symbol() {
        return Character.toString(pieceName);
    }

    public boolean isWhite() {return white;}
    public boolean isBlack() {return !white;}

    public boolean isKing() {return pieceName == 'K';}
    public boolean isKnight() {return pieceName == 'N';}
    public boolean isRook() {return pieceName == 'R';}
    public boolean isBishop() {return pieceName == 'B';}
    public boolean isQueen() {return pieceName == 'Q';}
    public boolean isPawn() {return  pieceName == 'P';}


    public static Node getSpriteOf(boolean white, char c) {
        if (c == 'Q')
            return (new Queen(white)).sprite;
        else if (c == 'R')
            return (new Rook(white, 0)).sprite;
        else if (c == 'B')
            return (new Bishop(white)).sprite;
        else if (c == 'N')
            return (new Knight(white)).sprite;
        return null;
    }

    public Node sprite() {
        return sprite;
    }

    public Node copySprite() {
        ImageView imv = new ImageView();
        imv.setImage(new Image("images/" + (white ? "W" : "B") + pieceName + ".png"));
        imv.setFitWidth(75);
        imv.setFitHeight(75);
        return imv;
    }

    public boolean isRookA() {return false;}
    public boolean isRookH() {return false;}

    public static ArrayList<Pair<POS, Piece>> getStandardGamePieces() {
        ArrayList<Pair<POS, Piece>> pieces = new ArrayList<>();

        for (int i=10; i<18; i++)
            pieces.add(new Pair<>(new POS(i), new Pawn(false)));
        pieces.add(new Pair<>(new POS("a8"), new Rook(false, 1)));
        pieces.add(new Pair<>(new POS("h8"), new Rook(false, 2)));
        pieces.add(new Pair<>(new POS("b8"), new Knight(false)));
        pieces.add(new Pair<>(new POS("g8"), new Knight(false)));
        pieces.add(new Pair<>(new POS("c8"), new Bishop(false)));
        pieces.add(new Pair<>(new POS("f8"), new Bishop(false)));
        pieces.add(new Pair<>(new POS("d8"), new Queen(false)));
        pieces.add(new Pair<>(new POS("e8"), new King(false)));

        for (int i=60; i<68; i++)
            pieces.add(new Pair<>(new POS(i), new Pawn(true)));
        pieces.add(new Pair<>(new POS("a1"), new Rook(true, 1)));
        pieces.add(new Pair<>(new POS("h1"), new Rook(true, 2)));
        pieces.add(new Pair<>(new POS("b1"), new Knight(true)));
        pieces.add(new Pair<>(new POS("g1"), new Knight(true)));
        pieces.add(new Pair<>(new POS("c1"), new Bishop(true)));
        pieces.add(new Pair<>(new POS("f1"), new Bishop(true)));
        pieces.add(new Pair<>(new POS("d1"), new Queen(true)));
        pieces.add(new Pair<>(new POS("e1"), new King(true)));

        return pieces;
    }
}
