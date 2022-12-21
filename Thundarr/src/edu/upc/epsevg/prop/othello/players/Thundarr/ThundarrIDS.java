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
public class ThundarrIDS implements IPlayer, IAuto {

    private String name;
    private GameStatus s;
    private CellType myType;
    private CellType hisType;
    private static HashMap<Long, Integer> zobristTable;
    private static long[][][] zobristKeys;
    private static int BOARD_SIZE = 8;
    private static Random RANDOM = new Random();
    private boolean timeout = false;
    private int minEval = Integer.MIN_VALUE;
    private int maxEval = Integer.MAX_VALUE;
    private int V[][];
    private HashMap<Integer, Integer> searchScores; // Array de puntuacions de cada profunditat
    private int evalanterior = 0;

    public ThundarrIDS() {
        V = new int[8][8];

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
        zobristTable = new HashMap<>();

        // Inicialitzem la taula de hash
        zobristKeys = new long[BOARD_SIZE][BOARD_SIZE][2];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // Generem un valor aleatori per cada casella del tauler
                zobristKeys[i][j][0] = RANDOM.nextLong();
                zobristKeys[i][j][1] = RANDOM.nextLong();
            }
        }
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
        timeout = true;
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
        timeout = false;
        int indicemov = 0;
        int eval = 0;
        int depth = 1;

        myType = s.getCurrentPlayer();
        hisType = CellType.opposite(myType);

        int maxEval = Integer.MIN_VALUE;

        Point bestMove = new Point();
        searchScores = new HashMap<Integer, Integer>();
        ArrayList<Point> moves = s.getMoves();

        // Fem una cerca en profunditat iterativa
        while (!timeout) {
            for (int i = 0; i < moves.size(); i++) {
                GameStatus fill = new GameStatus(s);
                fill.movePiece(moves.get(i));
                eval = minMinimax(fill, depth, Integer.MAX_VALUE, Integer.MIN_VALUE);
                // System.out.println("EVAL: " + eval);
                if (eval > maxEval) {
                    maxEval = eval;
                    bestMove = moves.get(i);
                    indicemov = i;
                }
            }
            depth++;
            System.out.println("PROFUNDITAT: " + depth);
        }

        System.out.println("MOVIMIENTO: " + indicemov + "de" + (moves.size() - 1) + " " + bestMove + " " + eval);

        return new Move(bestMove, 0L, 0, SearchType.MINIMAX_IDS);
    }

    int minMinimax(GameStatus s, int depth, int beta, int alpha) {
        if (timeout)
            return searchScores.getOrDefault(depth + 1, Integer.MIN_VALUE);
        /*
         * long hash = calculaBoardHash(s);
         * int value = treuZobrist(hash);
         * if (value != Integer.MIN_VALUE) {
         * return value;
         * }
         */

        if (s.isGameOver()) { // ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 9999999;
            else // Guanya el contrincant
                return -9999999;
        } else if (depth == 0 || timeout) { // no hi ha moviments possibles o profunditat es 0
            int valor = heuristica(s);
            // zobristTable.put(hash, valor);
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
        searchScores.put(depth, minEval);

        // zobristTable.put(hash, minEval); // Guardem el valor en la zobrist table
        return minEval;
    }

    int maxMiniMax(GameStatus s, int depth, int beta, int alpha) {
        if (timeout)
            return searchScores.getOrDefault(depth + 1, Integer.MIN_VALUE);
        /*
         * long hash = calculaBoardHash(s);
         * int value = treuZobrist(hash);
         * if (value != Integer.MIN_VALUE) {
         * return value;
         * }
         */
        if (s.isGameOver()) { // Ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 9999999;
            else // Guanya el contrincant
                return -9999999;
        } else if (depth == 0 || timeout) { // no hi ha moviments possibles o profunditat es 0
            int valor = heuristica(s);
            // zobristTable.put(hash, valor);
            return valor;
        }

        maxEval = Integer.MIN_VALUE;
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
        searchScores.put(depth, maxEval);
        // zobristTable.put(hash, maxEval); // Guardem el valor en la zobrist table

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
        if (pecesnostres + pecescontrari >= 46) {
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

    public int treuZobrist(long hash) {
        return zobristTable.getOrDefault(hash, Integer.MIN_VALUE);
    }

    public long calculaBoardHash(GameStatus s) {
        long hash = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // si el cuadrat esta ocupat per una peça, XOR el seu valor hash al hash total
                // hash
                CellType peca = s.getPos(i, j);
                if (peca == CellType.PLAYER1) {
                    hash ^= zobristKeys[i][j][0];
                } else if (peca == CellType.PLAYER2) {
                    hash ^= zobristKeys[i][j][1];
                }
            }
        }
        return hash;
    }
}