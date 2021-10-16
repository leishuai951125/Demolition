package demolition;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.TimeUnit;

class Playgroud {
    int[][] AllGrads = null; //value 是 GradType
    final int x_count = Public.x_count;  //水平格子数
    final int y_count = Public.y_count;  //锤直格子数
    final int topSpaceHeightPx = Public.GridWith * 2;
    private Location playLocation = null; //玩家的位置
    private ArrayList<Location> redEnemyLocations = new ArrayList<Location>();

    Playgroud() {
//        randomInitAllGrads();
        initAllGradsByConfigFile();
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

    List<Location> getBombExplodedCover(Location bombLocation){
        int y=bombLocation.y;
        int x=bombLocation.x;
        AllGrads[y][x] = Public.GridType_Bomb;
        //计算爆炸范围
        List<Location>  bombExplodedCover=new ArrayList<>(5); //爆炸范围
        bombExplodedCover.add(new Location(x,y));
        if(AllGrads[y-1][x]!=Public.GridType_Stone){//上面不是石头
            bombExplodedCover.add(new Location(x,y-1));
        }
        if(AllGrads[y+1][x]!=Public.GridType_Stone){//下面不是石头
            bombExplodedCover.add(new Location(x,y+1));
        }
        if(AllGrads[y][x-1]!=Public.GridType_Stone){//左边不是石头
            bombExplodedCover.add(new Location(x-1,y));
        }
        if(AllGrads[y][x+1]!=Public.GridType_Stone){//右边不是石头
            bombExplodedCover.add(new Location(x+1,y));
        }
        return bombExplodedCover;
    }

    void releaseBomb() {
        int y=playLocation.y;
        int x=playLocation.x;
        //放置炸弹
        AllGrads[y][x] = Public.GridType_Bomb;
        //-------------------------
        //计算爆炸影响范围
        List<Location>  bombExplodedCover=getBombExplodedCover(playLocation);
        //设置爆炸定时器
        Public.scheduledExecutorService.schedule(()->{ //两秒后爆炸
            for(Location coverLocation:bombExplodedCover){ //爆炸
                if(playLocation.equals(coverLocation)){ //炸到玩家，玩家死亡
                    //todo 死亡逻辑
                }
                Iterator<Location> iterator=redEnemyLocations.iterator();
                while (iterator.hasNext()){
                    Location enemyLocation=iterator.next();
                    if(enemyLocation.equals(coverLocation)){
                        iterator.remove(); //敌军死亡
                    }
                }
                AllGrads[coverLocation.y][coverLocation.x] = Public.GridType_BombExploded;
            }
            Public.scheduledExecutorService.schedule(()->{//一秒后爆炸效果清空
                for(Location location:bombExplodedCover){
                    AllGrads[location.y][location.x] = Public.GridType_Empty;
                }
            },1, TimeUnit.SECONDS);
        },2, TimeUnit.SECONDS);
    }

    //根据方向和可达性，返回移动后的位置
    Location moveHuman(Location oldLocation, int directionKeyCode) {
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
                if (gridType == Public.GridType_Bomb || gridType == Public.GridType_BombExploded) {
                    //绘制炸弹时需要先拿背景填充一下
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

    //按配置文件生成地图
    void initAllGradsByConfigFile(){
        String parentPath = "/Users/leishuai/Downloads/Demolition/src/main/resources/";
        String level1MapFilePath = parentPath+"/game_config/level1.txt";
        File file=new File(level1MapFilePath);
        Scanner sc=null;
        try {
            sc=new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<String> allLine=new ArrayList<>(10);
        while (sc.hasNextLine()){
            allLine.add(sc.nextLine());
        }
        sc.close();
        Map<Character,Integer> charToGradType =new HashMap<Character,Integer>(){
            {
                put('W',Public.GridType_Stone); //石头
                put('P',Public.GridType_Player); //玩家
                put(' ',Public.GridType_Empty); //空白
                put('B',Public.GridType_Brick); //砖块
                put('Y',Public.GridType_RedEnemy); //敌军
                put('R',Public.GridType_RedEnemy); //敌军 todo
                put('G',Public.GridType_Destination); //终点
            }
        };
        AllGrads=new int[y_count][x_count];
        for(int y=0;y<y_count;y++){
            for(int x=0;x<x_count;x++){
                char c=allLine.get(y).charAt(x);
                int gradType=charToGradType.get(c);
                switch (gradType){
                    case Public.GridType_Player:{
                        playLocation=new Location(x,y);
                        break;
                    }
                    case Public.GridType_RedEnemy:{
                        redEnemyLocations.add(new Location(x,y));
                        break;
                    }
                    default:{
                        AllGrads[y][x]=gradType;
                    }
                }
            }
        }
    }
    //随机生成地图
    void randomInitAllGrads() {
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

}
