package com.team1.caro.minigamecaro;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import java.util.List;
import com.team1.caro.minigamecaro.R;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    private RenderThread thread;

    private float screenWidth;
    private float bgLength;
    private float chessBoardLength;
    private float scale[] = new float[] { 0.75f, 0.80f, 0.85f, 0.90f, 0.95f };
    private int scaleLevel = 4;

    private static final int M = 10;

    private float a;

    private float chessBoardLeft;
    private float chessBoardRight;
    private float chessBoardTop;
    private float chessBoardBottom;

    private static final byte NULL = 0;
    private static final byte BLACK = -1;
    private static final byte WHITE = 1;

    private float margin;

    private byte[][] chessBoard;
    private int[][] index;

    private Bitmap[] images;
    private Bitmap background;

    private float ratio = 0.9f;


    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ReversiView);
        ratio = typedArray.getFloat(R.styleable.ReversiView_ratio, 0.9f);

        getHolder().addCallback(this);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;

        //screenWidth = dm.widthPixels;
        screenWidth = dm.heightPixels;
        bgLength = screenWidth * scale[scaleLevel];
        chessBoardLength = 10f / 11f * bgLength;
        a = chessBoardLength / M;
        margin = 1f / 22f * bgLength;
        chessBoardLeft = margin;
        chessBoardRight = chessBoardLeft + M * a;
        chessBoardTop = margin;
        chessBoardBottom = chessBoardTop + M * a;
        images = new Bitmap[22];
        loadChesses(context);
        background = loadBitmap(bgLength, bgLength, context.getResources().getDrawable(R.drawable.wood));
        initialChessBoard();
    }

    public DrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context) {
        this(context, null, 0);
    }

    public void initialChessBoard(){
        chessBoard = new byte[M][M];
        index = new int[M][M];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                chessBoard[i][j] = NULL;
            }
        }
    }

    //TODO
    public void updateChessBoard(byte newchessBoard[][]){
        this.copyBinaryArray(newchessBoard, this.chessBoard);

        for (int row = 0; row<10;row++)
            for(int col = 0; col <10 ; col++)
                if (chessBoard[row][col] == WHITE) {
                    index[row][col] = 11;
                } else if (chessBoard[row][col] == BLACK) {
                    index[row][col] = 0;
                }

        this.update();
    }


    private int updateIndex(int index, int color) {

        if (index == 0 || index == 11) {
            return index;
        } else if (index >= 1 && index <= 10 || index >= 12 && index <= 21) {
            return (index + 1) % 22;
        } else {
            return defaultIndex(color);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) bgLength, View.MeasureSpec.EXACTLY);
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) bgLength, View.MeasureSpec.EXACTLY);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void move(byte[][] chessBoard, List<Move> reversed, Move move, int chessColor) {

        //Util.copyBinaryArray(chessBoard, this.chessBoard);
        this.copyBinaryArray(chessBoard, this.chessBoard);

        for (int i = 0; i < reversed.size(); i++) {
            int reverseRow = reversed.get(i).row;
            int reverseCol = reversed.get(i).col;
            if (chessBoard[reverseRow][reverseCol] == WHITE) {
                index[reverseRow][reverseCol] = 1;
            } else if (chessBoard[reverseRow][reverseCol] == BLACK) {
                index[reverseRow][reverseCol] = 12;
            }
        }
        int row = move.row, col = move.col;
        if (chessBoard[row][col] == WHITE) {
            index[row][col] = 11;
        } else if (chessBoard[row][col] == BLACK) {
            index[row][col] = 0;
        }

    }

    public void update() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                if (chessBoard[i][j] == NULL)
                    continue;
                index[i][j] = updateIndex(index[i][j], chessBoard[i][j]);
            }
        }

    }

    public boolean inChessBoard(float x, float y) {
        return x >= chessBoardLeft && x <= chessBoardRight && y >= chessBoardTop && y <= chessBoardBottom;
    }

    public int getRow(float y) {
        return (int) Math.floor((y - chessBoardTop) / a);
    }

    public int getCol(float x) {
        return (int) Math.floor((x - chessBoardLeft) / a);
    }

    public void render(Canvas canvas) {
        Paint paint1 = new Paint();

        canvas.drawBitmap(background, 0, 0, paint1);

        Paint paint2 = new Paint();
        paint2.setColor(Color.BLACK);
        paint2.setStrokeWidth(3);
        //TODO
        for (int i = 0; i < M+1; i++) {
            canvas.drawLine(chessBoardLeft, chessBoardTop + i * a, chessBoardRight, chessBoardTop + i * a, paint2);
            canvas.drawLine(chessBoardLeft + i * a, chessBoardTop, chessBoardLeft + i * a, chessBoardBottom, paint2);
        }

        Paint paint3 = new Paint();
        for (int col = 0; col < M; col++) {
            for (int row = 0; row < M; row++) {
                if (chessBoard[row][col] != NULL) {
                    canvas.drawBitmap(images[index[row][col]], chessBoardLeft + col * a, chessBoardTop + row * a, paint3);
                }
            }
        }
    }

    public int defaultIndex(int color) {
        if (color == WHITE)
            return 11;
        else if (color == BLACK)
            return 0;
        return -1;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new RenderThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public Bitmap loadBitmap(float width, float height, Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //drawable.setBounds(0, 0, (int) width, (int) height);
        drawable.setBounds(0, 0, (int) width, (int) height);
        drawable.draw(canvas);
        return bitmap;
    }

    private void loadChesses(Context context) {
        images[0] = loadBitmap(a, a, context.getResources().getDrawable(R.drawable.stone_b1));
        images[11] = loadBitmap(a, a, context.getResources().getDrawable(R.drawable.stone_w2));
    }

    public static void copyBinaryArray(byte[][] src, byte[][] dest) {
        for (int i = 0; i < M; i++) {
            System.arraycopy(src[i], 0, dest[i], 0, M);
        }
    }
}
