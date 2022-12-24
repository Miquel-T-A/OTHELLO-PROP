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
 * Jugador Thundarr amb IDS
 * 
 * @author Miquel Torres, Pau Raduà
 */
public class PlayerID implements IPlayer, IAuto {

    private String name;
    private CellType myType;
    private CellType hisType;
    private static long[][][] zobristKeys;
    private static int BOARD_SIZE = 8;
    private static Random RANDOM = new Random();
    private boolean timeout = false;
    private int minEval = Integer.MIN_VALUE;
    private int maxEval = Integer.MAX_VALUE;
    private int V[][];
    private int evalanterior = 0; // Valor de la evaluación de profundidad anterior
    private int nodesexplorats = 0; // Número de nodos explorados
    private InfoNode[] taulaTransposicio; // Taula de transposició
    long N = 119304599;taula[][][][][][InfoNode(millorfill, color)][][][]

    public PlayerID() {
        // 1GB = 59652323
        // 2GB = 119304599
        taulaTransposicio = new InfoNode[(int) N];

        V = new int[8][8];

        // Matriz de puntuaciones

        V[0] = new int[] { 100, -30, 11, 8, 8, 11, -30, 100 };
        V[1] = new int[] { -30, -7, -4, 1, 1, -4, -7, -30 };
        V[2] = new int[] { 11, -4, 2, 2, 2, 2, -4, 11 };
        V[3] = new int[] { 8, 1, 2, -3, -3, 2, 1, 8 };
        V[4] = new int[] { 8, 1, 2, -3, -3, 2, 1, 8 };
        V[5] = new int[] { 11, -4, 2, 2, 2, 2, -4, 11 };
        V[6] = new int[] { -30, -7, -4, 1, 1, -4, -7, -30 };
        V[7] = new int[] { 100, -30, 11, 8, 8, 11, -30, 100 };

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

    /**
     * Ens dona el nom del jugador.
     * 
     * @return Nom del jugador.
     */
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
        nodesexplorats = 0;
        myType = s.getCurrentPlayer();
        hisType = CellType.opposite(myType);

        int maxEval = Integer.MIN_VALUE;

        Point bestMove = new Point();
        ArrayList<Point> moves = s.getMoves();

        // Fem una cerca en profunditat iterativa
        while (!timeout) {
            // Evitem que es quedi bucle infinit si no hi ha moviments
            if (moves.size() == 0) {
                bestMove = null;
                break;
            }

            for (int i = 0; i < moves.size(); i++) {
                GameStatus s_aux = new GameStatus(s);
                s_aux.movePiece(moves.get(i));
                eval = minMinimax(s_aux, depth, Integer.MAX_VALUE, Integer.MIN_VALUE);
                // System.out.println("EVAL: " + eval);
                if (eval > maxEval) {
                    maxEval = eval;
                    bestMove = moves.get(i);
                    indicemov = i;
                }
            }
            evalanterior = maxEval;
            depth++;
        }
        System.out.println("PROFUNDITAT: " + depth + " " + evalanterior);

        System.out.println(
                "MOVIMIENTO: " + indicemov + "de" + (moves.size() - 1) + " " + bestMove + " " + maxEval
                        + " NODESEXPLORATS: " + nodesexplorats);

        return new Move(bestMove, 0L, 0, SearchType.MINIMAX_IDS);
    }

    /**
     * Algoritme min de minimax
     * 
     * @param s     Tauler i estat actual de joc.
     * @param depth Profunditat de la cerca.
     * @param beta  Valor de beta (poda).
     * @param alpha Valor de alpha (poda).
     * @return valor de la heurística.
     */
    int minMinimax(GameStatus s, int depth, int beta, int alpha) {
        if (timeout) {
            return evalanterior;
            // return valor;
        }

        // Millor index sense assignar
        int millorindex = -1;

        if (s.isGameOver()) { // ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 9999999;
            else // Guanya el contrincant
                return -9999999;
        } else if (depth == 0) { // no hi ha moviments possibles o profunditat es 0

            int valor = heuristica(s);
            return valor;
        }

        // Node intermig
        long hash = calculaBoardHash(s);
        int posicio = (int) (hash % N);

        InfoNode info = taulaTransposicio[posicio];
        byte color = (byte) (s.getCurrentPlayer() == myType ? 1 : 0);
        if (info != null && info.color == color)
            millorindex = (int) info.millorfill;
        // Comprovem que es el mateix color

        minEval = Integer.MAX_VALUE;
        ArrayList<Point> moves = s.getMoves();

        GameStatus s_aux = new GameStatus(s);

        // Evitem que retorni un valor que no és correcte quan no tenim moviments i
        // continuem amb el contrari
        if (moves.size() == 0) {
            s_aux.skipTurn();
            minEval = Math.min(minEval, maxMiniMax(s_aux, depth - 1, beta, alpha));
        } else {
            if (millorindex != -1) {
                // Recorrem primer el millor moviment
                s_aux.movePiece(moves.get(millorindex));
                minEval = Math.min(minEval, maxMiniMax(s_aux, depth - 1, beta, alpha));
                beta = Math.min(beta, minEval);
            }
            // Iterem sobre tots el moviments nous posibles
            for (int i = 0; i < moves.size(); i++) {
                // Saltem el millor moviment
                if (millorindex != -1 && i == millorindex)
                    continue;
                s_aux = new GameStatus(s);
                // Movem la peça en el status auxiliar
                s_aux.movePiece(moves.get(i));
                minEval = Math.min(minEval, maxMiniMax(s_aux, depth - 1, beta, alpha));
                beta = Math.min(beta, minEval);
                if (alpha >= beta) {
                    break;
                }
            }
        }
        // Afegim el millor index i el seu color a la taula de transposicio
        taulaTransposicio[posicio] = new InfoNode((byte) millorindex, color);
        return minEval;
    }

