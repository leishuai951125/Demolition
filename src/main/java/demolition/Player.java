package demolition;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.Map;
import java.util.concurrent.TimeUnit;

class Player {
    Location location;//坐标
    Location locationPxOffset=new Location(0,0);//坐标像素偏移量
//  PImage[] 运动的图片,a,b,c,d。 静止时在第一张
    private Map<Integer, PImage[]> allActionPImages;
    private int nowDirect; //当前的方向
    private volatile int motionImgIndex=0;//运动图片帧
    private int motionCostMs=300;//一次运动持续时间
    boolean isMotion=false;//是否在运动中
    Player(Location location){
        this.location=location;
        nowDirect=Public.DirectionKeyCode_Down;//初始化朝下
        allActionPImages =Public.imgCenter.getPlayerMotionImgs();
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
                    //运动结束
                    System.out.println("一次运动结束");
                    isMotion=false;
                    location=newLocation;
                }
            },periodMs*i, TimeUnit.MILLISECONDS);
        }
    }

    void draw(PApplet canvas){
        //绘制玩家
        PImage img = getImg();
        int x = location.x * Public.GridWith+locationPxOffset.x;
        int y = location.y * Public.GridWith + Public.TopSpaceHeightPx+locationPxOffset.y;
        canvas.image(img, x, y - Public.HumanOverstep, Public.GridWith, Public.GridWith + Public.HumanOverstep);
    }
}
