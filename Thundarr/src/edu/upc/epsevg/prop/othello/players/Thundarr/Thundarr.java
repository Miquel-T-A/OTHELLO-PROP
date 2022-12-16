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

    private int[][] stabilityTable = {
            { 4, -3, 2, 2, 2, 2, -3, 4, },
            { -3, -4, -1, -1, -1, -1, -4, -3, },
            { 2, -1, 1, 0, 0, 1, -1, 2, },
            { 2, -1, 0, 1, 1, 0, -1, 2, },
            { 2, -1, 0, 1, 1, 0, -1, 2, },
            { 2, -1, 1, 0, 0, 1, -1, 2, },
            { -3, -4, -1, -1, -1, -1, -4, -3, },
            { 4, -3, 2, 2, 2, 2, -3, 4 }
    };

    public Thundarr(String name) {
        this.name = name;
        // Initialize the hash values 2D array
        zobristKeys = new long[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // Generate a random long value for each square on the board
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
            int eval = minValor(fill, depth - 1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            if (maxEval < eval) {
                maxEval = eval;
                bestMove = moves.get(i);
            }
        }
        return bestMove;
    }

    int minValor(GameStatus s, int depth, int beta, int alpha) {
        long hash = getBoardHash(s);
        if (zobristTable.containsKey(hash)) {
            // If we have already evaluated this board position, return the stored value
            return zobristTable.get(hash);
        }
        // System.out.println(s.getCurrentPlayer());
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
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            // System.out.println(fill.getCurrentPlayer());
            fill.movePiece(moves.get(i));

            minEval = Math.min(minEval, maxValor(fill, depth - 1, beta, alpha));
            beta = Math.min(beta, minEval);
            if (alpha >= beta) {
                break;
            }

        }
        zobristTable.put(hash, minEval); // Store the value in the Zobrist table

        return minEval;
    }

    int maxValor(GameStatus s, int depth, int beta, int alpha) {
        long hash = getBoardHash(s);
        if (zobristTable.containsKey(hash)) {
            // If we have already evaluated this board position, return the stored value
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

        int maxEval = Integer.MIN_VALUE + 1;
        ArrayList<Point> moves = s.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
            maxEval = Math.max(maxEval, minValor(fill, depth - 1, beta, alpha));
            alpha = Math.max(alpha, maxEval);
            if (alpha >= beta) {
                break;
            }
        }
        zobristTable.put(hash, maxEval); // Store the value in the Zobrist table

        return maxEval;
    }

    public int heuristica(GameStatus s, CellType player) {
        int h = 0;
        CellType contrari = CellType.opposite(player);
        // System.out.println(player +" "+ contrari);
        // aqui calculem, corners moviments i paritat (corners * 100,moviments * 5,
        // paritat * 25, 25 * stabilitat)
        // Res is 100 * Res_corners + 5 * Res_mobility + 25 * Res_coinParity + 25 *
        // Res_stability.
        h += heur(s, player);
        h -= heur(s, contrari);
        int stability = 0;
        for (int i = 0; i < stabilityTable.length; i++) {
            for (int j = 0; j < stabilityTable.length; j++) {
                if (s.getPos(i, j) == player) {
                    stability += stabilityTable[i][j];
                }
                if (s.getPos(i, j) == contrari) {
                    stability -= stabilityTable[i][j];
                }
            }
        }
        h += stability * 5;
        // System.out.println(h+" " +player);
        return h;
    }

    public int heur(GameStatus s, CellType player) {
        // mirar corners
        // mirar quantes fitxer te cadascu
        // mirar posibles moviments
        // Coin Parity Heuristic Value =
        // 100 * (Max Player Coins - Min Player Coins ) / (Max Player Coins + Min Player
        // Coins)
        int heur = 0;

        int paritat = s.getScore(player);
        heur += paritat * 5;

        int moviments = s.getMoves().size();
        heur += moviments;

        int corners = 0;
        if (s.getPos(0, 0) == player) {
            corners += 5;
        }
        if (s.getPos(s.getSize() - 1, s.getSize() - 1) == player) {
            corners += 5;
        }
        if (s.getPos(0, s.getSize() - 1) == player) {
            corners += 5;
        }
        if (s.getPos(s.getSize() - 1, 0) == player) {
            corners += 5;
        }
        heur += corners * 1000;

        return heur;
    }

    public long getBoardHash(GameStatus s) {
        long hash = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // If the square is occupied by a piece, XOR its hash value into the overall
                // hash
                if (s.getPos(i, j) != CellType.EMPTY) {
                    hash ^= zobristKeys[i][j];
                }
            }
        }
        return hash;
    }
}