    /**
     * Algoritme min de minimax
     * 
     * @param s     Estat del game.
     * @param depth Profunditat de la cerca.
     * @param beta  Valor de beta (poda).
     * @param alpha Valor de alpha (poda).
     * @return valor de la heurística.
     */
    int maxMiniMax(GameStatus s, int depth, int beta, int alpha) {
        if (timeout) {
            return evalanterior;
        }

        // Millor index sense assignar
        int millorindex = -1;

        if (s.isGameOver()) { // Ha guanyat algu
            if (myType == s.GetWinner()) // Guanyem nosaltres
                return 9999999;
            else // Guanya el contrincant
                return -9999999;
        } else if (depth == 0) { // no hi ha moviments possibles o profunditat es 0
            int valor = heuristica(s);
            // Guardem el valor de la heuristica en la taula hash
            return valor;
        }

        // Node intermig
        long hash = calculaBoardHash(s);
        int posicio = (int) (hash % N);

        InfoNode info = taulaTransposicio[posicio];
        byte color = (byte) (s.getCurrentPlayer() == myType ? 1 : 0);
        if (info != null && info.color == color)
            millorindex = (int) info.millorfill;
        // Comprovem que es el mateix color

        maxEval = Integer.MIN_VALUE;
        ArrayList<Point> moves = s.getMoves();

        GameStatus s_aux = new GameStatus(s);
        // Evitem que retorni un valor que no és correcte quan no tenim moviments i
        // continuem amb el contrari
        if (moves.size() == 0) {
            s_aux.skipTurn();
            maxEval = Math.max(maxEval, minMinimax(s_aux, depth - 1, beta, alpha));
        } else {
            if (millorindex != -1) {
                // Recorrem primer el millor moviment
                s_aux.movePiece(moves.get(millorindex));
                maxEval = Math.max(maxEval, minMinimax(s_aux, depth - 1, beta, alpha));
                alpha = Math.max(alpha, maxEval);
            }

            // Iterem sobre tots el moviments nous posibles
            for (int i = 0; i < moves.size(); i++) {
                // Saltem el millor moviment
                if (millorindex != -1 && i == millorindex)
                    continue;
                s_aux = new GameStatus(s);
                s_aux.movePiece(moves.get(i));
                maxEval = Math.max(maxEval, minMinimax(s_aux, depth - 1, beta, alpha));
                alpha = Math.max(alpha, maxEval);
                if (alpha >= beta) {
                    break;
                }
            }
        }
        // Afegim el millor index i el seu color a la taula de transposicio
        taulaTransposicio[posicio] = new InfoNode((byte) millorindex, color);

        return maxEval;
    }

