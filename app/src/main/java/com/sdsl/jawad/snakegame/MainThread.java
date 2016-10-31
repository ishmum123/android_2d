package com.sdsl.jawad.snakegame;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by ishmum on 28/10/16.
 */

public class MainThread extends Thread {

    private int FPS = 30;
    private double averageFPS;
    private SurfaceHolder mSurfaceHolder;
    private GamePanel mGamePanel;
    private boolean running;
    public static Canvas sCanvas;

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel){
        super();
        this.mSurfaceHolder = surfaceHolder;
        this.mGamePanel = gamePanel;
    }

    @Override
    public void run(){
        long startTime;
        long timeMilis;
        long waitTime;
        long totaltime = 0;
        int frameCount = 0;
        long targetTime = 1000/FPS;


        while (running){
            startTime = System.nanoTime();
            sCanvas = null;

            //try looking the canvas for pixel editing
            try {
                sCanvas = this.mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder){
                    this.mGamePanel.update();
                    this.mGamePanel.draw(sCanvas);
                }
            }
            catch (Exception e){}
            finally {
                if (sCanvas != null){
                    try {
                        mSurfaceHolder.unlockCanvasAndPost(sCanvas);

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            timeMilis = (System.nanoTime() - startTime)/1000000;
            waitTime = targetTime - timeMilis;

            try{
                this.sleep(waitTime);
            }
            catch (Exception e){}

            totaltime = System.nanoTime() - startTime;
            frameCount++;

            if (frameCount == FPS){
                averageFPS = 1000/((totaltime/frameCount)/1000000);
                frameCount = 0;
                totaltime = 0;
            }
        }
    }

    public void setRunning(boolean b){
        running = b;
    }
}
