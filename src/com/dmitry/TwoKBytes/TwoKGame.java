package com.dmitry.TwoKBytes;

import java.util.Random;

/**
 * Created by dmitry on 12.03.14.
 */
public class TwoKGame {

    Tile[][] field;

    public final int COUNT = 4;   // Y
    public final int SMOOTH = 5;

    private int newW;
    private int newH;

    public enum MoveAction {NEW_GAME, MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT}

    /// Constructors ---------------------------------------------------------------------------------------------------

    public TwoKGame() {
        startGame();
    }

    /// Getters/setters ------------------------------------------------------------------------------------------------

    public Tile[][] getField() {
        return field;
    }

    public int getNewW() {
        return newW;
    }

    public int getNewH() {
        return newH;
    }

    /// Methods/functions ----------------------------------------------------------------------------------------------

    public void placeNewTile() {
        Random rand = new Random();

        newW = rand.nextInt(COUNT);
        newH = rand.nextInt(COUNT);

        while (field[newW][newH] != null) {
            newW = rand.nextInt(COUNT);
            newH = rand.nextInt(COUNT);
        }

        field[newW][newH] = new Tile(0, 0);
    }

    public void startGame() {
        field = new Tile[COUNT][COUNT];

        for (int w = 0; w < COUNT; w++)
            for (int h = 0; h < COUNT; h++)
                field[w][h] = null;
    }



}
