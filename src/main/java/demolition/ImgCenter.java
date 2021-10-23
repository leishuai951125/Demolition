package demolition;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.HashMap;
import java.util.Map;

class ImgCenter {
    private HashMap<Integer, PImage> gridTypeToImg = null;
    private final static String parentPath = GameConfig.ProjectRootPath+"src/main/resources/";
//    private final static String parentPath = "/USYD/INFO1113/DemolitionProject/src/main/resources/";
    private Map<Integer,PImage[]> playerAllActionPImages=new HashMap<>(4);
    private Map<Integer,PImage[]> redEnemyAllActionPImages=new HashMap<>(4);
    private Map<Integer,PImage[]> yellowEnemyAllActionPImages=new HashMap<>(4);
    private PImage playerLifeImg;
    private PImage clockImg;

    ImgCenter(PApplet imgLoader) {
        loadImg(imgLoader);
    }

    private void loadImg(PApplet imgLoader) {
        gridTypeToImg = new HashMap<Integer, PImage>(10);
        gridTypeToImg.put(Public.GridType_Empty, imgLoader.loadImage(parentPath + "empty/empty.png"));
        gridTypeToImg.put(Public.GridType_Stone, imgLoader.loadImage(parentPath + "wall/solid.png"));
        gridTypeToImg.put(Public.GridType_Brick, imgLoader.loadImage(parentPath + "broken/broken.png"));
        gridTypeToImg.put(Public.GridType_Destination, imgLoader.loadImage(parentPath + "goal/goal.png"));
        gridTypeToImg.put(Public.GridType_Player, imgLoader.loadImage(parentPath + "player/player.gif"));
        gridTypeToImg.put(Public.GridType_RedEnemy, imgLoader.loadImage(parentPath + "red_enemy/red_down1.png"));
        gridTypeToImg.put(Public.GridType_YellowEnemy, imgLoader.loadImage(parentPath + "yellow_enemy/yellow_down1.png"));
        gridTypeToImg.put(Public.GridType_Bomb, imgLoader.loadImage(parentPath + "bomb/bomb.png"));
        gridTypeToImg.put(Public.GridType_BombExploded, imgLoader.loadImage(parentPath + "explosion/centre.png"));

        //加载玩家的动画
        playerAllActionPImages=loadMotionImgUrls(imgLoader,new String[]{"_up","","_left","_right"},"player/player%s%d.png");
        redEnemyAllActionPImages=loadMotionImgUrls(imgLoader,new String[]{"_up","_down","_left","_right"},"red_enemy/red%s%d.png");
        yellowEnemyAllActionPImages=loadMotionImgUrls(imgLoader,new String[]{"_up","_down","_left","_right"},"yellow_enemy/yellow%s%d.png");
        //title 上的动画
        playerLifeImg=imgLoader.loadImage(parentPath + "icons/player.png");
        clockImg=imgLoader.loadImage(parentPath + "icons/clock.png");
    }
    private Map<Integer,PImage[]> loadMotionImgUrls(PApplet imgLoader,String[] directionNames,String format){
        Map<Integer,PImage[]> allActionPImages=new HashMap<>(4);
        //加载动画
        for(int i=0;i<Public.DirectionKeyCodes.length;i++){
            String directionName=directionNames[i];
            PImage[] temp=new PImage[4];
            for(int j=0;j<4;j++){
                String path=String.format(parentPath + format,directionName,j+1);
                PImage img=imgLoader.loadImage(path);
                if(img!=null){
                    temp[j]=img;
                }else {
                    System.out.println(path+"load img fail");
                }
            }
            allActionPImages.put(Public.DirectionKeyCodes[i],temp);
        }
        return allActionPImages;
    }

    PImage getImgByGridType(int GridType) {
        return gridTypeToImg.get(GridType);
    }
    Map<Integer,PImage[]> getPlayerMotionImgs(){
        return playerAllActionPImages;
    }
    Map<Integer,PImage[]> getRedEnemyMotionImgs(){
        return redEnemyAllActionPImages;
    }
    Map<Integer,PImage[]> getYellowEnemyMotionImgs(){
        return yellowEnemyAllActionPImages;
    }
    PImage getPlayerLifeImg(){
        return playerLifeImg;
    }
    PImage getClockImg(){
        return clockImg;
    }
}
