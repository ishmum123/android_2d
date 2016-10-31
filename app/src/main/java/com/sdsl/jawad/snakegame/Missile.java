package com.sdsl.jawad.snakegame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by ishmum on 28/10/16.
 */

public class Missile extends GameObject {

    private int score;
    private int speed;
    private Random mRandom = new Random();
    private MyAnimation mMyAnimation = new MyAnimation();
    private Bitmap spritesheet;

    public Missile(Bitmap res, int x, int y, int w, int h, int s, int numFrames){
        super.x = x;
        super.y = y;
        width = w;
        height = h;
        score = s;

        speed = 7 + (int) (mRandom.nextDouble()*score/30);

        //cap missile speed
        if (speed > 40) speed = 40;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for (int i = 0; i < image.length; i++){
            image[i] = Bitmap.createBitmap(spritesheet, 0, i*height, width, height);
        }

        mMyAnimation.setFrames(image);
        mMyAnimation.setDelay(100 - speed);
    }

    public void update(){
        x -= speed;
        mMyAnimation.update();
    }

    public void draw(Canvas canvas){
        try{
            canvas.drawBitmap(mMyAnimation.getImage(), x, y, null);
        }
        catch (Exception e){}
    }

    @Override
    public int getWidth() {
        //offset slightly for more realistic collition
        return width - 10;
    }
}
