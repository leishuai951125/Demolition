package demolition;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class Human {
    Location location;//坐标
    Location locationPxOffset=new Location(0,0);//坐标像素偏移量
//  PImage[] 运动的图片,a,b,c,d。 静止时在第一张
    private Map<Integer, PImage[]> allActionPImages;
    private int nowDirect; //当前的方向
    private volatile int motionImgIndex=0;//运动图片帧
    private int motionCostMs=300;//一次运动持续时间
    boolean isMotion=false;//是否在运动中
    Human(Location location,Map<Integer, PImage[]> allActionPImages){
        this.location=location;
        nowDirect=Public.DirectionKeyCode_Down;//初始化朝下
        this.allActionPImages =allActionPImages;
    }
    private PImage getImg(){
        PImage [] motionImgs=allActionPImages.get(nowDirect);
        if(!isMotion){ //静止
            return motionImgs[0];
        }else {
            return motionImgs[motionImgIndex];
        }
    }

    void doOnceMove(Location newLocation,int directionKeyCode){
        nowDirect=directionKeyCode;
        final int motionFPS=4;
        int periodMs=motionCostMs/ motionFPS;
        int oneFrame_x_offset_=(newLocation.x-location.x)*Public.GridWith/motionFPS;
        int oneFrame_y_offset_=(newLocation.y-location.y)*Public.GridWith/motionFPS;
        //运动开始
        isMotion=true; //运动开始
        motionImgIndex=0;
        for(int i = 1; i<= motionFPS; i++){
            Public.scheduledExecutorService.schedule(()->{
                motionImgIndex=(motionImgIndex+1)%(motionFPS); //帧的更换
                locationPxOffset.x=oneFrame_x_offset_*motionImgIndex;
                locationPxOffset.y=oneFrame_y_offset_*motionImgIndex;
                if(motionImgIndex==0){
//                    System.out.println("一次运动结束");
                    isMotion=false;
                    location=newLocation;
                }
            },periodMs*i, TimeUnit.MILLISECONDS);
        }
    }

    void draw(PApplet canvas){
        //绘制玩家
        PImage img = getImg();
        Location pxLocation=getPxLocation();
        canvas.image(img, pxLocation.x, pxLocation.y - Public.HumanOverstep, Public.GridWith, Public.GridWith + Public.HumanOverstep);
    }
    Location getPxLocation(){
        int x = location.x * Public.GridWith+locationPxOffset.x;
        int y = location.y * Public.GridWith + Public.TopSpaceHeightPx+locationPxOffset.y;
        return new Location(x,y);
    }

    void releaseBomb(Playgroud playgroud) {
        int[][] AllGrads = playgroud.AllGrads;
        int y = location.y;
        int x = location.x;
        //放置炸弹
        AllGrads[y][x] = Public.GridType_Bomb;
        //-------------------------
        //计算爆炸影响范围
        List<Location> bombExplodedCover = getBombExplodedCover(location,AllGrads);
        //设置爆炸定时器
        Public.scheduledExecutorService.schedule(() -> { //两秒后爆炸
            for (Location coverLocation : bombExplodedCover) {
                if (location.equals(coverLocation)) { //炸到玩家，玩家死亡
                    //todo 死亡逻辑
                }
//                Iterator<Enemy> iterator = playgroud.redEnemys.iterator();
//                while (iterator.hasNext()) {
//                    Enemy enemy = iterator.next();
//                     if (enemy.location.equals(coverLocation)) {
//                        System.out.println("敌军死亡");
//                        iterator.remove(); //敌军死亡
//                    }
//                }
                AllGrads[coverLocation.y][coverLocation.x] = Public.GridType_BombExploded;
            }
            Public.scheduledExecutorService.schedule(() -> {//一秒后爆炸效果清空
                for (Location location : bombExplodedCover) {
                    if(AllGrads[location.y][location.x]!=Public.GridType_Bomb){
                        AllGrads[location.y][location.x] = Public.GridType_Empty;
                    }
                }
            }, 1, TimeUnit.SECONDS);
        }, 2, TimeUnit.SECONDS);
    }
    List<Location> getBombExplodedCover(Location bombLocation,int [][]AllGrads) {
        int y = bombLocation.y;
        int x = bombLocation.x;
        //计算爆炸范围
        List<Location> bombExplodedCover = new ArrayList<>(5); //爆炸范围
        bombExplodedCover.add(new Location(x, y));
        if (AllGrads[y - 1][x] != Public.GridType_Stone) {//上面不是石头
            bombExplodedCover.add(new Location(x, y - 1));
        }
        if (AllGrads[y + 1][x] != Public.GridType_Stone) {//下面不是石头
            bombExplodedCover.add(new Location(x, y + 1));
        }
        if (AllGrads[y][x - 1] != Public.GridType_Stone) {//左边不是石头
            bombExplodedCover.add(new Location(x - 1, y));
        }
        if (AllGrads[y][x + 1] != Public.GridType_Stone) {//右边不是石头
            bombExplodedCover.add(new Location(x + 1, y));
        }
        return bombExplodedCover;
    }
}

class Player extends Human{
    Player(Location location){
        super(location,Public.imgCenter.getPlayerMotionImgs());
    }
}

class Enemy extends Human{
    Enemy(Location location){
        super(location,Public.imgCenter.getRedEnemyMotionImgs());
    }
}


