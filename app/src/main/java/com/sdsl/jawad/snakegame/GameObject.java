package com.sdsl.jawad.snakegame;

import android.graphics.Rect;

/**
 * Created by ishmum on 28/10/16.
 */

public abstract class GameObject {

    protected int x, y, dx, dy, width, height;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setY(int y) {

        this.y = y;
    }

    public Rect getRectangle(){
        return new Rect(x, y, x+width, y+height);
    }
}
