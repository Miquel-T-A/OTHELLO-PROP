
package edu.upc.epsevg.prop.othello.players.Thundarr;

import edu.upc.epsevg.prop.othello.GameStatus;

public class ThundarrAux extends GameStatus {
    public ThundarrAux(GameStatus s) {
        super(s);
    }

    public int getColor(int fila, int columna) {
        int valor = fila + columna * 8;
        return (board_color.get(valor) ? 1 : 0);
    }

    public boolean getOccupation(int fila, int columna) {
        return (board_occupied.get(fila + columna * 8));
    }
}