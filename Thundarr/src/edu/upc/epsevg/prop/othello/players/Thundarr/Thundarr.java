package edu.upc.epsevg.prop.othello.players.Thundarr;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;
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
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public String getName() {
        return "Hellowda(" + name + ")";
    }

    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
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
        Point mov = triaPosició(s, 4);
        return new Move(mov, 0L, 0, SearchType.RANDOM);
        // return move (posicio, 0, 0, MINIMAX)
    }

    Point triaPosició(GameStatus s, int depth) {

        int maxEval = Integer.MIN_VALUE;
        Point bestMove = new Point();
        ArrayList<Point> moves = s.getMoves();
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
        // System.out.println(s.getCurrentPlayer());
        if (s.checkGameOver()) { // ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 1000000;
            else // Guanya el contrincant
                return -1000000;
        } else if (s.isGameOver() || depth == 0) { // no hi ha moviments possibles o profunditat es 0
            return heuristica(s, s.getCurrentPlayer());
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
        return minEval;
    }

    int maxValor(GameStatus s, int depth, int beta, int alpha) {
        // System.out.println(s.getCurrentPlayer());
        if (s.checkGameOver()) { // ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 1000000;
            else // Guanya el contrincant
                return -1000000;
        } else if (s.isGameOver() || depth == 0) { // no hi ha moviments possibles o profunditat es 0
            return heuristica(s, s.getCurrentPlayer());
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

    /*
     * public int minimax(int depth, int nodeIndex, boolean maximizingPlayer,
     * GameStatus s, int alpha, int beta)
     * {
     * // Si se llega al fondo del árbol o si ya no hay movimientos válidos, se
     * devuelve el valor de la posición actual
     * if (depth == 0 || s.checkGameOver())
     * return values[nodeIndex];
     * 
     * if (maximizingPlayer)
     * {
     * int best = -INFINITY;
     * 
     * // Recorremos todas las posibles jugadas válidas
     * for (int i = 0; i < possibleMoves(); i++)
     * {
     * int value = minimax(depth - 1, newNodeIndex, false, values, alpha, beta);
     * best = max(best, value);
     * alpha = max(alpha, best);
     * 
     * // Corte alpha-beta
     * if (beta <= alpha)
     * break;
     * }
     * 
     * return best;
     * }
     * else
     * {
     * int best = INFINITY;
     * 
     * // Recorremos todas las posibles jugadas válidas
     * for (int i = 0; i < possibleMoves(); i++)
     * {
     * int value = minimax(depth - 1, newNodeIndex, true, values, alpha, beta);
     * best = min(best, value);
     * beta = min(beta, best);
     * 
     * // Corte alpha-beta
     * if (beta <= alpha)
     * break;
     * }
     * 
     * return best;
     * }
     * }
     */

}
