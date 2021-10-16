package demolition;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App extends PApplet {
    public static final int WIDTH = 720;
    public static final int HEIGHT = WIDTH;

    public static final int FPS = 60;

    public Playgroud playgroud = null;

    public App() {
    }

    //程序初始化时调用一次
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }
    //程序初始化时调用一次
    @Override
    public void setup() {
        int yellow = color(238, 129, 0);
        background(yellow);//黄色背景
        frameRate(FPS);
        Public.imgCenter = new ImgCenter(this);
        playgroud = new Playgroud();
    }

    //被循环调用
    @Override
    public void draw() {
        playgroud.draw(this);
    }

    public static void main(String[] args) {
        PApplet.main("demolition.App");
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (Public.isDirectionKeyCode(event.getKeyCode())) { //是方向键
            playgroud.movePlayer(event.getKeyCode()); //移动玩家（修改玩家的位置）
        } else if (event.getKeyCode() == Public.KeyCode_ReleaseBomb) {  //是投炸弹的键（是空格键）
            playgroud.releaseBomb(); //投放炸弹
        }
    }
}

