package game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import piece.*;
import util.ChessBoard;
import util.POS;
import util.Parser;
import util.Rule;

import java.util.ArrayList;

public class Game extends Application {

    private ArrayList<String> gameMoves = new ArrayList<>();

    private Pane pane = new Pane();
    private StackPane[] boardSquare;
    private StackPane[] boardLabels;

    private Scene scene;
    private Stage stage;

    private ChessBoard board;

    private Pair<Double, Double> startClick = null;
    private Pair<Double, Double> endClick = null;

    private boolean white = true;

    private ArrayList<Node> temporaryNodes = new ArrayList<>();

    private Pair<Node, POS> temporaryRemovedNode = null;

    private String lastMove = "Pf9f9";

    private POS pieceInCheckPos = null;

    private TextField inputField = new TextField();

    private Button loadPGN = new Button("Load PGN");

    private int gameToLoad = 600;
    private int loadIndex = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {

        loadPGN.setOnAction(e -> {
            board = new ChessBoard();
            board.addPieces(Piece.getStandardGamePieces());
            updateStackPanes();
            ArrayList<String> moves = Parser.readPGN(this, board, gameToLoad);
            gameToLoad++;
            loadIndex = 0;
            white = true;
            Timeline tl = new Timeline(new KeyFrame(Duration.millis(200), ae -> {
                System.out.println(moves.get(loadIndex));
                attemptMove(Parser.reformat(moves.get(loadIndex), board, white, lastMove));
                loadIndex++;
                updateClickedPane();
                updateStackPanes();
            }));
            tl.setCycleCount(moves.size() - 1);
            tl.play();
            tl.setOnFinished(ef -> System.out.println(moves.get(moves.size() - 1)));
        });
        loadPGN.setLayoutX(75);
        loadPGN.setLayoutY(710);
        loadPGN.setFont(new Font(20));
        pane.getChildren().add(loadPGN);

        inputField.setLayoutX(75);
        inputField.setLayoutY(710);
        inputField.setFont(new Font(20));
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                attemptMove(Parser.reformat(inputField.getText(), board, white, lastMove));
                inputField.setText("");
            }
        });
        //pane.getChildren().add(inputField);

        boardLabels = new StackPane[16];
        for (int i=0;i<8;i++) {
            StackPane sp = new StackPane();
            sp.setMinSize(30, 75);
            sp.setLayoutX(45);
            sp.setLayoutY(75+(525 - 75*i));
            Label lb = new Label(String.valueOf(i+1));
            lb.setFont(new Font(20));
            sp.getChildren().add(lb);
            boardLabels[i] = sp;
        }
        for (int i=8;i<16;i++) {
            StackPane sp = new StackPane();
            sp.setMinSize(75, 30);
            sp.setLayoutX(75+75*(i-8));
            sp.setLayoutY(675);
            Label lb = new Label(Character.toString('a' + (i - 8)));
            lb.setFont(new Font(20));
            sp.getChildren().add(lb);
            boardLabels[i] = sp;
        }
        pane.getChildren().addAll(boardLabels);

        board = new ChessBoard();
        boardSquare = new StackPane[78];//index 0 - 77
        for (int i=0;i<8;i++) {
            for (int j=0;j<8;j++) {
                StackPane sp = new StackPane();
                sp.setMinSize(75, 75);
                sp.setMaxSize(75, 75);
                sp.setLayoutX(75 + 75 * j);
                sp.setLayoutY(75 + 75 * i);
                sp.setId(String.valueOf(new POS(i, j)));
                if ((i + j) % 2 == 0)
                    sp.setBackground(new Background(new BackgroundFill(Color.rgb(238, 238, 210), null, null)));
                else
                    sp.setBackground(new Background(new BackgroundFill(Color.rgb(118, 150, 86), null, null)));
                boardSquare[i * 10 + j] = sp;
                pane.getChildren().add(boardSquare[i * 10 + j]);
            }
        }
        board.addPieces(Piece.getStandardGamePieces());
        updateStackPanes();

        scene = new Scene(pane, 750, 750);
        initSceneHandler();
        stage = primaryStage;
        stage.setScene(scene);
        stage.show();
    }

    private void initSceneHandler() {
        scene.setOnMousePressed(e -> {
            if (startClick == null) {
                startClick = new Pair<>(e.getSceneX(), e.getSceneY());
                Piece pieceClicked = board.getPiece(posFromClick(startClick));
                if (pieceClicked == null || pieceClicked.isWhite() != white) {
                    startClick = null;
                } else {
                    addHoveringPiece();
                }
            }
            updateClickedPane();
        });
        scene.setOnMouseReleased(e -> {
            if (startClick != null &&
                    Math.abs(startClick.getValue() - e.getSceneY()) < 3 &&
                    Math.abs(startClick.getKey() - e.getSceneX()) < 3) {
            } else if (startClick != null) {
                endClick = new Pair<>(e.getSceneX(), e.getSceneY());
                attemptMove();
                removeHoveringPiece();
                scene.setCursor(Cursor.DEFAULT);
            }
            updateClickedPane();
            updateStackPanes();
        });
        scene.setOnMouseMoved(e -> {
            if (temporaryNodes.size() > 0) {
                temporaryNodes.get(0).setLayoutX(e.getSceneX() - 37.5);
                temporaryNodes.get(0).setLayoutY(e.getSceneY() - 37.5);
            }
        });
        scene.setOnMouseDragged(e -> {
            if (temporaryNodes.size() > 0) {
                temporaryNodes.get(0).setLayoutX(e.getSceneX() - 37.5);
                temporaryNodes.get(0).setLayoutY(e.getSceneY() - 37.5);
            }
        });
    }

    private void updateClickedPane() {
        POS lastMovePos1 = new POS(lastMove.substring(1, 3));
        POS lastMovePos2 = new POS(lastMove.replaceAll("x", "").substring(3, 5));
        for (int i=0;i<8;i++) {
            for (int j=0;j<8;j++) {
                POS thisPos = new POS(i, j);
                if (startClick != null && thisPos.equals(posFromClick(startClick)))
                    boardSquare[i*10+j].setBackground(new Background(new BackgroundFill(Color.DARKTURQUOISE, null, null)));
                else if (pieceInCheckPos != null && pieceInCheckPos.equals(thisPos))
                    boardSquare[i*10+j].setBackground(new Background(new BackgroundFill(Color.rgb(242, 51, 45), null, null)));
                else if (thisPos.equals(lastMovePos1) || thisPos.equals(lastMovePos2))
                    boardSquare[i*10+j].setBackground(new Background(new BackgroundFill(Color.rgb(207, 207, 127), null, null)));
                else if ((i + j) % 2 == 0)
                    boardSquare[i*10+j].setBackground(new Background(new BackgroundFill(Color.rgb(238, 238, 210), null, null)));
                else
                    boardSquare[i*10+j].setBackground(new Background(new BackgroundFill(Color.rgb(118, 150, 86), null, null)));
            }
        }
    }

    private void addHoveringPiece() {
        try {
            if (temporaryNodes.size() == 0) {
                POS clickPos = posFromClick(startClick);
                Piece clickedPiece = board.getPiece(clickPos);//Should never be null
                temporaryNodes.add(clickedPiece.copySprite());
                temporaryNodes.get(0).setLayoutX(startClick.getKey() - 37.5);
                temporaryNodes.get(0).setLayoutY(startClick.getValue() - 37.5);
                pane.getChildren().addAll(temporaryNodes);
                this.temporaryRemovedNode = new Pair<>(boardSquare[clickPos.getValue()].getChildren().remove(0), clickPos);
                boardSquare[clickPos.getValue()].getChildren().clear();
                scene.setCursor(Cursor.CROSSHAIR);
            }
        } catch (IllegalArgumentException e) {
            //Ignore
        }
    }

    private void removeHoveringPiece() {
        scene.setCursor(Cursor.DEFAULT);
        temporaryNodes.get(0).setLayoutX(-500);
        pane.getChildren().removeAll(temporaryNodes);
        temporaryNodes.clear();
        temporaryRemovedNode = null;
    }

    public void attemptMove(String...command) {
        try {
            POS startClickPos;
            if (command.length == 0)
                startClickPos = posFromClick(startClick);
            else
                startClickPos = new POS(command[0].substring(0, 2));
            POS endClickPos;
            if (command.length == 0)
                endClickPos = posFromClick(endClick);
            else
                endClickPos = new POS(command[0].substring(2, 4));
            if (startClickPos.equals(endClickPos)) {
                System.out.println("Nullified 2 clicks on " + startClickPos + " and " + endClickPos);
            } else {
                if (command.length == 0)
                    System.out.println("Moving from " + startClickPos + " to " + endClickPos);


                Piece movingPiece = board.getPiece(startClickPos);
                Object[] ruleCheck = Rule.isValidMove(movingPiece, endClickPos, board, lastMove);
                if ((boolean) ruleCheck[0]) {
                    lastMove = board.getPiece(startClickPos).symbol() + startClickPos + endClickPos;
                    Pair<Piece, POS>[] movingDirections = (Pair<Piece, POS>[]) ruleCheck[1];
                    Pair<Piece, POS>[] removedPieces = (Pair<Piece, POS>[]) ruleCheck[2];
                    Pair<Piece, POS> addedPiece = (Pair<Piece, POS>) ruleCheck[3];

                    //Promotion
                    if (movingPiece.isWhite() && movingPiece.isPawn() && endClickPos.getRank() == 8 ||
                            movingPiece.isBlack() && movingPiece.isPawn() && endClickPos.getRank() == 1) {

                        if (command.length != 0) {
                            removedPieces[1] = new Pair<>(movingPiece, startClickPos);
                            movingDirections[0] = null;
                            Pair<Piece, POS> addPiece;
                            try {
                                char c = command[0].charAt(5);

                                if (c == 'Q') {
                                    addPiece = new Pair<>(new Queen(movingPiece.isWhite()), endClickPos);
                                } else if (c == 'R') {
                                    addPiece = new Pair<>(new Rook(movingPiece.isWhite(), 0), endClickPos);
                                } else if (c == 'B') {
                                    addPiece = new Pair<>(new Bishop(movingPiece.isWhite()), endClickPos);
                                } else {
                                    addPiece = new Pair<>(new Knight(movingPiece.isWhite()), endClickPos);
                                }
                            } catch (Exception e) {
                                System.out.println("defaulted to making queen");
                                addPiece = new Pair<>(new Queen(movingPiece.isWhite()), endClickPos);
                            }
                            commitChanges(removedPieces, movingDirections, addPiece);
                        } else {
                            Stage popup = new Stage();
                            VBox vb = new VBox();
                            vb.setSpacing(15);
                            vb.setAlignment(Pos.CENTER);
                            Label text = new Label("What to promote your " + endClickPos + " pawn to?");
                            text.setFont(new Font(20));
                            Node queen = Piece.getSpriteOf(movingPiece.isWhite(), 'Q');
                            queen.setOnMouseClicked(e -> {
                                removedPieces[1] = new Pair<>(movingPiece, startClickPos);
                                movingDirections[0] = null;
                                Pair<Piece, POS> addPiece = new Pair<>(new Queen(movingPiece.isWhite()), endClickPos);
                                commitChanges(removedPieces, movingDirections, addPiece);
                                popup.close();
                            });
                            Node rook = Piece.getSpriteOf(movingPiece.isWhite(), 'R');
                            rook.setOnMouseClicked(e -> {
                                removedPieces[1] = new Pair<>(movingPiece, startClickPos);
                                movingDirections[0] = null;
                                Pair<Piece, POS> addPiece = new Pair<>(new Rook(movingPiece.isWhite(), 0), endClickPos);
                                commitChanges(removedPieces, movingDirections, addPiece);
                                popup.close();
                            });
                            Node bishop = Piece.getSpriteOf(movingPiece.isWhite(), 'B');
                            bishop.setOnMouseClicked(e -> {
                                removedPieces[1] = new Pair<>(movingPiece, startClickPos);
                                movingDirections[0] = null;
                                Pair<Piece, POS> addPiece = new Pair<>(new Bishop(movingPiece.isWhite()), endClickPos);
                                commitChanges(removedPieces, movingDirections, addPiece);
                                popup.close();
                            });
                            Node knight = Piece.getSpriteOf(movingPiece.isWhite(), 'N');
                            knight.setOnMouseClicked(e -> {
                                removedPieces[1] = new Pair<>(movingPiece, startClickPos);
                                movingDirections[0] = null;
                                Pair<Piece, POS> addPiece = new Pair<>(new Knight(movingPiece.isWhite()), endClickPos);
                                commitChanges(removedPieces, movingDirections, addPiece);
                                popup.close();
                            });
                            vb.getChildren().addAll(text, queen, rook, bishop, knight);
                            Scene newScene = new Scene(vb, 400, 400);
                            popup.initModality(Modality.APPLICATION_MODAL);
                            popup.setScene(newScene);
                            popup.show();
                        }
                    } else {
                        commitChanges(removedPieces, movingDirections, addedPiece);
                    }

                } else {
                    System.out.println("Illegal move!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getClass() + " at attemptMove. Most likely due to invalid command.");
        }
        startClick = null;
        endClick = null;
    }

    private void commitChanges(Pair<Piece, POS>[] removedPieces, Pair<Piece, POS>[] movingDirections, Pair<Piece, POS> addedPiece) {
        if (removedPieces[0] != null) {
            lastMove = lastMove.substring(0, 3) + "x" + lastMove.substring(3);
        }
        for (Pair<Piece, POS> pair : removedPieces) {
            if (pair != null) {
                board.removePiece(pair.getKey());
            }
        }
        for (Pair<Piece, POS> pair : movingDirections) {
            if (pair != null)
                board.movePiece(pair.getKey(), pair.getValue());
        }
        if (addedPiece != null) {
            board.addPiece(addedPiece);
            lastMove += "=" + addedPiece.getKey().symbol();
        }

        white = !white;
        updateStackPanes();

        pieceInCheckPos = null;

        if (Rule.isInCheck(true, lastMove, board)) {
            pieceInCheckPos = board.getPositionOfKing(true);
            if (Rule.hasValidMove(true, lastMove, board)) {
                System.out.println("White is in check!");
                gameMoves.add(replaceCastleMoves(lastMove) + "+");
            } else {
                System.out.println("White is checkmated!");
                gameMoves.add(replaceCastleMoves(lastMove) + "#");
                gameMoves.add("0-1");
                scene.setOnMousePressed(e -> {});
                scene.setOnMouseDragged(e -> {});
                scene.setOnMouseMoved(e -> {});
                scene.setOnMouseReleased(e -> {});
                System.out.println(gameMoves);
            }
        } else if (Rule.isInCheck(false, lastMove, board)) {
            pieceInCheckPos = board.getPositionOfKing(false);
            if (Rule.hasValidMove(false, lastMove, board)) {
                System.out.println("Black is in check!");
                gameMoves.add(replaceCastleMoves(lastMove) + "+");
            } else {
                System.out.println("Black is checkmated!");
                gameMoves.add(replaceCastleMoves(lastMove) + "#");
                gameMoves.add("1-0");
                scene.setOnMousePressed(e -> {});
                scene.setOnMouseDragged(e -> {});
                scene.setOnMouseMoved(e -> {});
                scene.setOnMouseReleased(e -> {});
                System.out.println(gameMoves);
            }
        } else if (!Rule.hasValidMove(white, lastMove, board)) {
            //Stalemate
            System.out.println("Stalemate!");
            gameMoves.add("1/2-1/2");
            scene.setOnMousePressed(e -> {});
            scene.setOnMouseDragged(e -> {});
            scene.setOnMouseMoved(e -> {});
            scene.setOnMouseReleased(e -> {});
            System.out.println(gameMoves);
        } else {
            gameMoves.add(replaceCastleMoves(lastMove));
        }
        updateClickedPane();
    }

    private POS posFromClick(Pair<Double, Double> clickPos) {
        return new POS((int)(clickPos.getValue() - 75) / 75, (int)(clickPos.getKey() - 75) / 75);
    }

    public void updateStackPanes() {
        ArrayList<Pair<POS, Piece>> pieces = board.getAllMappings();
        pieces.forEach(pos -> {

            if (pos != null && pos.getValue() != null &&
                (this.temporaryRemovedNode == null || !this.temporaryRemovedNode.getValue().equals(pos.getKey()))) {
                boardSquare[pos.getKey().getValue()].getChildren().clear();
                boardSquare[pos.getKey().getValue()].getChildren().add(pos.getValue().sprite());
            } else if (pos != null && pos.getValue() == null && boardSquare[pos.getKey().getValue()] != null) {
                //System.out.println(pos.getKey().getValue());
                boardSquare[pos.getKey().getValue()].getChildren().clear();
            }
        });
    }

    private String replaceCastleMoves(String str) {
        str = str.replaceAll("Ke1g1", "O-O");
        str = str.replaceAll("Ke1c1", "O-O-O");
        str = str.replaceAll("Ke8g8", "O-O");
        str = str.replaceAll("Ke8c8", "O-O-O");
        return str;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
