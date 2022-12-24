
package edu.upc.epsevg.prop.othello.players.Thundarr;

import edu.upc.epsevg.prop.othello.GameStatus;

/**
 * Nodes auxiliars per a la taula hash
 * 
 * @author Miquel Torres, Pau Raduà
 */
public class InfoNode {
    byte millorfill;
    byte color; // 0 = negre, 1 = blanc

    /**
     * Constructor per a crear un nou node en la taula
     * 
     * @param millorfill El index de la millor tirada
     * @param color      El color de la tirada
     * 
     */
    public InfoNode(Byte millorfill, byte color) {
        this.millorfill = millorfill;
        this.color = color;
    }

}