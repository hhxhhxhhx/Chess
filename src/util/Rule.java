package util;

import javafx.util.Pair;
import piece.*;

import java.util.ArrayList;

public final class Rule {

    /**
     * Returns {valid or invalid move (boolean)},
     *         {Pair<>{Moving piece, new position}, Pair<>{Another moving piece, new position}},
     *         {Pair<>{Removed piece, original piece position}, Pair<>{Another removed piece, original piece position}},
     *         Pair<>{Added piece (pawn promotion), new location}
     *
     * If invalid move, it'll simply be a length 1 array of {false}
     * Index 3 (added piece) may be null
     *
     * @param piece piece to move
     * @param endPos endPos
     * @param board chessBoard
     * @return length of 1 or 4 array
     */
    public static Object[] isValidMove(Piece piece, POS endPos, ChessBoard board, String lastMove, boolean...verbose) {
        if (verbose.length == 0)
            return __isValidMoveWithCheckCheck__(piece, endPos, board, lastMove, true, true);
        else
            return __isValidMoveWithCheckCheck__(piece, endPos, board, lastMove, true, verbose[0]);
    }

    @SuppressWarnings("unchecked")
    private static Object[] __isValidMoveWithCheckCheck__(Piece piece, POS endPos, ChessBoard board, String lastMove, boolean checkChecks, boolean verbose) {
        //Check if valid (return new Object[]{false};) if not
        if (board.containsPiece(endPos) && board.getPiece(endPos).isWhite() == piece.isWhite()) {
            //Moving on top of your own piece
            return new Object[]{false};
        }

        lastMove = lastMove.replaceAll(" ", "");
        lastMove = lastMove.replaceAll("\\+", "");
        lastMove = lastMove.replaceAll("#", "");
        lastMove = lastMove.replaceAll("x", "");

        Pair<Piece, POS>[] movingPieces = new Pair[2];
        Pair<Piece, POS>[] removedPieces = new Pair[2];

        POS startPos = board.getPosition(piece);
        if (piece instanceof Knight) {
            if (!isClose(POS.distance(startPos, endPos), Math.sqrt(5))) {
                if (verbose) System.out.println("From Rule: Knight not moving in sqrt(5) dist");
                return new Object[]{false};
            }
        }
        if (piece instanceof Bishop) {
            if (POS.notSameDiagonal(startPos, endPos)) {
                if (verbose) System.out.println("From Rule: Bishop not moving in diagonal");
                return new Object[]{false};
            }
        }
        if (piece instanceof Rook) {
            if (POS.notInLine(startPos, endPos)) {
                if (verbose) System.out.println("From Rule: Rook not moving in a straight line");
                return new Object[]{false};
            }
        }
        if (piece instanceof Queen) {
            if (POS.notInLine(startPos, endPos) && POS.notSameDiagonal(startPos, endPos)) {
                if (verbose) System.out.println("From Rule: Queen not moving in a straight line nor diagonal");
                return new Object[]{false};
            }
        }
        if (piece instanceof Pawn) {
            if (isClose(POS.distance(startPos, endPos), 2)) {
                if (piece.isWhite()) {
                    if (startPos.getRank() != 2 || POS.notSameFile(startPos, endPos) || endPos.getRank() != 4 || board.getPiece(endPos) != null) {
                        if (verbose) System.out.println("From Rule: Pawn moving distance of 2 not valid!");
                        return new Object[]{false};
                    }
                } else if (piece.isBlack()) {
                    if (startPos.getRank() != 7 || POS.notSameFile(startPos, endPos) || endPos.getRank() != 5 || board.getPiece(endPos) != null) {
                        if (verbose) System.out.println("From Rule: Pawn moving distance of 2 not valid!");
                        return new Object[]{false};
                    }
                }
            } else if (isClose(POS.distance(startPos, endPos), 1)) {
                if (POS.notSameFile(startPos, endPos) || board.getPiece(endPos) != null) {
                    if (verbose) System.out.println("From Rule: Pawn moving distance of 1 not valid!");
                    return new Object[]{false};
                }
            } else if (isClose(POS.distance(startPos, endPos), Math.sqrt(2))) {
                if (board.getPiece(endPos) == null) {
                    if (piece.isWhite()) {
                        if (lastMove.charAt(0) != 'P' ||
                                startPos.getRank() != 5 || endPos.getRank() != 6 ||
                                !(new POS(lastMove.substring(1, 3))).equals(new POS(endPos.toString().charAt(0), 7)) ||
                                !(new POS(lastMove.substring(3))).equals(new POS(endPos.toString().charAt(0), 5))) {
                            if (verbose) System.out.println("From Rule: Invalid en passant attempt!");
                            return new Object[]{false};
                        } else  {
                            removedPieces[0] = new Pair<>(board.getPiece(new POS(lastMove.substring(3))), new POS(lastMove.substring(3)));
                        }
                    } else if (piece.isBlack()) {
                        if (lastMove.charAt(0) != 'P' ||
                                startPos.getRank() != 4 || endPos.getRank() != 3 ||
                                !(new POS(lastMove.substring(1, 3))).equals(new POS(endPos.toString().charAt(0), 2)) ||
                                !(new POS(lastMove.substring(3))).equals(new POS(endPos.toString().charAt(0), 4))) {
                            if (verbose) System.out.println("From Rule: Invalid en passant attempt!");
                            return new Object[]{false};
                        } else {
                            removedPieces[0] = new Pair<>(board.getPiece(new POS(lastMove.substring(3))), new POS(lastMove.substring(3)));
                        }
                    }
                }
                if (piece.isWhite()) {
                    if (startPos.getRank() + 1 != endPos.getRank()) {
                        if (verbose) System.out.println("From Rule: Invalid diagonal move attempt!");
                        return new Object[]{false};
                    }
                } else if (piece.isBlack()) {
                    if (startPos.getRank() - 1 != endPos.getRank()) {
                        if (verbose) System.out.println("From Rule: Invalid diagonal move attempt!");
                        return new Object[]{false};
                    }
                }
            } else {
                if (verbose) System.out.println("From Rule: Invalid pawn move! (not move size 2 / sqrt(2) / 1)");
                return new Object[]{false};
            }
        }
        if (piece instanceof King) {
            if (POS.distance(startPos, endPos) > 2.00001) {
                return new Object[]{false};
            }
            if (isClose(POS.distance(startPos, endPos), 2)) {//Castle
                if (piece.isWhite()) {
                    if (!endPos.equals(new POS("g1")) && !endPos.equals(new POS("c1"))) {
                        if (verbose) System.out.println("From Rule: White King not castling to g1 or c1");
                        return new Object[]{false};
                    } else if (endPos.equals(new POS("g1")) &&
                            (!board.canCastleKing(true) || isJumpingOverPieces(startPos, new POS("h1"), board))) {
                        if (verbose) System.out.println("From Rule: White King cannot castle kingside or piece in between!");
                        return new Object[]{false};
                    } else if (endPos.equals(new POS("c1")) &&
                            (!board.canCastleQueen(true) || isJumpingOverPieces(startPos, new POS("a1"), board))) {
                        if (verbose) System.out.println("From Rule: White King cannot castle queenside or piece in between!");
                        return new Object[]{false};
                    } else if (endPos.equals(new POS("g1"))) {
                        //Castle king side
                        if (isSquareTargetableByOpponent(false, board, lastMove, new POS("e1"), new POS("f1"), new POS("g1"))) {
                            if (verbose) System.out.println("From Rule: Cannot castle kingside because a square is targetable by black!");
                            return new Object[]{false};
                        }
                        movingPieces[0] = new Pair<>(piece, endPos);
                        movingPieces[1] = new Pair<>(board.getPiece(new POS("h1")), new POS("f1"));
                    } else {
                        //Castle queen side
                        if (isSquareTargetableByOpponent(false, board, lastMove, new POS("e1"), new POS("d1"), new POS("c1"))) {
                            if (verbose) System.out.println("From Rule: Cannot castle queenside because a square is targetable by black!");
                            return new Object[]{false};
                        }
                        movingPieces[0] = new Pair<>(piece, endPos);
                        movingPieces[1] = new Pair<>(board.getPiece(new POS("a1")), new POS("d1"));
                    }
                } else {
                    if (!endPos.equals(new POS("g8")) && !endPos.equals(new POS("c8"))) {
                        if (verbose) System.out.println("From Rule: Black King not castling to g8 or c8");
                        return new Object[]{false};
                    } else if (endPos.equals(new POS("g8")) &&
                            (!board.canCastleKing(false) || isJumpingOverPieces(startPos, new POS("h8"), board))) {
                        if (verbose) System.out.println("From Rule: Black King cannot castle kingside or piece in between!");
                        return new Object[]{false};
                    } else if (endPos.equals(new POS("c8")) &&
                            (!board.canCastleQueen(false) || isJumpingOverPieces(startPos, new POS("a8"), board))) {
                        if (verbose) System.out.println("From Rule: Black King cannot castle queenside or piece in between!");
                        return new Object[]{false};
                    } else if (endPos.equals(new POS("g8"))) {
                        //Castle king side
                        if (isSquareTargetableByOpponent(true, board, lastMove, new POS("e8"), new POS("f8"), new POS("g8"))) {
                            if (verbose) System.out.println("From Rule: Cannot castle kingside because a square is targetable by white!");
                            return new Object[]{false};
                        }
                        movingPieces[0] = new Pair<>(piece, endPos);
                        movingPieces[1] = new Pair<>(board.getPiece(new POS("h8")), new POS("f8"));
                    } else {
                        //Castle queen side
                        if (isSquareTargetableByOpponent(true, board, lastMove, new POS("e8"), new POS("d8"), new POS("c8"))) {
                            if (verbose) System.out.println("From Rule: Cannot castle queenside because a square is targetable by white!");
                            return new Object[]{false};
                        }
                        movingPieces[0] = new Pair<>(piece, endPos);
                        movingPieces[1] = new Pair<>(board.getPiece(new POS("a8")), new POS("d8"));
                    }
                }
            }
            //Only other case is just moving in any direction by 1 move
        }

        if (!(piece instanceof Knight)) {
            //Check if jumping over pieces
            if (isJumpingOverPieces(startPos, endPos, board)) {
                if (verbose) System.out.println("From Rule: Cannot jump over pieces");
                return new Object[]{false};
            }
        }

        if (movingPieces[0] == null)
            movingPieces[0] = new Pair<>(piece, endPos);

        if (board.containsPiece(endPos)) {
            removedPieces[0] = new Pair<>(board.getPiece(endPos), endPos);
        }

        ChessBoard hypotheticalPosition = board.createTemporaryChange(movingPieces, removedPieces, null);
        if (checkChecks && isSquareTargetableByOpponent(!piece.isWhite(), hypotheticalPosition,
                piece.symbol()+hypotheticalPosition.getPosition(piece)+endPos, hypotheticalPosition.getPositionOfKing(piece.isWhite()))) {
            //Check if committing change will allow enemy to be checking you right now.
            if (verbose) System.out.println("From Rule: Your king will be in check because of this move!");
            return new Object[]{false};
        }

        return new Object[]{true, movingPieces, removedPieces, null};
    }

