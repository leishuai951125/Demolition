package demolition;

import processing.core.PApplet;
import processing.event.KeyEvent;


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
        textSize(Public.GridWith*2/3); //文字大小
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
            playgroud.player.releaseBomb(playgroud); //投放炸弹
        }
    }
}

