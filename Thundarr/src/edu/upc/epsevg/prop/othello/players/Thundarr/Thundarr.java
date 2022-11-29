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
    private GameStatus s; // tauler i estat del joc
    public int tablero[][];

    // Variable que almacena la mejor puntuacion posible para el movimiento. (mas
    // infinito)
    private static final int GANADOR = 999999;

    // Variable que almacena la peor puntuacion posible para el movimiento. (menos
    // infinito)
    private static final int PERDEDOR = -999999;

    public Thundarr(String name) {
        this.name = name;
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

        ArrayList<Point> moves = s.getMoves(); // Pillamos todos los movimientos posibles
        if (moves.isEmpty()) {
            // no podem moure, el moviment (de tipus Point) es passa null.
            return new Move(null, 0L, 0, SearchType.RANDOM);
        } else {
            // TODO: implementar movimiento en funcion de heurisitica
            Random rand = new Random();
            int q = rand.nextInt(moves.size());
            return new Move(moves.get(q), 0L, 0, SearchType.RANDOM);
        }
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public String getName() {
        return "Thundarr(" + name + ")";
    }

    // Boolean turno indica si en min o es max
    // 0 casella buida, 1 player 1, -1 player 2
    public int heuristica(Point tirada, int player) {
        int puntuacion_final = 0;
        int V[][] = new int[8][8];

        // Matriz de puntuaciones
        V[0] = new int[] { 20, -3, 11, 8, 8, 11, -3, 20 };
        V[1] = new int[] { -3, -7, -4, 1, 1, -4, -7, -3 };
        V[2] = new int[] { 11, -4, 2, 2, 2, 2, -4, 11 };
        V[3] = new int[] { 8, 1, 2, -3, -3, 2, 1, 8 };
        V[4] = new int[] { 8, 1, 2, -3, -3, 2, 1, 8 };
        V[5] = new int[] { 11, -4, 2, 2, 2, 2, -4, 11 };
        V[6] = new int[] { -3, -7, -4, 1, 1, -4, -7, -3 };
        V[7] = new int[] { 20, -3, 11, 8, 8, 11, -3, 20 };
        // heuristica millor casella on posar fitxa
        Point Max = moves.get(0);
        for (int i = 0; i < moves.size(); i++) {
            int a = moves.get(i).x;
            int b = moves.get(i).y;
            if (V[a][b] > V[Max.x][Max.y]) {
                Max = moves.get(i);
            }
        }
        return V[Max.x][Max.y];

    }

    // True max, false min
    public int minimax(GameStatus s_copia, int color, int profunditat, int alpha, int beta, boolean turno) {
        int color_oponent = color;
        int valor;
        // Si la profundidad ya ha llegado a su limite no habra mas movimientos
        // posibles, devolvemos la heurisica del tablero.
        if (profunditat == 0 || s_copia.checkGameOver() == true) {
            return heuristica(s_copia, color);
        }
        // Inicializamos variables en funcion si es min o max
        if (turno) { // Max
            // Establecemos el valor inicial como al minimo asi cualquier valor sera
            // superior.
            valor = Integer.MIN_VALUE;
        } else { // Min
            // Establecemos el valor inicial como al maximo asi cualquier valor sera
            // inferior.
            valor = Integer.MAX_VALUE;
        }
        // Para cada tirada que se pueda hacer
        ArrayList<Point> moves = s_copia.getMoves(); // Pillamos todos los movimientos posibles

        for (int col = 0; col < s_copia.getMoves().size(); col++) {
            // Copia del tablero
            Tauler tauler_aux = new Tauler(tauler_copia);
            // Si se puede realizar un movimiento en la columna que estamos posicionados
            if (tauler_aux.movpossible(col)) {

                if (turno) {
                    // Realizamos el movimiento seleccionado con el color del jugador.
                    tauler_aux.afegeix(col, color);

                    if (tauler_aux.solucio(col, color)) {
                        return GANADOR;
                    }
                    // Calculamos heuristica. Si esta es supererior al valor actual, substituimos
                    // valor por esta.
                    valor = Math.max(valor, minimax(tauler_aux, color, profunditat - 1, alpha, beta, false));

                    // Poda alfa-beta
                    if (beta <= valor) {
                        return valor;
                    }

                    alpha = Math.max(valor, alpha);
                } else { // min
                    // Realizamos el movimiento con el color del oponente ya que estamos en la capa
                    // Min
                    tauler_aux.afegeix(col, color_oponent);

                    if (tauler_aux.solucio(col, color_oponent)) {
                        return PERDEDOR;
                    }
                    // Calculamos heuristica. Si esta es menor al valor actual substituimos por esta
                    valor = Math.min(valor, minimax(tauler_aux, color, profunditat - 1, alpha, beta, true));

                    // Realitzem la poda alpha-beta
                    if (valor <= alpha) {
                        return valor;
                    }

                    beta = Math.min(valor, beta);
                }
            }
        }
        // Devolvemos heuristica
        return valor;

    }

}