    private static boolean isSquareTargetableByOpponent(boolean opponent, ChessBoard board, String lastMove, POS...squares) {
        for (POS pos : squares) {
            for (Pair<POS, Piece> pair : board.getAllPieces()) {
                if (pair.getValue() != null && pair.getValue().isWhite() == opponent) {
                    if ((boolean)__isValidMoveWithCheckCheck__(pair.getValue(), pos, board, lastMove, false, false)[0]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isInCheck(boolean white, String lastMove, ChessBoard board) {
        POS kingPos = board.getPositionOfKing(white);
        return isSquareTargetableByOpponent(!white, board, lastMove, kingPos);
    }

    public static boolean hasValidMove(boolean white, String lastMove, ChessBoard board) {
        ArrayList<Pair<POS, Piece>> pieces = board.getAllPieces();
        for (Pair<POS, Piece> pair : pieces) {
            if (pair.getValue().isWhite() == white) {
                for (int i=0;i<8;i++) {
                    for (int j=0;j<8;j++) {
                        POS attemptPos = new POS(i, j);
                        if ((boolean)__isValidMoveWithCheckCheck__(pair.getValue(), attemptPos, board, lastMove, true, false)[0]) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean drawByInsufficientMaterial(ChessBoard board) {

        ArrayList<Pair<POS, Piece>> pieces = board.getAllPieces();
        int[] whitePieceCount = new int[5];
        int[] blackPieceCount = new int[5];
        for (Pair<POS, Piece> pair : pieces) {
            if (pair.getValue().isWhite() && !pair.getValue().isKing()) {
                whitePieceCount[pieceToIndex(pair.getValue())]++;
            } else if (!pair.getValue().isKing()) {
                blackPieceCount[pieceToIndex(pair.getValue())]++;
            }
        }
        if (whitePieceCount[0] != 0 || blackPieceCount[0] != 0 || whitePieceCount[1] != 0 || blackPieceCount[1] != 0 ||
            whitePieceCount[4] != 0 || blackPieceCount[4] != 0)
            return false;
        if (arraySum(whitePieceCount) == 0 && arraySum(blackPieceCount) == 0)
            return true;
        if (arraySum(whitePieceCount, 2) == 0 && whitePieceCount[2] == 1 && arraySum(blackPieceCount) == 0)
            return true;
        if (arraySum(blackPieceCount, 2) == 0 && blackPieceCount[2] == 1 && arraySum(whitePieceCount) == 0)
            return true;
        if (arraySum(whitePieceCount, 3) == 0 && whitePieceCount[3] == 1 && arraySum(blackPieceCount) == 0)
            return true;
        if (arraySum(blackPieceCount, 3) == 0 && blackPieceCount[3] == 1 && arraySum(whitePieceCount) == 0)
            return true;
        if (arraySum(whitePieceCount, 2) == 0 && whitePieceCount[2] == 1 &&
                arraySum(blackPieceCount, 2) == 0 && blackPieceCount[2] == 1) {
            Pair<POS, Piece> bishop1 = null;
            Pair<POS, Piece> bishop2 = null;
            for (Pair<POS, Piece> pair : pieces) {
                if (pair.getValue().isBishop() && bishop1 == null)
                    bishop1 = pair;
                else if (pair.getValue().isBishop())
                    bishop2 = pair;
            }
            assert bishop1 != null;
            assert bishop2 != null;
            int pos1Sum = bishop1.getKey().getValue() / 10 + bishop1.getKey().getValue() % 10;
            int pos2Sum = bishop2.getKey().getValue() / 10 + bishop2.getKey().getValue() % 10;
            return pos1Sum % 2 == pos2Sum % 2;
        }

        return false;
    }

    private static int arraySum(int[] arr, int...exclude) {
        int sum = 0;
        for (int i=0;i<arr.length;i++)
            if (!contains(exclude, i))
                sum += arr[i];
        return sum;
    }

    private static boolean contains(int[] exclude, int n) {
        for (int a : exclude)
            if (a == n)
                return true;
        return false;
    }

    private static int pieceToIndex(Piece p) {
        if (p instanceof Queen)
            return 0;
        else if (p instanceof Rook)
            return 1;
        else if (p instanceof Bishop)
            return 2;
        else if (p instanceof Knight)
            return 3;
        else if (p instanceof Pawn)
            return 4;
        return -1;
    }

    private static boolean isJumpingOverPieces(POS startPos, POS endPos, ChessBoard board) {
        ArrayList<POS> inBetween = POS.between(startPos, endPos);
        for (POS pos : inBetween) {
            if (board.containsPiece(pos)) {
                return true;
            }
        }
        return false;
    }
    public static boolean isClose(double a, double b) {
        return (Math.abs(a - b) < 0.00001);
    }
}
