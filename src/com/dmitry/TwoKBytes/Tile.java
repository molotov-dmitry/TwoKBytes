package com.dmitry.TwoKBytes;

public class Tile {

    /// Variables ------------------------------------------------------------------------------------------------------

    private int x;
    private int y;

    private int level;

    private int moveTiles;
    private boolean merge;

    private int sizePercent;

    /// Constructors ---------------------------------------------------------------------------------------------------

    Tile(int x, int y) {
//        setX(x);
//        setY(y);
        setLevel(1);
        setMerge(false);
        setMoveTiles(0);
        setSizePercent(0);
    }

    /// Getters/setters ------------------------------------------------------------------------------------------------

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMoveTiles() {
        return moveTiles;
    }

    public void setMoveTiles(int moveTiles) {
        this.moveTiles = moveTiles;
    }

    public boolean isMerge() {
        return merge;
    }

    public void setMerge(boolean merge) {
        this.merge = merge;
    }

    public int getSizePercent() {
        if (sizePercent >= 100)
            return 100;
        else
            return sizePercent;
    }

    public void setSizePercent(int sizePercent) {
        if (sizePercent >= 100)
            this.sizePercent = 100;
        else
            this.sizePercent = sizePercent;
    }

    /// Methods/functions ----------------------------------------------------------------------------------------------

    public void increaseLevel() {
        if (this.level < 11)
            level++;
    }

    public void decreaseMoveTiles() {
        if (moveTiles > 0)
            moveTiles--;
        else if (moveTiles < 0)
            moveTiles = 0;
    }

}
