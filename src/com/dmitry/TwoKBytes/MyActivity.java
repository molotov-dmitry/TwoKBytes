package com.dmitry.TwoKBytes;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MyActivity extends Activity {

    TwoKGame game;
    TextView gameLog;
    MySurfaceView gameField;
    Button gameRestart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        gameLog = (TextView) findViewById(R.id.gameLog);
        gameField = (MySurfaceView) findViewById(R.id.gameField);
        gameRestart = (Button) findViewById(R.id.gameRestart);

        game = new TwoKGame();

        gameField.setGame(game);
        gameLog.setText("NewGame");
        gameField.gameAction(TwoKGame.MoveAction.NEW_GAME);

        gameField.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeTop() {
                gameLog.setText("WIP");
                gameField.gameAction(TwoKGame.MoveAction.MOVE_UP);
            }

            public void onSwipeRight() {
                gameLog.setText("R");
                gameField.gameAction(TwoKGame.MoveAction.MOVE_RIGHT);
            }

            public void onSwipeLeft() {
                gameLog.setText("L");
                gameField.gameAction(TwoKGame.MoveAction.MOVE_LEFT);
            }

            public void onSwipeBottom() {
                gameLog.setText("WIP");
                gameField.gameAction(TwoKGame.MoveAction.MOVE_DOWN);
            }
        });
    }

    public void RestartClick(View v) {
        gameLog.setText("NewGame");
        gameField.gameAction(TwoKGame.MoveAction.NEW_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        gameField.onPauseMySurfaceView();
    }

    @Override
    public void onResume() {
        super.onResume();
        gameField.onResumeMySurfaceView();
    }
}
