package demolition;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class App extends PApplet {
    public static final int WIDTH = 720;
    public static final int HEIGHT = WIDTH;

    public static final int FPS = 60;

    public Playgroud playgroud = null;

    public App() {
    }

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    @Override
    public void setup() {
        frameRate(FPS);
        Public.imgCenter = new ImgCenter(this);
        playgroud = new Playgroud();
    }

    @Override
    public void draw() {
        int yellow = color(255, 145, 0);
        background(yellow);//黄色背景
        playgroud.draw(this);
    }

    public static void main(String[] args) {
        PApplet.main("demolition.App");
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (Public.isDirectionKeyCode(event.getKeyCode())) { //移动
            playgroud.movePlayer(event.getKeyCode());
        } else if (event.getKeyCode() == Public.KeyCode_ReleaseBomb) { //投放炸弹
            playgroud.releaseBomb();
        }
    }
}

//公共基建或者配置
class Public {
    public static ImgCenter imgCenter = null;
    public static final int x_count = 15;  //水平格子数
    public static final int y_count = 13;  //锤直格子数
    public static int GridWith = App.WIDTH/x_count;  //480/15=32
    public static int HumanOverstep = Public.GridWith / 2;
    // GridType 常量定义
    static final int GridType_Empty = 0; //
    static final int GridType_Stone = 1; //石头
    static final int GridType_Brick = 2; //砖
    static final int GridType_Destination = 3; //终点
    static final int GridType_Player = 4; //玩家
    static final int GridType_RedEnemy = 5; //红色敌军
    //    static final int GridType_YellowEnemy = 6; //黄色敌军
    static final int GridType_Bomb = -1; //炸弹
    // Direction 方向常量定义
    static final int DirectionKeyCode_Up = java.awt.event.KeyEvent.VK_UP;
    static final int DirectionKeyCode_Down = java.awt.event.KeyEvent.VK_DOWN;
    static final int DirectionKeyCode_Left = java.awt.event.KeyEvent.VK_LEFT;
    static final int DirectionKeyCode_Right = java.awt.event.KeyEvent.VK_RIGHT;
    static final int[] DirectionKeyCodes = new int[]{DirectionKeyCode_Up, DirectionKeyCode_Down, DirectionKeyCode_Left, DirectionKeyCode_Right};
    //
    static final int KeyCode_ReleaseBomb = java.awt.event.KeyEvent.VK_SPACE;

    //是否为有效的方向
    static final boolean isDirectionKeyCode(int keyCode) {
        for (int i = 0; i < DirectionKeyCodes.length; i++) {
            if (keyCode == DirectionKeyCodes[i]) {
                return true;
            }
        }
        return false;
    }
    public static Random random=new Random();
}

class Playgroud {
    int[][] AllGrads = null;
    final int x_count = Public.x_count;  //水平格子数
    final int y_count = Public.y_count;  //锤直格子数
    final int topSpaceHeightPx = Public.GridWith * 2;
    private Location playLocation = null;
    private ArrayList<Location> redEnemyLocations = new ArrayList<Location>();

    Playgroud() {
        initAllGrads();
        startLoop();
    }

