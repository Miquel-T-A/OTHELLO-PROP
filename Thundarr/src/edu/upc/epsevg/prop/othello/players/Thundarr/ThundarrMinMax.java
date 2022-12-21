package edu.upc.epsevg.prop.othello.players.Thundarr;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Jugador aleatori
 * 
 * @author bernat
 */
public class ThundarrMinMax implements IPlayer, IAuto {

    private String name;
    private GameStatus s;
    private CellType myType;
    private CellType hisType;
    private static int BOARD_SIZE = 8;
    private int minEval = Integer.MIN_VALUE;
    private int maxEval = Integer.MAX_VALUE;
    private int V[][];
    private int depth;

    public ThundarrMinMax(int depth) {

        V = new int[8][8];

        // static weights

        // Matriz de puntuaciones

        V[0] = new int[] { 100, -3, 11, 8, 8, 11, -3, 100 };
        V[1] = new int[] { -3, -7, -4, 1, 1, -4, -7, -3 };
        V[2] = new int[] { 11, -4, 2, 2, 2, 2, -4, 11 };
        V[3] = new int[] { 8, 1, 2, -3, -3, 2, 1, 8 };
        V[4] = new int[] { 8, 1, 2, -3, -3, 2, 1, 8 };
        V[5] = new int[] { 11, -4, 2, 2, 2, 2, -4, 11 };
        V[6] = new int[] { -3, -7, -4, 1, 1, -4, -7, -3 };
        V[7] = new int[] { 100, -3, 11, 8, 8, 11, -3, 100 };

        this.name = getName();
        this.depth = depth;
    }

    @Override
    public String getName() {
        return "Thundarr";
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        myType = s.getCurrentPlayer();
        hisType = CellType.opposite(myType);
        Point mov = triaPosicio(s, depth);
        return new Move(mov, 0L, 0, SearchType.MINIMAX);
    }

    Point triaPosicio(GameStatus s, int depth) {
        int maxEval = Integer.MIN_VALUE;
        Point bestMove = new Point();
        ArrayList<Point> moves = s.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
            int eval = minMinimax(fill, depth - 1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            if (eval > maxEval) {
                maxEval = eval;
                bestMove = moves.get(i);
            }
        }
        return bestMove;
    }

    int minMinimax(GameStatus s, int depth, int beta, int alpha) {
        if (s.isGameOver()) { // ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 9999999;
            else // Guanya el contrincant
                return -9999999;
        } else if (depth == 0) { // no hi ha moviments possibles o profunditat es 0
            int valor = heuristica(s);
            return valor;
        }
        minEval = Integer.MAX_VALUE;
        ArrayList<Point> moves = s.getMoves();

        // Iterem sobre tots el moviments nous posibles
        for (int i = 0; i < moves.size(); i++) {
            GameStatus s_aux = new GameStatus(s);
            // Movem la peça en el status auxiliar
            s_aux.movePiece(moves.get(i));
            minEval = Math.min(minEval, maxMiniMax(s_aux, depth - 1, beta, alpha));
            beta = Math.min(beta, minEval);
            if (alpha >= beta) {
                break;
            }
        }
        return minEval;
    }

    int maxMiniMax(GameStatus s, int depth, int beta, int alpha) {

        if (s.isGameOver()) { // Ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 9999999;
            else // Guanya el contrincant
                return -9999999;
        } else if (depth == 0) { // no hi ha moviments possibles o profunditat es 0
            int valor = heuristica(s);
            return valor;
        }

        maxEval = Integer.MIN_VALUE + 1;
        ArrayList<Point> moves = s.getMoves();

        // Iterem sobre tots el moviments nous posibles
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
            maxEval = Math.max(maxEval, minMinimax(fill, depth - 1, beta, alpha));
            alpha = Math.max(alpha, maxEval);
            if (alpha >= beta) {
                break;
            }
        }
        return maxEval;
    }

    public int heuristica(GameStatus s) {
        int puntuacio = 0;
        int stability = 0;
        int pecesnostres = 0;
        int pecescontrari = 0;
        int blackMoves = 0;
        int whiteMoves = 0;
        int percentatge = 0;
        int permanent = 0;
        // Heuristica 1: Contar el numero de peces del tauler
        // Heuristica 2: Contar el numero de peces permanents
        // (es a dir, que no poden ser girades per l'oponent)
        // Heuristica 3: contar el numero de moviments possibles (movilitat)
        // Heuristica 4 contar numero de peces i fer percentatge
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {

                // 3
                if (s.canMove(new Point(i, j), myType)) {
                    blackMoves++;
                }
                if (s.canMove(new Point(i, j), hisType)) {
                    whiteMoves++;
                }
                // 2 TODO MIRAR QUE CASILLA ES LA DEL CENTrO
                if (isEstable(s, i, j, myType)) {
                    stability++;
                }
                if (isEstable(s, i, j, hisType)) {
                    stability--;
                }
                // Miramos si estan en el exterior (priorizamos)
                if (i == 0 || i == 7 || j == 0 || j == 7) {
                    if (s.getPos(i, j) == myType) {
                        puntuacio += 20;
                    }
                    if (s.getPos(i, j) == hisType) {
                        puntuacio -= 20;
                    }
                }

                // 1
                if (s.getPos(i, j) == myType) {
                    puntuacio += V[i][j];
                    // 4
                    pecesnostres++;
                }
                if (s.getPos(i, j) == hisType) {
                    puntuacio -= V[i][j];
                    // 4
                    pecescontrari++;
                }
            }
        }

        // Si es a partir de 32 peces comptem les peces
        if (pecesnostres + pecescontrari >= 50) {
            percentatge = 100 * (pecesnostres - pecescontrari) / (pecesnostres + pecescontrari);
        }

        // Add a bonus to the score for the player with more legal moves
        if (blackMoves > whiteMoves) {
            puntuacio += 2;
        } else if (whiteMoves > blackMoves) {
            puntuacio -= 2;
        }
        return puntuacio + stability + percentatge + permanent;
    }

    // Funcion que comprueba si una casilla es estable
    public boolean isEstable(GameStatus s, int row, int col, CellType player) {
        // check if the piece is surrounded on all sides
        return isEnvoltat(s, row, col, player, -1, 0) &&
                isEnvoltat(s, row, col, player, 1, 0) &&
                isEnvoltat(s, row, col, player, 0, -1) &&
                isEnvoltat(s, row, col, player, 0, 1);
    }

    private boolean isEnvoltat(GameStatus s, int row, int col, CellType player, int rowDelta, int colDelta) {
        // check if the piece is surrounded in the given direction
        int newRow = row + rowDelta;
        int newCol = col + colDelta;
        if (newRow < 0 || newRow >= BOARD_SIZE || newCol < 0 || newCol >= BOARD_SIZE) {
            // fora del tauler, no envoltat
            return false;
        }
        if (s.getPos(newRow, newCol) == player) {
            // mateixa color, no envoltat
            return false;
        }
        if (s.getPos(newRow, newCol) == CellType.EMPTY) {
            // espai buit, no envoltat
            return false;
        }
        // Comprovem el seguent espai en la direccio donada
        return isEnvoltat(s, newRow, newCol, player, rowDelta, colDelta);
    }
}
