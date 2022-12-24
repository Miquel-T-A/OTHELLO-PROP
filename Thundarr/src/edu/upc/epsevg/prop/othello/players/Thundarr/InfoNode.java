
package edu.upc.epsevg.prop.othello.players.Thundarr;

import edu.upc.epsevg.prop.othello.GameStatus;

public class InfoNode {
    byte millorfill;
    byte color; // 0 = negre, 1 = blanc

    public InfoNode(Byte millorfill, byte color) {
        this.millorfill = millorfill;
        this.color = color;
    }

}