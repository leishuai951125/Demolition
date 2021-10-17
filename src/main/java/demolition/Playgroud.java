package demolition;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

class Playgroud {
    int[][] AllGrads = null; //value 是 GradType,前面是y，后面是x
    final int x_count = Public.x_count;  //水平格子数
    final int y_count = Public.y_count;  //锤直格子数
    Player player = null; //玩家
    private int playLife =5;
    volatile int countdown = 180; //倒计时，单位秒
//    ArrayList<Enemy> redEnemys = new ArrayList<Enemy>();
    List<Enemy> redEnemys = new CopyOnWriteArrayList<Enemy>();
    Playgroud() {
        initAllGradsByConfigFile();
        startLoop();
    }
    void startLoop() {
        //敌军行动
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            autoMoveEnemy();
            bombCheck();
        }, 0, 100, TimeUnit.MILLISECONDS);
        //倒计时
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            countdown--;
        }, 0, 1000, TimeUnit.MILLISECONDS);
        //敌军自动扔炸弹
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            enemyAutoReleaseBomb();
        }, 0, 3300, TimeUnit.MILLISECONDS);
    }

    private void enemyAutoReleaseBomb() {
        int randomIndex= Public.random.nextInt(redEnemys.size());
        redEnemys.get(randomIndex).releaseBomb(this);
    }

    private void autoMoveEnemy() {
        System.out.println("移动敌军start");
        for(Human redEnemy:redEnemys){
            int rand = Public.random.nextInt(4); //生成0到4随机数
            int randomDirectionKeyCode = Public.DirectionKeyCodes[rand];
            //移动敌军
            moveHuman(redEnemy, randomDirectionKeyCode);
        }
        System.out.println("移动敌军end");
    }

    //炸弹效果碰撞检测
    private void bombCheck() {
        System.out.println("碰撞检测start");
        for(int y=0;y<y_count;y++){
            for(int x=0;x<x_count;x++){
                int gradType=AllGrads[y][x];
                if(gradType== Public.GridType_BombExploded){ //此处是炸弹
                    Iterator<Enemy> iterator=redEnemys.iterator();
                    while (iterator.hasNext()){
                        Human human=iterator.next();
                        //爆炸范围矩形
                        Location leftTopPxLocation=getGradPxLocation(x,y);
                        Location rightBottomLocation=leftTopPxLocation.clone();
                        rightBottomLocation.x+=Public.GridWith;
                        rightBottomLocation.y+=Public.GridWith;
                        Rectangle coverRectangle=new Rectangle(leftTopPxLocation,rightBottomLocation);
                        //人的矩形
                        Location humanPxLocation=human.getPxLocation();
                        Location humanRightBottomLocation=humanPxLocation.clone();
                        humanRightBottomLocation.x+=Public.GridWith;
                        humanRightBottomLocation.y+=Public.GridWith;
                        Rectangle humanRectangle=new Rectangle(humanPxLocation,humanRightBottomLocation);
                        if(humanRectangle.isOverlap(coverRectangle)){ //被炸
                            System.out.println("被炸,human:"+human.location+"cover:"+new Location(x,y));
                            System.out.println("被炸,human:"+humanPxLocation+"cover:"+leftTopPxLocation);
                            System.out.println("=======");
                            redEnemys.remove(human); //CopyOnWriteArrayList 不能用 iterator.remove();
                        }
                    }
                }
            }
        }
        System.out.println("碰撞检测end");
    }

    //根据方向和可达性，返回移动后的位置
    Location getNewLocation(Location oldLocation, int directionKeyCode) {
        Location newLocation = oldLocation.clone(); //newLocation用来记录移动后的新位置
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
        // 判断位置是否可达
        int newLocationGradType = AllGrads[newLocation.y][newLocation.x];
        boolean newLocationCanArrive = newLocationGradType == Public.GridType_Empty || newLocationGradType == Public.GridType_Destination;
        if (!newLocation.equals(oldLocation) && newLocationCanArrive) { //位置有变化且新位置可抵达
            return newLocation;
        }
        return oldLocation;
    }

    void moveHuman(Human human, int directionKeyCode) {
        if (human.isMotion) { //运动中
            //不做任何事
            return;
        }
        Location newlocation = getNewLocation(human.location, directionKeyCode);
        if (!newlocation.equals(human.location)) {//位置移动，播放动画
            human.doOnceMove(newlocation, directionKeyCode);
        }
    }

    void movePlayer(int directionKeyCode) {
        moveHuman(player, directionKeyCode);
    }

    void drawTitle(PApplet canvas){
        canvas.textSize(Public.GridWith*2/3);
        canvas.color(Public.GridWith*2/3);
        int textTopPx=Public.GridWith*2/3;
        int imgTopPx=Public.GridWith/2;
        //生命
        int playerLifeImgLeftPx=Public.GridWith*4;
        canvas.image(Public.imgCenter.getPlayerLifeImg(),playerLifeImgLeftPx,imgTopPx,Public.GridWith,Public.GridWith);
        int playerLifeTextLeftPx=Public.GridWith*5+Public.GridWith/3;
        canvas.text("x "+Integer.toString(playLife),playerLifeTextLeftPx,textTopPx,Public.GridWith*4,Public.GridWith*3);
        //时间
        int clockImgLeftPx=Public.GridWith*8;
        int clockTextLeftPx=Public.GridWith*9+Public.GridWith/3;
        canvas.image(Public.imgCenter.getClockImg(),clockImgLeftPx,imgTopPx,Public.GridWith,Public.GridWith);
        canvas.text(Integer.toString(countdown),clockTextLeftPx,textTopPx,Public.GridWith*5,Public.GridWith);
    }

    void resetBackground(PApplet canvas){
        canvas.clear();
        int yellow = canvas.color(238, 129, 0);
        canvas.background(yellow);//黄色背景
    }

    void draw(PApplet canvas) {
        //重置背景
        resetBackground(canvas);
        //标题
        drawTitle(canvas);
        //绘制地图，包括：草地、墙、砖、炸弹
        drawGrads(canvas);

        //绘制敌人
        for (Enemy redEnemy : redEnemys) {
            redEnemy.draw(canvas);
        }

        //绘制玩家
        player.draw(canvas);
    }

    Location getGradPxLocation(int x,int y){
        return new Location(x * Public.GridWith,y * Public.GridWith + Public.TopSpaceHeightPx);
    }

    //绘制地图,包括：草地、墙、砖、炸弹
    private void drawGrads(PApplet canvas) {
        PImage emptyImg = Public.imgCenter.getImgByGridType(Public.GridType_Empty);
        for (int i = 0; i < y_count; i++) {
            for (int j = 0; j < x_count; j++) {
                int gridType = AllGrads[i][j];
                PImage img = Public.imgCenter.getImgByGridType(gridType);
                Location pxLocation=getGradPxLocation(j,i);
                int x = pxLocation.x;
                int y = pxLocation.y;
                if (gridType == Public.GridType_Bomb || gridType == Public.GridType_BombExploded) {
                    //绘制炸弹时需要先拿背景填充一下
                    canvas.image(emptyImg, x, y, Public.GridWith, Public.GridWith);
                }
                canvas.image(img, x, y, Public.GridWith, Public.GridWith);
            }
        }
    }

    //按配置文件生成地图
    void initAllGradsByConfigFile() {
        String parentPath = "/Users/leishuai/Downloads/Demolition/src/main/resources/";
        String level1MapFilePath = parentPath + "/game_config/level1.txt";
        File file = new File(level1MapFilePath);
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<String> allLine = new ArrayList<>(10);
        while (sc.hasNextLine()) {
            allLine.add(sc.nextLine());
        }
        sc.close();
        Map<Character, Integer> charToGradType = new HashMap<Character, Integer>() {
            {
                put('W', Public.GridType_Stone); //石头
                put('P', Public.GridType_Player); //玩家
                put(' ', Public.GridType_Empty); //空白
                put('B', Public.GridType_Brick); //砖块
                put('Y', Public.GridType_RedEnemy); //敌军
                put('R', Public.GridType_RedEnemy); //敌军 todo
                put('G', Public.GridType_Destination); //终点
            }
        };
        AllGrads = new int[y_count][x_count];
        for (int y = 0; y < y_count; y++) {
            for (int x = 0; x < x_count; x++) {
                char c = allLine.get(y).charAt(x);
                int gradType = charToGradType.get(c);
                switch (gradType) {
                    case Public.GridType_Player: {
                        player = new Player(new Location(x, y));
                        break;
                    }
                    case Public.GridType_RedEnemy: {
                        redEnemys.add(new Enemy(new Location(x, y)));
                        break;
                    }
                    default: {
                        AllGrads[y][x] = gradType;
                    }
                }
            }
        }
    }
}