    /**
     * Funcio que calcula la heuristica del tauler
     * Heuristica 1: Contar el numero de peces del tauler en el lategame
     * Heuristica 2: Contar el numero de peces permanents (semipermanents)
     * (es a dir, que no poden ser girades per l'oponent)
     * Heuristica 3: contar el numero de moviments possibles (movilitat)
     * Heuristica 4 actualitzem taula pesos per les cantonades ()
     * Heuristica 5: Afegim bonus quan una peca esta a les cantonades
     * (i == 0 || 7)
     * (j == 0 || 7)
     * 
     * @param s Tauler i estat actual de joc.
     * @return Puntuacio de la heuristica.
     */
    public int heuristica(GameStatus s) {
        int puntuacio = 0;
        int stability = 0;
        int pecesnostres = 0;
        int pecescontrari = 0;
        int blackMoves = 0;
        int whiteMoves = 0;
        int percentatge = 0;
        int permanent = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                CellType pecaactual = s.getPos(i, j);
                // 3
                if (s.canMove(new Point(i, j), myType)) {
                    blackMoves++;
                }
                if (s.canMove(new Point(i, j), hisType)) {
                    whiteMoves++;
                }

                // 2
                if (isEstable(s, i, j, myType)) {
                    stability++;
                }
                if (isEstable(s, i, j, hisType)) {
                    stability--;
                }

                // Miramos si estan en el exterior (priorizamos)
                if (i == 0 || i == 7 || j == 0 || j == 7) {
                    if (pecaactual == myType) {
                        puntuacio += 20;
                    }
                    if (pecaactual == hisType) {
                        puntuacio -= 20;
                    }
                }

                // Actualitzem la taula de pesos
                actualitzaTaulaPesos(i, j, pecaactual);

                // 1
                if (pecaactual == myType) {
                    puntuacio += V[i][j];
                    // 4
                    pecesnostres++;
                }
                if (pecaactual == hisType) {
                    puntuacio -= V[i][j];
                    // 4
                    pecescontrari++;
                }
            }
        }

        // Si es a partir de 48 peces comptem les peces
        if (pecesnostres + pecescontrari >= 48) {
            percentatge = 100 * (pecesnostres - pecescontrari) / (pecesnostres + pecescontrari);
        }

        // Afegim bonus a la puntuacio amb el jugador que te mes moviments
        if (blackMoves > whiteMoves) {
            puntuacio += 2;
        } else if (whiteMoves > blackMoves) {
            puntuacio -= 2;
        }
        nodesexplorats++;
        return puntuacio + stability + percentatge + permanent;
    }

    /**
     * Actualitza la taula de pesos en funcio si tenim una peca nostra o del
     * contrari posada en cantonades.
     *
     * @param i          Coordenada i que comprovem.
     * @param j          Coordenada j que comprovem.
     * @param pecaactual Peca que estem comprovant que sigui nostra o seva.
     * 
     * @return -
     */
    private void actualitzaTaulaPesos(int i, int j, CellType pecaactual) {
        if (i == 0 && j == 0) {
            if (pecaactual == myType) {
                V[1][0] = 50;
                V[0][1] = 50;
            } else if (pecaactual == hisType) {
                V[1][0] = -50;
                V[0][1] = -50;
            }
        }
        if (i == 0 && j == 7) {
            if (pecaactual == myType) {
                V[0][6] = 50;
                V[1][7] = 50;
            } else if (pecaactual == hisType) {
                V[0][6] = -50;
                V[1][7] = -50;
            }
        }
        if (i == 7 && j == 0) {
            if (pecaactual == myType) {
                V[7][1] = 50;
                V[6][0] = 50;
            } else if (pecaactual == hisType) {
                V[7][1] = -50;
                V[6][0] = -50;
            }
        }
        if (i == 7 && j == 7) {
            if (pecaactual == myType) {
                V[7][6] = 50;
                V[6][7] = 50;
            } else if (pecaactual == hisType) {
                V[7][6] = -50;
                V[6][7] = -50;
            }
        }
    }

    /**
     * Comprova si la peca es estable o no. (semiestable)
     *
     * @param s      Tauler i estat actual de joc.
     * @param row    Coordenada de fila que comprovem.
     * @param col    Coordenada de columna que comprovem.
     * @param player Tipus de peça que comprovem.
     * 
     * @return True si la peça es estable, false si no ho es.
     */
    public boolean isEstable(GameStatus s, int row, int col, CellType player) {
        // Comprova si la peça està rodejada per tots els costats
        return isEnvoltat(s, row, col, player, -1, 0) &&
                isEnvoltat(s, row, col, player, 1, 0) &&
                isEnvoltat(s, row, col, player, 0, -1) &&
                isEnvoltat(s, row, col, player, 0, 1);
    }

    /**
     * Comprova si la peca es envoltada en la direccio donada.
     *
     * @param s        Tauler i estat actual de joc.
     * @param row      Coordenada de fila que comprovem.
     * @param col      Coordenada de columna que comprovem.
     * @param player   Tipus de peça que comprovem.
     * @param rowDelta Coordenada de la nova fila que comprovem.
     * @param colDelta Coordenada de la nova columna que comprovem.
     * 
     * @return True si la peça es envoltada, false si no ho es.
     */
    private boolean isEnvoltat(GameStatus s, int row, int col, CellType player, int rowDelta, int colDelta) {
        // Comprovem si la peça esta envoltada en la direccio donada
        int newRow = row + rowDelta;
        int newCol = col + colDelta;
        if (newRow < 0 || newRow >= BOARD_SIZE || newCol < 0 || newCol >= BOARD_SIZE) {
            // fora del tauler, no envoltat
            return false;
        }
        if (s.getPos(newRow, newCol) == player) {
            // mateix color, no envoltat
            return false;
        }
        if (s.getPos(newRow, newCol) == CellType.EMPTY) {
            // espai buit, no envoltat
            return false;
        }
        // Comprovem el seguent espai en la direccio donada
        return isEnvoltat(s, newRow, newCol, player, rowDelta, colDelta);
    }

    /**
     * Calcula el hash del tauler
     * 
     * @param s Tauler i estat actual de joc.
     * @return Hash del tauler.
     */
    public long calculaBoardHash(GameStatus s) {
        ThundarrAux s_aux = new ThundarrAux(s);
        long hash = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // si el cuadrat esta ocupat per una peça, XOR el seu valor hash al hash total
                // hash
                if (s_aux.getOccupation(i, j)) {
                    hash ^= zobristKeys[i][j][s_aux.getColor(i, j)];
                }
            }
        }
        return Math.abs(hash);
    }

}
