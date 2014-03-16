package com.dmitry.TwoKBytes;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class MySurfaceView extends SurfaceView implements Runnable {
    /// Constants and variables ========================================================================================

    /// Constants ------------------------------------------------------------------------------------------------------

    final int BORDER_DOWNSCALE = 7;

    final int[] fillColors = {
            getResources().getColor(R.color.tile_1),
            getResources().getColor(R.color.tile_2),
            getResources().getColor(R.color.tile_3),
            getResources().getColor(R.color.tile_4),
            getResources().getColor(R.color.tile_5),
            getResources().getColor(R.color.tile_6),
            getResources().getColor(R.color.tile_7),
            getResources().getColor(R.color.tile_8),
            getResources().getColor(R.color.tile_9),
            getResources().getColor(R.color.tile_10),
            getResources().getColor(R.color.tile_11),
    };

    /// Thread variables -----------------------------------------------------------------------------------------------

    Thread thread = null;

    volatile boolean threadRunning = false;
    volatile boolean animationStop = false;

    /// Drawing vatiables ----------------------------------------------------------------------------------------------

    private SurfaceHolder surfaceHolder;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /// Game variables -------------------------------------------------------------------------------------------------

    private TwoKGame game = null;

    volatile TwoKGame.MoveAction moveAction = TwoKGame.MoveAction.MOVE_LEFT;

    /// Field canvas variables -----------------------------------------------------------------------------------------

    private int cvCount;

    private int cvWidth;        // Canvas width
    private int cvHeight;       // Canvas height

    private int cvCX, cvCY;     // Canvas center

    private int cvSize;         // Canvas size (min of height and width)

    private int cvTileSize;     // Size of tile
    private int cvBorderSize;   // Width of border

    private int cvFieldR;       // Half of field size

    private int cvStep;


    /// Implementation =================================================================================================

    /// Init function --------------------------------------------------------------------------------------------------


    private void init(Context context) {
        surfaceHolder = getHolder();
//        tile = new Tile(0, 0);
    }

    /// Constructors ---------------------------------------------------------------------------------------------------

    public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MySurfaceView(Context context) {
        super(context);
        init(context);
    }

    /// Setters --------------------------------------------------------------------------------------------------------

    public void setGame(TwoKGame game) {
        this.game = game;
    }

    /// Resume/Pause functions -----------------------------------------------------------------------------------------

    public void onResumeMySurfaceView() {
        redrawField(true);
    }

    public void onPauseMySurfaceView() {
        boolean retry = true;

        animationStop = true;

        while (retry) {
            try {
                if (thread != null)
                    thread.join();
                threadRunning = false;
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /// Start functions ------------------------------------------------------------------------------------------------

    public void gameAction(TwoKGame.MoveAction action) {

        Object sync = new Object();

        synchronized (sync) {

            if (game == null)
                return;

            boolean retry = true;

            while (retry) {
                try {
                    if (!animationStop)
                        animationStop = true;

                    if (thread != null)
                        thread.join();

                    retry = false;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (action == TwoKGame.MoveAction.NEW_GAME) {
                game.startGame();
            }

            moveAction = action;

            animationStop = false;
            threadRunning = true;

            thread = new Thread(this);
            thread.start();
        }
    }

    /// Drawing functions ----------------------------------------------------------------------------------------------

    private void getDimensions(Canvas canvas) {

        if (game == null)
            return;

        cvWidth = canvas.getWidth();
        cvHeight = canvas.getHeight();

        cvCX = cvWidth / 2;
        cvCY = cvHeight / 2;

        cvSize = Math.min(cvWidth, cvHeight);

        cvTileSize = (cvSize * BORDER_DOWNSCALE) / ((BORDER_DOWNSCALE + 1) * game.COUNT + 1);
        cvTileSize -= cvTileSize % 2;

        cvBorderSize = cvTileSize / BORDER_DOWNSCALE;
        cvBorderSize -= cvBorderSize % 2;

        cvFieldR = (cvTileSize / 2) * game.COUNT + (cvBorderSize / 2) * (game.COUNT + 1);

        cvStep = cvTileSize + cvBorderSize;
    }

    private void drawField(Canvas canvas) {

        if (game == null)
            return;

        /// Fill background with bg_gray

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.bg_gray));
        canvas.drawRect(0, 0, cvWidth, cvHeight, paint);

        /// Draw rounded rectangle field

        paint.setColor(getResources().getColor(R.color.field_bg));

        RectF rectF = new RectF(cvCX - cvFieldR, cvCY - cvFieldR, cvCX + cvFieldR, cvCY + cvFieldR);

        canvas.drawRoundRect(rectF, cvFieldR / (BORDER_DOWNSCALE * 2), cvFieldR / (BORDER_DOWNSCALE * 2), paint);

        paint.setColor(getResources().getColor(R.color.field_tile_bg));

        for (int h = 0; h < game.COUNT; h++)
            for (int w = 0; w < game.COUNT; w++) {
                int xa = cvCX - cvFieldR + cvBorderSize * (w + 1) + cvTileSize * w;
                int ya = cvCY - cvFieldR + cvBorderSize * (h + 1) + cvTileSize * h;
                int xb = xa + cvTileSize;
                int yb = ya + cvTileSize;
                rectF = new RectF(xa, ya, xb, yb);
                canvas.drawRoundRect(rectF, cvFieldR / (BORDER_DOWNSCALE * 4), cvFieldR / (BORDER_DOWNSCALE * 4), paint);
            }

    }

    int getX(int xPos) {
        if (game == null)
            return 0;

        return cvCX - cvFieldR + cvBorderSize * (xPos + 1) + cvTileSize * xPos;
    }

    int getY(int yPos) {
        if (game == null)
            return 0;

        return cvCY - cvFieldR + cvBorderSize * (yPos + 1) + cvTileSize * yPos;
    }

    void drawTile(Tile tile, Canvas canvas, boolean isNew) {

        if (game == null)
            return;

        int xa, xb, ya, yb, r;
        float textSize, strokeWidth;

        int digitCount = (int)Math.log10(1 << tile.getLevel()) + 1;
        float digitCountDiv;

        if (digitCount < 3) {
            digitCountDiv = 1;
        }
        else {
            digitCountDiv = (float) digitCount / 2.0f;
        }



        if (tile.getSizePercent() == 100) {
            xa          = tile.getX();
            xb          = xa + cvTileSize;
            ya          = tile.getY();
            yb          = ya + cvTileSize;
            r           = cvFieldR / (BORDER_DOWNSCALE * 4);

            textSize    = ((float) (cvTileSize) / 2) / digitCountDiv;
            strokeWidth = (float)cvTileSize / 50;
        }
        else {
            int offset  = (100 - tile.getSizePercent()) / 2;

            xa          = tile.getX() + offset;
            xb          = tile.getX() + cvTileSize - offset;
            ya          = tile.getY() + offset;
            yb          = tile.getY() + cvTileSize - offset;
            r           = (cvFieldR *  tile.getSizePercent()) / (BORDER_DOWNSCALE * 4 * 100);

            textSize    = ((float) (cvTileSize) / 2) * ( (float) (tile.getSizePercent()) / 100) / digitCountDiv;
            strokeWidth = ((float) cvTileSize * ( (float) (tile.getSizePercent()) / 100)) / 50;
        }

        RectF rectF = new RectF(xa, ya, xb, yb);

        /// Fill tile

        int cl = fillColors[(tile.getLevel() - 1) % 11];
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(cl);
        canvas.drawRoundRect(rectF, r, r, paint);

        // Draw border, if tile is new

        if (isNew) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(rectF, r, r, paint);
        }

        /// Draw tile level

        Paint textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.tile_font_color));
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.setTextSize(textSize);

        int xPos = (xa + ((xb - xa) / 2));
        int yPos = (int) ((ya + ((yb - ya) / 2)) - ((textPaint.descent() + textPaint.ascent()) / 2));

        canvas.drawText(String.valueOf(1 << tile.getLevel()), xPos, yPos, textPaint);

    }

    void drawTiles(Canvas canvas, boolean showNew) {
        if (game == null)
            return ;

        for (int w = 0; w < game.COUNT; w++)
            for (int h = 0; h < game.COUNT; h++)
                if (game.getField()[w][h] != null) {
                    drawTile(game.getField()[w][h], canvas,
                            (( showNew && game.getNewH() == h && game.getNewW() == w) ?  true : false));
                }
    }

    void redrawField(boolean showNew) {
        if (!surfaceHolder.getSurface().isValid())
            return;

        Canvas canvas = surfaceHolder.lockCanvas();

        getDimensions(canvas);
        drawField(canvas);
        drawTiles(canvas, showNew);

        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void run() {

        if (game == null) {
            threadRunning = false;
            return;
        }

        int tileStart, tileStop, tileAdd, tileWatch;
        int freeSpace, neighboorLevel, maxActions = 0, curActions = 0;
        boolean canMerge;

        if (moveAction == TwoKGame.MoveAction.MOVE_LEFT || moveAction == TwoKGame.MoveAction.MOVE_RIGHT) {
            if (moveAction == TwoKGame.MoveAction.MOVE_RIGHT) {
                tileStart = game.COUNT - 1;
                tileStop  = 0;
                tileAdd   = -1;
                tileWatch = 1;
            }
            else {
                tileStart = 0;
                tileStop  = game.COUNT - 1;
                tileAdd   = 1;
                tileWatch = -1;
            }

            for (int h = 0; h < game.COUNT; h++) {

                if (game.getField()[tileStart][h] == null) {
                    freeSpace = 1;
                    canMerge = false;
                    neighboorLevel = 0;
                }
                else {
                    freeSpace = 0;
                    canMerge = true;
                    neighboorLevel = game.getField()[tileStart][h].getLevel();
                }


                for (int w = tileStart + tileAdd; (w * tileAdd) <= (tileStop * tileAdd); w += tileAdd) {
                    if (game.getField()[w][h] == null) {
                        freeSpace ++;
                        continue;
                    }

                    Tile curTile = game.getField()[w][h];

                    curTile.setMoveTiles(freeSpace);

                    if (canMerge && neighboorLevel == curTile.getLevel()) {
                        freeSpace++;
                        curTile.setMerge(true);
                        canMerge = false;
                    }
                    else {
                        canMerge = true;
                        curTile.setMerge(false);
                        neighboorLevel = curTile.getLevel();
                    }

                    curActions = freeSpace;

                    if (curActions > maxActions)
                        maxActions = curActions;

                }
            }

            for (int act = 0; act  < maxActions; act++) {
                for (int  step = 1; step < game.SMOOTH && !animationStop; step++) {
                    int xOffset = ((getX(1) - getX(1 + tileAdd)) * step) / game.SMOOTH;

                    for (int h = 0; h < game.COUNT; h++) {
                        for (int w = tileStart + tileAdd; (w * tileAdd) <= (tileStop * tileAdd); w += tileAdd) {
                            Tile curTile = game.getField()[w][h];

                            if (curTile == null)
                                continue;

//                            if (curTile.getMoveTiles() == 0 && curTile.isMerge()) {
//                                curTile.increaseLevel();
//                                curTile.setMerge(false);
//                            }

                            if (curTile.getMoveTiles() > 0) {
                                curTile.setX(getX(w) + xOffset);
                            }
                        }
                    }

                    if (!animationStop || act == maxActions - 1) {
                        redrawField(false);
                    }

                }

                for (int h = 0; h < game.COUNT; h++) {
                    for (int w = tileStart + tileAdd; (w * tileAdd) <= (tileStop * tileAdd); w += tileAdd) {
                        Tile curTile = game.getField()[w][h];

                        if (curTile == null)
                            continue;

                        if (curTile.getMoveTiles() > 0 || curTile.isMerge()) {
                            game.getField()[w - tileAdd][h] = curTile;
                            game.getField()[w][h] = null;
                            curTile.setX(getX(w - tileAdd));
                        }
                        else {
                            curTile.setX(getX(w));
                        }

                        if (curTile.getMoveTiles() == 0 && curTile.isMerge()) {
                            curTile.increaseLevel();
                            curTile.setMerge(false);
                        }

                        if (curTile.getMoveTiles() > 0) {
                            curTile.decreaseMoveTiles();
                        }
                    }
                }

                if (!animationStop || act == maxActions - 1) {
                    redrawField(false);
                }
            }
        }
        else if (moveAction == TwoKGame.MoveAction.MOVE_UP || moveAction == TwoKGame.MoveAction.MOVE_DOWN) {
            if (moveAction == TwoKGame.MoveAction.MOVE_DOWN) {
                tileStart = game.COUNT - 1;
                tileStop  = 0;
                tileAdd   = -1;
                tileWatch = 1;
            }
            else {
                tileStart = 0;
                tileStop  = game.COUNT - 1;
                tileAdd   = 1;
                tileWatch = -1;
            }

            for (int w = 0; w < game.COUNT; w++) {

                if (game.getField()[w][tileStart] == null) {
                    freeSpace = 1;
                    canMerge = false;
                    neighboorLevel = 0;
                }
                else {
                    freeSpace = 0;
                    canMerge = true;
                    neighboorLevel = game.getField()[w][tileStart].getLevel();
                }


                for (int h = tileStart + tileAdd; (h * tileAdd) <= (tileStop * tileAdd); h += tileAdd) {
                    if (game.getField()[w][h] == null) {
                        freeSpace ++;
                        continue;
                    }

                    Tile curTile = game.getField()[w][h];

                    curTile.setMoveTiles(freeSpace);

                    if (canMerge && neighboorLevel == curTile.getLevel()) {
                        freeSpace++;
                        curTile.setMerge(true);
                        canMerge = false;
                    }
                    else {
                        canMerge = true;
                        curTile.setMerge(false);
                        neighboorLevel = curTile.getLevel();
                    }

                    curActions = freeSpace;

                    if (curActions > maxActions)
                        maxActions = curActions;

                }
            }

            for (int act = 0; act  < maxActions; act++) {
                for (int  step = 1; step < game.SMOOTH && !animationStop; step++) {
                    int yOffset = ((getY(1) - getY(1 + tileAdd)) * step) / game.SMOOTH;

                    for (int w = 0; w < game.COUNT; w++) {
                        for (int h = tileStart + tileAdd; (h * tileAdd) <= (tileStop * tileAdd); h += tileAdd) {
                            Tile curTile = game.getField()[w][h];

                            if (curTile == null)
                                continue;

//                            if (curTile.getMoveTiles() == 0 && curTile.isMerge()) {
//                                curTile.increaseLevel();
//                                curTile.setMerge(false);
//                            }

                            if (curTile.getMoveTiles() > 0) {
                                curTile.setY(getY(h) + yOffset);
                            }
                        }
                    }

                    if (!animationStop || act == maxActions - 1) {
                        redrawField(false);
                    }

                }

                for (int w = 0; w < game.COUNT; w++) {
                    for (int h = tileStart + tileAdd; (h * tileAdd) <= (tileStop * tileAdd); h += tileAdd) {
                        Tile curTile = game.getField()[w][h];

                        if (curTile == null)
                            continue;

                        if (curTile.getMoveTiles() > 0 || curTile.isMerge()) {
                            game.getField()[w][h - tileAdd] = curTile;
                            game.getField()[w][h] = null;
                            curTile.setY(getY(h - tileAdd));
                        }
                        else {
                            curTile.setY(getY(h));
                        }

                        if (curTile.getMoveTiles() == 0 && curTile.isMerge()) {
                            curTile.increaseLevel();
                            curTile.setMerge(false);
                        }

                        if (curTile.getMoveTiles() > 0) {
                            curTile.decreaseMoveTiles();
                        }
                    }
                }

                if (!animationStop || act == maxActions - 1) {
                    redrawField(false);
                }
            }
        }

        if (maxActions > 0 || moveAction == TwoKGame.MoveAction.NEW_GAME) {
            game.placeNewTile();

            int w = game.getNewW();
            int h = game.getNewH();

            if (!surfaceHolder.getSurface().isValid()) {
                threadRunning = false;
                return;
            }

            Canvas canvas = surfaceHolder.lockCanvas();
            getDimensions(canvas);
            surfaceHolder.unlockCanvasAndPost(canvas);

            game.getField()[w][h].setX(getX(w));
            game.getField()[w][h].setY(getY(h));

            for (int step = 1; step < game.SMOOTH && !animationStop; step++) {
                game.getField()[w][h].setSizePercent((100 * step) / game.SMOOTH);
                redrawField(true);
            }

            game.getField()[w][h].setSizePercent(100);
            redrawField(true);
        }

        threadRunning = false;
    }
}
