package com.sdsl.jawad.snakegame;

import android.graphics.Bitmap;

/**
 * Created by ishmum on 28/10/16.
 */

public class MyAnimation {

    private Bitmap[] mFrames;
    private int currentFrame;
    private long startTime;
    private long delay;
    private boolean playedOnce;

    public void setFrames(Bitmap[] frames){
        mFrames = frames;
        currentFrame = 0;
        startTime = System.nanoTime();
    }

    public void setDelay(long d){delay = d;}
    public void setFrame(int i){currentFrame = i;}

    public void update(){
        long elapsed = (System.nanoTime() - startTime)/1000000;

        if (elapsed > delay) {
            currentFrame++;
            startTime = System.nanoTime();
        }

        if (currentFrame == mFrames.length){
            currentFrame = 0;
            playedOnce = true;
        }
    }

    public Bitmap getImage(){
        return mFrames[currentFrame];
    }

    public int getFrame(){return currentFrame;}
    public boolean isPlayedOnce(){return playedOnce;}
}
