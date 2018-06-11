package de.hhn.aib3.aufg3.gruppe11.game.elements.game;

import java.io.Serializable;


/**
 * Represents one cell of a game board
 */
public class Cell implements Serializable {

    private int row;
    private int column;

    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
