package util;

import java.util.ArrayList;

public class POS {
    /**
     * +++++++++++++++++++++++
     * a8 b8 c8 d8 e8 f8 g8 h8
     * a7 b7 c7 d7 e7 f7 g7 h7
     * a6 b6 c6 d6 e6 f6 g6 h6
     * a5 b5 c5 d5 e5 f5 g5 h5
     * a4 b4 c4 d4 e4 f4 g4 h4
     * a3 b3 c3 d3 e3 f3 g3 h3
     * a2 b2 c2 d2 e2 f2 g2 h2
     * a1 b1 c1 d1 e1 f1 g1 h1
     * +++++++++++++++++++++++
     *
     * +++++++++++++++++++++++
     * 00 01 02 03 04 05 06 07
     * 10 11 12 13 14 15 16 17
     * 20 21 22 23 24 25 26 27
     * 30 31 32 33 34 35 36 37
     * 40 41 42 43 44 45 46 47
     * 50 51 52 53 54 55 56 57
     * 60 61 62 63 64 65 66 67
     * 70 71 72 73 74 75 76 77
     * +++++++++++++++++++++++
     */

    private int value;

    public POS(String pos) {
        try {
            value = Integer.parseInt(pos);
        } catch (Exception e) {
            char letter = pos.charAt(0);
            int num = Integer.parseInt(pos.substring(1));
            value = (8 - num) * 10 + (letter - 'a');
        }
    }
    public POS(char c, int pos) {
        value = (8 - pos) * 10 + (c - 'a');
    }
    public POS(int pos) {
        value = pos;
    }
    public POS(int pos1, int pos2) {
        value = pos1 * 10 + pos2;
    }

    @Override
    public String toString() {
        char letter = (char)(value % 10 + 'a');
        int num = 8 - value / 10;
        return String.valueOf(letter) + num;
    }

    public int getValue() {
        return value;
    }

    @SuppressWarnings("all")
    public static double distance(POS a, POS b) {
        return Math.hypot(a.value % 10 - b.value % 10, a.value / 10 - b.value / 10);
    }

    public static boolean notSameDiagonal(POS a, POS b) {
        return Math.abs(a.value % 10 - b.value % 10) != Math.abs(a.value / 10 - b.value / 10);
    }

    public char getFile() {
        return (char)(value % 10 + 'a');
    }

    public int getRank() {
        return 8 - value / 10;
    }

    public static boolean notSameFile(POS a, POS b) {
        return a.value % 10 != b.value % 10;
    }

    public static boolean notInLine(POS a, POS b) {
        return (a.equals(b) || (a.value % 10 - b.value % 10 != 0 && a.value / 10 - b.value / 10 != 0));
    }

    public static ArrayList<POS> between(POS a, POS b) {
        if (notSameDiagonal(a, b) && notInLine(a, b)) {
            throw new RuntimeException(a + " and " + b + " are not in a straight line or diagonal!");
        } else {
            ArrayList<POS> points = new ArrayList<>();
            for (int i=0;i<8;i++) {
                for (int j=0;j<8;j++) {
                    POS newPos = new POS(i, j);
                    if (newPos.equals(a) || newPos.equals(b))
                        continue;
                    if (Rule.isClose(distance(a, b), distance(a, newPos) + distance(b, newPos)))
                        points.add(new POS(i, j));
                }
            }
            return points;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof POS && ((POS)o).value == this.value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
