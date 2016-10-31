package com.sdsl.jawad.snakegame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by ishmum on 28/10/16.
 */

public class Player extends GameObject {

    private Bitmap spritesheet;
    private int score;
    private boolean up;
    private boolean playing;
    private MyAnimation mMyAnimation = new MyAnimation();
    private long startTime;

    public Player(Bitmap res, int w, int h, int numFrames){
        x = 100;
        y = GamePanel.HEIGHT/2;
        dy = 0;
        score = 0;
        height = h;
        width = w;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++){
            image[i] = Bitmap.createBitmap(spritesheet, i*width, 0, width, height);
        }

        mMyAnimation.setFrames(image);
        mMyAnimation.setDelay(10);
        startTime = System.nanoTime();
    }

    public void setUp(boolean b){ up = b;}

    public void update(){
        long elapsed = (System.nanoTime() - startTime)/1000000;
        if (elapsed > 100){
            score++;
            startTime = System.nanoTime();
        }
        mMyAnimation.update();

        if (up){
            dy -= 1;
        }
        else {
            dy += 1;
        }

        if (dy > 14) {
            dy = 14;
        }
        if (dy < (-14)) {
            dy = -14;
        }

        //y += dy*2;
        y += dy;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(mMyAnimation.getImage(), x, y, null);
    }

    public int getScore(){return score;}
    public boolean isPlaying(){return playing;}
    public void setPlaying(Boolean b){playing = b;}
    public void resetDY(){dy = 0;}
    public void resetScore(){score = 0;}
}
