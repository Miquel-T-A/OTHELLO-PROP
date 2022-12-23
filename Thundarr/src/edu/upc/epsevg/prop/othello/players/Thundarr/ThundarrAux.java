
package edu.upc.epsevg.prop.othello.players.Thundarr;

import edu.upc.epsevg.prop.othello.GameStatus;

public class ThundarrAux extends GameStatus {
    public ThundarrAux(GameStatus s) {
        super(s);
    }

    /**
     * Funcio per obtenir el color de la casella
     * 
     * @param fila    Posicio de la fila
     * @param columna Posicio de la columna
     * @return 0 si es negra, 1 si es blanca
     */
    public int getColor(int fila, int columna) {
        int valor = fila + columna * 8;
        return (board_color.get(valor) ? 1 : 0);
    }

    /**
     * Devuelve el valor del bit en un index en especific
     * 
     * @param fila    Posicio de la fila
     * @param columna Posicio de la columna
     * @return True si esta, false si no
     */
    public boolean getOccupation(int fila, int columna) {
        return (board_occupied.get(fila + columna * 8));
    }
}