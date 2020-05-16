package piece;

public class Rook extends Piece {

    /**
     * 0 if neither rookA or rookH (promoted from pawn)
     * 1 if rookA
     * 2 if rookH
     */
    private final int rookA;

    public Rook(boolean white, int rookA) {
        super(white, 'R');
        this.rookA = rookA;
    }

    @Override
    public boolean isRookA() {
        return rookA == 1;
    }

    @Override
    public boolean isRookH() {
        return rookA == 2;
    }
}
