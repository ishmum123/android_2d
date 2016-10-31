package com.sdsl.jawad.snakegame;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ishmum on 28/10/16.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missileStartTime;
    private MainThread mThread;
    private Background mBackground;
    private Player mPlayer;
    private Random mRandom = new Random();
    private ArrayList<SmokePuff> mSmoke;
    private ArrayList<Missile> mMissiles;
    private ArrayList<TopBorder> mTopBorders;
    private ArrayList<BottomBorder> mBottomBorders;
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean bottomDown = true;
    private boolean newGameCreated;
    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;

    private Explosion mExplosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started;
    private  int best;

    public GamePanel(Context context){
        super(context);


        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        //set gamepanel focusable so that it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while(retry && counter < 1000){
            counter++;
            try{
                mThread.setRunning(false);
                mThread.join();
                retry = false;
                mThread = null;
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mBackground = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.unnamed));
        mPlayer = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        mSmoke = new ArrayList<SmokePuff>();
        mMissiles = new ArrayList<Missile>();
        mTopBorders = new ArrayList<TopBorder>();
        mBottomBorders = new ArrayList<BottomBorder>();
        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        //we can safely start the gameloop here
        mThread = new MainThread(getHolder(), this);
        mThread.setRunning(true);
        mThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if (!mPlayer.isPlaying() && newGameCreated && reset){
                mPlayer.setPlaying(true);
                mPlayer.setUp(true);
            }
            if (mPlayer.isPlaying()){
                if (!started) started = true;
                reset = false;
                mPlayer.setUp(true);
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP){
            mPlayer.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update(){
        if (mPlayer.isPlaying()) {

            if (mBottomBorders.isEmpty()){
                mPlayer.setPlaying(false);
                return;
            }
            if (mTopBorders.isEmpty()){
                mPlayer.setPlaying(false);
                return;
            }

            mBackground.update();
            mPlayer.update();

            //Calculate the threshold of height of the borders based on the player's score
            maxBorderHeight = 30 + mPlayer.getScore() / progressDenom;
            //cap max border height so that both borders together can take only half of the screen
            if (maxBorderHeight > HEIGHT/4) maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5 + mPlayer.getScore() / progressDenom;

            //check top border for collision
            for (int i = 0; i < mTopBorders.size(); i++){
                if (collision(mTopBorders.get(i), mPlayer)){
                    mPlayer.setPlaying(false);
                }
            }

            //check botttom border for collision
            for (int i = 0; i < mBottomBorders.size(); i++){
                if (collision(mBottomBorders.get(i), mPlayer)){
                    mPlayer.setPlaying(false);
                }
            }

            //update top border
            this.updateTopBorder();

            //update bottom border
            this.updateBottomBorder();

            //add missile on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - mPlayer.getScore()/4)){

                //first missile always goes down the middle
                if(mMissiles.size() == 0){
                    mMissiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH + 10,
                            HEIGHT/2, 45, 15, mPlayer.getScore(), 13));
                }
                else {
                    mMissiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH+10,
                            (int) (mRandom.nextDouble()*(HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight), 45, 15, mPlayer.getScore(), 13));
                }

                //reset timer
                missileStartTime = System.nanoTime();
            }

            //loop through every missile and check collision and remove
            for (int i = 0; i < mMissiles.size(); i++) {
                //update missile
                mMissiles.get(i).update();

                //check for collision
                if (collision(mMissiles.get(i), mPlayer)) {
                    mMissiles.remove(i);
                    mPlayer.setPlaying(false);
                    break;
                }

                //remove if missile is way off the screen
                if (mMissiles.get(i).getX() < -100) {
                    mMissiles.remove(i);
                    break;
                }
            }

            //add smoke puffs
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (elapsed > 120){
                mSmoke.add(new SmokePuff(mPlayer.getX(), mPlayer.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            for (int i = 0; i < mSmoke.size(); i++){
                mSmoke.get(i).update();
                if (mSmoke.get(i).getX() < -10){
                    mSmoke.remove(i);
                }
            }
        }

        else {
            mPlayer.resetDY();
            if (!reset){
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                mExplosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), mPlayer.getX(),
                        mPlayer.getY() - 30, 100, 100, 25);
            }

            mExplosion.update();
            long resetElapsed = (System.nanoTime() - startReset)/1000000;

            if (resetElapsed > 2500 && !newGameCreated){
                newGame();
            }
        }
    }

    public boolean collision(GameObject a, GameObject b){
        if (Rect.intersects(a.getRectangle(), b.getRectangle())){
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {

        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            mBackground.draw(canvas);

            if (!disappear){
                mPlayer.draw(canvas);
            }

            //draw smokepuffs
            for (SmokePuff sp: mSmoke){
                sp.draw(canvas);
            }

            //draw missiles
            for (Missile m : mMissiles){
                m.draw(canvas);
            }

            //draw topborder
            for(TopBorder tb : mTopBorders){
                tb.draw(canvas);
            }

            //draw bottomborder
            for (BottomBorder bb : mBottomBorders){
                bb.draw(canvas);
            }

            //draw explosion
            if (started){
                mExplosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void updateTopBorder(){
        //every 50 points insert randomly placed top block to break the pattern
        if (mPlayer.getScore() % 50 == 0){
            mTopBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                    R.drawable.brick), mTopBorders.get(mTopBorders.size() - 1).getX()+20, 0,
                    (int) ((mRandom.nextDouble()*maxBorderHeight)+1)));
        }

        for (int i = 0; i < mTopBorders.size(); i++){
            mTopBorders.get(i).update();
            if (mTopBorders.get(i).getX() < -20){
                mTopBorders.remove(i);

                //remove element from arraylist but replace with new one

                //calculate topdown which determines the direction of the border
                if (mTopBorders.get(mTopBorders.size() -1).getHeight() >= maxBorderHeight){
                    topDown = false;
                }
                if (mTopBorders.get(mTopBorders.size() -1).getHeight() <= minBorderHeight){
                    topDown = true;
                }

                if (topDown){
                    //new border will have larger height
                    mTopBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            mTopBorders.get(mTopBorders.size() - 1).getX() + 20,
                            0, mTopBorders.get(mTopBorders.size() - 1).getHeight() + 1));
                }
                else {
                    //new border will have smaller height
                    mTopBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            mTopBorders.get(mTopBorders.size() - 1).getX() + 20,
                            0, mTopBorders.get(mTopBorders.size() - 1).getHeight() - 1));
                }
            }
        }
    }

    public void updateBottomBorder(){
        //insert randomly placed bottom blocks every 40 points
        if (mPlayer.getScore() % 40 == 0){
            mBottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),
                    R.drawable.brick), mBottomBorders.get(mBottomBorders.size() - 1).getX() + 20,
                    (int) ((mRandom.nextDouble() * maxBorderHeight) + (HEIGHT - maxBorderHeight))));
        }

        for (int i = 0; i < mBottomBorders.size(); i++){
            mBottomBorders.get(i).update();

            //if border is off screen remove it and add corresponding new one
            if (mBottomBorders.get(i).getX() < -20){
                mBottomBorders.remove(i);

                //determine if border will be moving up or down
                if (mBottomBorders.get(mBottomBorders.size() -1).getY() <= (HEIGHT - maxBorderHeight)){
                    bottomDown = true;
                }
                if (mBottomBorders.get(mBottomBorders.size() -1).getY() >= (HEIGHT - minBorderHeight)){
                    bottomDown = false;
                }

                if (bottomDown){
                    //new border will have larger height
                    mBottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            mBottomBorders.get(mBottomBorders.size() - 1).getX() + 20,
                            mBottomBorders.get(mBottomBorders.size() - 1).getY() + 1));
                }
                else {
                    //new border will have smaller height
                    mBottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            mBottomBorders.get(mBottomBorders.size() - 1).getX() + 20,
                            mBottomBorders.get(mBottomBorders.size() - 1).getY() - 1));
                }
            }

            newGameCreated = true;
        }


    }

    public void newGame(){
        disappear = false;

        mBottomBorders.clear();
        mTopBorders.clear();

        mMissiles.clear();
        mSmoke.clear();
        mPlayer.resetDY();
        mPlayer.resetScore();
        mPlayer.setY(HEIGHT / 2);

        minBorderHeight = 5;
        maxBorderHeight = 30;

        if (mPlayer.getScore() > best){
            best = mPlayer.getScore();
        }

        //create initial borders

        //initial top borders
        for (int i = 0; i * 20 < WIDTH + 40; i++){
            //first top border created
            if (i == 0){
                mTopBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, 0 , 10));
            }
            else {
                mTopBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, 0 , mTopBorders.get(i - 1).getHeight() + 1));
            }
        }

        //initial bottom borders
        for (int i = 0; i * 20 < WIDTH + 40; i++){
            //first bottom border created
            if (i == 0){
                mBottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, HEIGHT - minBorderHeight));
            }
            else {
                mBottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, mBottomBorders.get(i - 1).getY() - 1));
            }
        }

        newGameCreated = true;
    }

    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        //paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTypeface(Typeface.create("Helvetica", Typeface.BOLD_ITALIC));
        canvas.drawText("DISTANCE: " + (mPlayer.getScore() * 3), 10, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if (!mPlayer.isPlaying() && newGameCreated && reset){
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2 - 50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-50, HEIGHT/2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2 + 40, paint1);
        }
    }
}