package demolition;

import org.checkerframework.checker.units.qual.A;
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

    volatile int a=1;
    public static void main(String[] args) {
//        App app=new App();
//        new Thread(()->{
//            while (true){
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("app.a++:"+app.a);
//                app.a++;
//            }
//
//        }).start();
//        Public.RepartRunWithLimt(()->{
//            System.out.println(app.a);
//        },3000,3,()->{
//            System.out.println("finish");
//        });

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

