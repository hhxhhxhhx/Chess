package util;

import javafx.util.Pair;
import piece.*;

import java.util.ArrayList;
import java.util.Collection;

public class ChessBoard {

    private BiHashMap<POS, Piece> board = new BiHashMap<>();

    private boolean whiteRookAMoved = false;
    private boolean whiteRookHMoved = false;
    private boolean whiteKingMoved = false;
    private boolean blackRookAMoved = false;
    private boolean blackRookHMoved = false;
    private boolean blackKingMoved = false;

    public boolean canCastleKing(boolean white) {
        if (white)
            return !whiteKingMoved && !whiteRookHMoved;
        else
            return !blackKingMoved && !blackRookHMoved;
    }
    public boolean canCastleQueen(boolean white) {
        if (white)
            return !whiteKingMoved && !whiteRookAMoved;
        else
            return !blackKingMoved && !blackRookAMoved;
    }

    public POS getPositionOfKing(boolean white) {
        for (Piece p : board.valueSet()) {
            if (p.isWhite() == white && p.isKing()) {
                return board.getReverse(p);
            }
        }
        return null;
    }

    @Override
    public ChessBoard clone() {
        ChessBoard newBoard = new ChessBoard();
        newBoard.board = this.board.clone();
        newBoard.whiteRookAMoved = this.whiteRookAMoved;
        newBoard.whiteRookHMoved = this.whiteRookHMoved;
        newBoard.whiteKingMoved = this.whiteKingMoved;
        newBoard.blackRookAMoved = this.blackRookAMoved;
        newBoard.blackRookHMoved = this.blackRookHMoved;
        newBoard.blackKingMoved = this.blackKingMoved;
        return newBoard;
    }

    public ChessBoard createTemporaryChange(Pair<Piece, POS>[] moving, Pair<Piece, POS>[] removed, Pair<Piece, POS> added) {

        ChessBoard clone = this.clone();

        for (Pair<Piece, POS> pair : removed) {
            if (pair != null) {
                clone.removePiece(pair.getKey());
            }
        }
        for (Pair<Piece, POS> pair : moving) {
            if (pair != null)
                clone.movePiece(pair.getKey(), pair.getValue());
        }
        if (added != null)
            clone.addPiece(added);

        return clone;
    }

    public void addPiece(Pair<Piece, POS> piece) {
        board.add(piece.getValue(), piece.getKey());
    }
    public void addPieces(Collection<Pair<POS, Piece>> maps) {
        maps.forEach(board::add);
    }
    public void removePiece(Piece p) {
        board.removeReverse(p);
    }
    public void changePiece(Piece original, Piece newPiece) {
        board.replaceReverse(original, newPiece);
    }
    public void movePiece(Piece piece, POS pos) {
        if (piece instanceof Rook) {
            Rook rook = (Rook)piece;
            if (rook.isRookA() && rook.isWhite())
                whiteRookAMoved = true;
            else if (rook.isRookH() && rook.isWhite())
                whiteRookHMoved = true;
            else if (rook.isRookA() && rook.isBlack())
                blackRookAMoved = true;
            else if (rook.isRookH() && rook.isBlack())
                blackRookHMoved = true;
        } else if (piece instanceof King && piece.isWhite())
            whiteKingMoved = true;
        else if (piece instanceof King && !piece.isWhite())
            blackKingMoved = true;
        board.reMapReverse(piece, pos);
    }
    public Piece getPiece(POS pos) {
        return board.getForward(pos);
    }
    public Piece getPiece(String pos) {
        return board.getForward(new POS(pos));
    }
    public POS getPosition(Piece piece) {
        return board.getReverse(piece);
    }
    public boolean containsPiece(POS pos) {
        return board.containsKey(pos);
    }
    public ArrayList<Pair<POS, Piece>> getAllPieces() {
        ArrayList<Pair<POS, Piece>> pieces = new ArrayList<>();
        board.keySet().forEach(e -> pieces.add(new Pair<>(e, board.getForward(e))));
        return pieces;
    }
    public ArrayList<Pair<POS, Piece>> getAllMappings() {
        ArrayList<Pair<POS, Piece>> pieces = new ArrayList<>();
        for (int i=0;i<78;i++) {
            pieces.add(new Pair<>(new POS(i), board.getForward(new POS(i))));
        }
        return pieces;
    }
}
