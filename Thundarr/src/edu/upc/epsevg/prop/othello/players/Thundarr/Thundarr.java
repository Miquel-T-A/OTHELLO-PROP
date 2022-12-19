package edu.upc.epsevg.prop.othello.players.Thundarr;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Jugador aleatori
 * 
 * @author bernat
 */
public class Thundarr implements IPlayer, IAuto {

    private String name;
    private GameStatus s;
    private CellType myType;
    private CellType hisType;
    private static HashMap<Long, Integer> zobristTable;
    private static long[][] zobristKeys;
    private static int BOARD_SIZE = 8;
    private static Random RANDOM = new Random();
    private int V[][];

    public Thundarr(String name) {

        V = new int[8][8];

        // Matriz de puntuaciones
        V[0] = new int[] { 20, -3, 11, 8, 8, 11, -3, 20 };
        V[1] = new int[] { -3, -7, -4, 1, 1, -4, -7, -3 };
        V[2] = new int[] { 11, -4, 2, 2, 2, 2, -4, 11 };
        V[3] = new int[] { 8, 1, 2, -3, -3, 2, 1, 8 };
        V[4] = new int[] { 8, 1, 2, -3, -3, 2, 1, 8 };
        V[5] = new int[] { 11, -4, 2, 2, 2, 2, -4, 11 };
        V[6] = new int[] { -3, -7, -4, 1, 1, -4, -7, -3 };
        V[7] = new int[] { 20, -3, 11, 8, 8, 11, -3, 20 };

        this.name = name;
        zobristTable = new HashMap<>();

        // Inicialitzem la taula de hash
        zobristKeys = new long[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // Generem un valor aleatori per cada casella del tauler
                zobristKeys[i][j] = RANDOM.nextLong();
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
        // No fem res
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
        Point mov = triaPosicio(s, 4);
        return new Move(mov, 0L, 0, SearchType.RANDOM);
        // return move (posicio, 0, 0, MINIMAX)
    }

    Point triaPosicio(GameStatus s, int depth) {

        int maxEval = Integer.MIN_VALUE;
        Point bestMove = new Point();
        ArrayList<Point> moves = s.getMoves();

        // Perform the iterative deepening search
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
            int eval = minMinimax(fill, depth - 1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            if (maxEval < eval) {
                maxEval = eval;
                bestMove = moves.get(i);
                System.out.println("MOVIMIENTO: " + i);
            }
        }
        return bestMove;
    }

    int minMinimax(GameStatus s, int depth, int beta, int alpha) {
        long hash = getBoardHash(s);
        if (zobristTable.containsKey(hash)) {
            // Si ja hem evaluat aquesta posició del tauler, retornem el valor
            System.out.println("ENTRAMOS EN ZOBRIST");
            return zobristTable.get(hash);
        }
        if (s.checkGameOver()) { // ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 9999999;
            else // Guanya el contrincant
                return -9999999;
        } else if (s.isGameOver() || depth == 0) { // no hi ha moviments possibles o profunditat es 0
            int valor = heuristica(s, s.getCurrentPlayer());
            zobristTable.put(hash, valor);
            return valor;
        }
        int minEval = Integer.MAX_VALUE;
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
        zobristTable.put(hash, minEval); // Guardem el valor en la zobrist table
        return minEval;
    }

    int maxMiniMax(GameStatus s, int depth, int beta, int alpha) {
        long hash = getBoardHash(s);
        if (zobristTable.containsKey(hash)) {
            // Si ja hem evaluat aquesta posició del tauler, retornem el valor
            System.out.println("ENTRAMOS EN ZOBRIST");
            return zobristTable.get(hash);
        }
        if (s.checkGameOver()) { // Ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 9999999;
            else // Guanya el contrincant
                return -9999999;
        } else if (s.isGameOver() || depth == 0) { // no hi ha moviments possibles o profunditat es 0
            int valor = heuristica(s, s.getCurrentPlayer());
            zobristTable.put(hash, valor);
            return valor;
        }

        int maxEval = Integer.MIN_VALUE + 1;
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
        zobristTable.put(hash, maxEval); // Guardem el valor en la zobrist table

        return maxEval;
    }

    public int heuristica(GameStatus s, CellType player) {
        int puntuacio = 0;
        CellType contrari = CellType.opposite(player);

        int stability = 0;
        // Heuristica 1: Contar el numero de peces del tauler
        // Heuristica 2: Contar el numero de peces estables
        // (es a dir, que no poden ser girades per l'oponent)
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (s.getPos(i, j) == player) {
                    puntuacio += V[i][j];
                    if (isEstable(s, i, j, player)) {
                        stability++;
                    }
                }
                if (s.getPos(i, j) == contrari) {
                    puntuacio -= V[i][j];
                    if (isEstable(s, i, j, player)) {
                        stability--;
                    }
                }
            }
        }

        return puntuacio + stability;
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
            // out of bounds, not surrounded
            return false;
        }
        if (s.getPos(newRow, newCol) == player) {
            // same color, not surrounded
            return false;
        }
        if (s.getPos(newRow, newCol) == CellType.EMPTY) {
            // empty space, not surrounded
            return false;
        }
        // Comprovem el seguent espai en la direccio donada
        return isEnvoltat(s, newRow, newCol, player, rowDelta, colDelta);
    }

    public long getBoardHash(GameStatus s) {
        long hash = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // si el cuadrat esta ocupat per una peça, XOR el seu valor hash al hash total
                // hash
                if (s.getPos(i, j) != CellType.EMPTY) {
                    hash ^= zobristKeys[i][j];
                }
            }
        }
        return hash;
    }
}
