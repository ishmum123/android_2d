package com.sdsl.jawad.snakegame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by ishmum on 29/10/16.
 */

public class Explosion {

    private int x, y, width, height, row;
    private MyAnimation mMyAnimation = new MyAnimation();
    private Bitmap spritesheet;

    public Explosion(Bitmap res, int x, int y, int w, int h, int numFrames){
        this.x = x;
        this.y = y;
        this.height = h;
        this.width = w;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for (int i = 0; i < image.length; i++){
            if (i % 5 == 0 && i > 0) row++;
            image[i] = Bitmap.createBitmap(spritesheet, (i - (5 * row)) * width, row * height, width, height);
        }

        mMyAnimation.setFrames(image);
        mMyAnimation.setDelay(10);
    }

    public void draw(Canvas canvas){
        if (!mMyAnimation.isPlayedOnce()){
            canvas.drawBitmap(mMyAnimation.getImage(), x, y, null);
        }
    }

    public void update(){
        if (!mMyAnimation.isPlayedOnce()){
            mMyAnimation.update();
        }
    }

    public int getHeight(){return height;}
}