    void startLoop() {
        new Thread(() -> {
            while (true){
                try {
                    Thread.sleep(500); //一秒一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //敌军行动
                for (int i = 0; i < redEnemyLocations.size(); i++) {
                    int rand = Public.random.nextInt(4); //生成0到4随机数
                    int randomDirectionKeyCode = Public.DirectionKeyCodes[rand];
                    Location redEnemyLocation = redEnemyLocations.get(i);
                    //移动敌军
                    redEnemyLocations.set(i, moveHuman(redEnemyLocation, randomDirectionKeyCode));
                }
            }
        }).start();
    }

    void initAllGrads() {
        AllGrads = new int[y_count][x_count]; //demo上的大小
        for (int i = 0; i < y_count; i++) {
            AllGrads[i][0] = Public.GridType_Stone;
            AllGrads[i][x_count - 1] = Public.GridType_Stone;
        }
        for (int j = 0; j < x_count; j++) {
            AllGrads[0][j] = Public.GridType_Stone;
            AllGrads[y_count - 1][j] = Public.GridType_Stone;
        }
        for(int j=4;j<12;j++){
            AllGrads[4][j] = Public.GridType_Brick;
            AllGrads[6][j] = Public.GridType_Brick;
            AllGrads[8][j] = Public.GridType_Brick;
        }
        AllGrads[y_count - 2][x_count - 2] = Public.GridType_Destination;
        //玩家
        playLocation = new Location(1, 1); //玩家不存在AllGrads，因为玩家和终点可能重合
        //敌军
        redEnemyLocations.add(new Location(3, 3));
        redEnemyLocations.add(new Location(9, 10));
    }

    void releaseBomb() {
        AllGrads[playLocation.y][playLocation.x] = Public.GridType_Bomb;
//        bombLocations.add(playLocation.clone());
    }

    Location moveHuman(Location oldLocation, int directionKeyCode) {
        Location newLocation = oldLocation.clone();
        switch (directionKeyCode) {
            case Public.DirectionKeyCode_Up: {
                newLocation.y--;
                break;
            }
            case Public.DirectionKeyCode_Down: {
                newLocation.y++;
                break;
            }
            case Public.DirectionKeyCode_Left: {
                newLocation.x--;
                break;
            }
            case Public.DirectionKeyCode_Right: {
                newLocation.x++;
                break;
            }
            default: {
                //nothing
            }
        }
        int newLocationGradType = AllGrads[newLocation.y][newLocation.x];
        boolean newLocationCanArrive = newLocationGradType == Public.GridType_Empty || newLocationGradType == Public.GridType_Destination;
        if (!newLocation.equals(oldLocation) && newLocationCanArrive) { //位置有变化且新位置可抵达
            return newLocation;
        }
        return oldLocation;
    }

    void movePlayer(int directionKeyCode) {
        playLocation = moveHuman(playLocation, directionKeyCode);
    }

    void draw(PApplet canvas) {
        int x, y;

        PImage emptyImg = Public.imgCenter.getImgByGridType(Public.GridType_Empty);

        //绘制地图,包括：草地、墙、砖、炸弹
        for (int i = 0; i < y_count; i++) {
            for (int j = 0; j < x_count; j++) {
                int gridType = AllGrads[i][j];
                PImage img = Public.imgCenter.getImgByGridType(gridType);
                x = j * Public.GridWith;
                y = i * Public.GridWith + topSpaceHeightPx;
                if (gridType == Public.GridType_Bomb) { //绘制炸弹时需要先拿背景填充一下
                    canvas.image(emptyImg, x, y, Public.GridWith, Public.GridWith);
                }
                canvas.image(img, x, y, Public.GridWith, Public.GridWith);
            }
        }


        //绘制敌人
        for (Location redEnemyLocation : redEnemyLocations) {
            PImage enemyImg = Public.imgCenter.getImgByGridType(Public.GridType_RedEnemy);
            x = redEnemyLocation.x * Public.GridWith;
            y = redEnemyLocation.y * Public.GridWith + topSpaceHeightPx;
            canvas.image(enemyImg, x, y - Public.HumanOverstep, Public.GridWith, Public.GridWith + Public.HumanOverstep);
        }

        //绘制玩家
        PImage img = Public.imgCenter.getImgByGridType(Public.GridType_Player);
        x = playLocation.x * Public.GridWith;
        y = playLocation.y * Public.GridWith + topSpaceHeightPx;
        canvas.image(img, x, y - Public.HumanOverstep, Public.GridWith, Public.GridWith + Public.HumanOverstep);
    }
}

class ImgCenter {
    HashMap<Integer, PImage> gridTypeToImg = null;

    ImgCenter(PApplet imgLoader) {
        loadImg(imgLoader);
    }

    private void loadImg(PApplet imgLoader) {
        gridTypeToImg = new HashMap<Integer, PImage>(10);
        String parentPath = "/Users/leishuai/Downloads/Demolition/src/main/resources/";
        gridTypeToImg.put(Public.GridType_Empty, imgLoader.loadImage(parentPath + "empty/empty.png"));
        gridTypeToImg.put(Public.GridType_Stone, imgLoader.loadImage(parentPath + "wall/solid.png"));
        gridTypeToImg.put(Public.GridType_Brick, imgLoader.loadImage(parentPath + "broken/broken.png"));
        gridTypeToImg.put(Public.GridType_Destination, imgLoader.loadImage(parentPath + "goal/goal.png"));
        gridTypeToImg.put(Public.GridType_Player, imgLoader.loadImage(parentPath + "player/player.gif"));
        gridTypeToImg.put(Public.GridType_RedEnemy, imgLoader.loadImage(parentPath + "red_enemy/red_down1.png"));
        gridTypeToImg.put(Public.GridType_Bomb, imgLoader.loadImage(parentPath + "bomb/bomb.png"));
    }

    PImage getImgByGridType(int GridType) {
        return gridTypeToImg.get(GridType);
    }
}

class Location {
    int x, y; //x 水平方向坐标,y 竖直方向坐标

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Location clone() {
        return new Location(x, y);
    }

    public boolean equals(Location o) {
        return this.x == o.x && this.y == o.y;
    }
}

