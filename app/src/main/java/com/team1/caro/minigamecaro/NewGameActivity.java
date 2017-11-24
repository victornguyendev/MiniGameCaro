package com.team1.caro.minigamecaro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.team1.caro.minigamecaro.data.Data;
import com.team1.caro.minigamecaro.widget.MessageDialog;

import java.util.List;

public class NewGameActivity extends Activity {
    public int COUNT_CLICK = 0;
    private static final byte NULL = 0;
    private static final byte BLACK = -1;
    private static final byte WHITE = 1;

    private static final int STATE_PLAYER_MOVE = 0;
    private static final int STATE_AI_MOVE = 1;
    private static final int STATE_GAME_OVER = 2;

    private byte playerColor = BLACK;
    private byte aiColor = WHITE;

    private static final int M = 10;
    private byte[][] chessBoard = new byte[M][M];
    //private List<byte[][]> chessBoards = new ArrayList<byte[][]>();
    private int gameState;

    private DrawView drawView = null;
    //private Button undoButton;
    private Button newGameButton;
    private Button homeButton;

    private MessageDialog msgDialog;

    private int newestPlayerX = -1;
    private int newestPlayerY = -1;
    private int newestAIX = -1;
    private int newestAIY = -1;

    private SoundPool soundPool;
    private int soundId;
    private TextView numClick;
    private TextView tvNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_new_game);
        drawView = (DrawView) findViewById(R.id.drawView);

        //zoom ban co, chua su dung duoc
        /*
        drawView = new DrawView(this);
        // Allocate a RegionView.
        final RegView_RelLayout lRegionView = (RegView_RelLayout) findViewById(R.id.regionView);
        // Add some example items to drag.
        lRegionView.addView(drawView);
        // Assert that we only want to drag Views within the confines of the RegionView.
        lRegionView.setWrapContent(false);
        // Assert that after we've finished scaling a View, we want to stop being able to drag it until a new drag is started.
        lRegionView.setDropOnScale(true);
        */

        //undoButton = (Button) findViewById(R.id.btnHome);
        newGameButton = (Button) findViewById(R.id.btnReset);
        homeButton = (Button) findViewById(R.id.btnHome);
        numClick = (TextView) findViewById(R.id.tvNumClick);
        numClick.setText(String.valueOf(COUNT_CLICK));
        soundPool= new SoundPool(10, AudioManager.STREAM_SYSTEM,0);
        soundId = soundPool.load(this,R.raw.sound,1);


        initialChessboard();

        tvNickname = (TextView) findViewById(R.id.tvNickname);
        Typeface nicknameFont = Typeface.createFromAsset(getAssets(), "font/FFF_Tusj.ttf");
        Typeface scoreFont = Typeface.createFromAsset(getAssets(), "font/Chunkfive.otf");
        tvNickname.setTypeface(nicknameFont);
        numClick.setTypeface(scoreFont);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra(NickNameActivity.BUNDLE);
            if (bundle != null) {
                tvNickname.setText(bundle.getString(NickNameActivity.NICKNAME));
            } else {
                tvNickname.setText(intent.getStringExtra(NickNameActivity.NICKNAME));
            }
        }

        drawView.setOnTouchListener(new View.OnTouchListener() {
            boolean down = false;
            int downRow;
            int downCol;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (gameState == STATE_GAME_OVER) {
                    return false;
                }

                float x = event.getX();
                float y = event.getY();
                if (!drawView.inChessBoard(x, y)) {
                    return false;
                }
                int row = drawView.getRow(y);
                int col = drawView.getCol(x);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        down = true;
                        downRow = row;
                        downCol = col;
                        break;

                    case MotionEvent.ACTION_UP:
                        if (down && downRow == row && downCol == col) {
                            down = false;
                            if (!Rule.isLegalMove(chessBoard, new Move(row, col), playerColor)) {
                                return true;
                            }

                            newestPlayerX = row;
                            newestPlayerY = col;

                            Move move = new Move(row, col);
                            List<Move> moves = Rule.move(chessBoard, move, playerColor);
                            drawView.move(chessBoard, moves, move, playerColor);
                            soundPool.play(1,1, 1, 0, 0, 1);
                            COUNT_CLICK ++;
                            numClick.setText(String.valueOf(COUNT_CLICK));


                            gameState = STATE_AI_MOVE;
                            if(Rule.isEnded(chessBoard,move,playerColor)){
                                gameState = STATE_GAME_OVER;
                                gameOverMessage(playerColor);
                            }
                            else {
                                AI ai = new AI();
                                move = ai.getAINextMove(chessBoard, aiColor);

                                newestAIX = move.row;
                                newestAIY = move.col;

                                moves = Rule.move(chessBoard, move, aiColor);
                                drawView.move(chessBoard, moves, move, aiColor);
                                //soundPool.play(1,1, 1, 0, 0, 1);


                                gameState = STATE_PLAYER_MOVE;
                                if (Rule.isEnded(chessBoard, move, aiColor)) {
                                    gameState = STATE_GAME_OVER;
                                    gameOverMessage(aiColor);
                                    COUNT_CLICK=0;
                                    numClick.setText(String.valueOf(COUNT_CLICK));
                                }
                            }
                            //kiem tra da check het o tren ban co
                            if(COUNT_CLICK==50){
                                String msg = "Hết ô rồi, chơi lại nha!";
                                Data.add_win_AI(getApplicationContext());
                                msgDialog = new MessageDialog(NewGameActivity.this, msg);
                                msgDialog.show();
                                initialChessboard();
                                drawView.initialChessBoard();
                                gameState = STATE_PLAYER_MOVE;
                                COUNT_CLICK=0;
                                numClick.setText(String.valueOf(COUNT_CLICK));
                            }
                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:
                        down = false;
                        break;
                }
                return true;
            }
        });

        newGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initialChessboard();
                drawView.initialChessBoard();
                gameState = STATE_PLAYER_MOVE;
                COUNT_CLICK=0;
                numClick.setText(String.valueOf(COUNT_CLICK));
                //drawView.update();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(NewGameActivity.this, MainActivity.class);
                startActivity(intent);
                NewGameActivity.this.finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void initialChessboard(){
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                chessBoard[i][j] = NULL;
            }
        }
    }

    private void gameOverMessage(byte playerColor){
        String msg;
        if (playerColor==BLACK) {
            msg = "Your Win\n High score:" + String.valueOf(100-COUNT_CLICK);
            Data.add_win_AI(getApplicationContext());
        }
        else{
            msg = ":Con gà!\nScore: "+ String.valueOf(100-COUNT_CLICK);
            Data.add_lose_AI(getApplicationContext());
        }

        msgDialog = new MessageDialog(NewGameActivity.this, msg);
        msgDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(NewGameActivity.this, com.team1.caro.minigamecaro.MainActivity.class);
            setResult(RESULT_CANCELED, intent);
            NewGameActivity.this.finish();
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
