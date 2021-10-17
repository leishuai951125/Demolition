package demolition;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.HashMap;
import java.util.Map;

class ImgCenter {
    private HashMap<Integer, PImage> gridTypeToImg = null;
    private PImage []playerMotionImgUrls=null;
    private Map<Integer,PImage[]> playerAllActionPImages=new HashMap<>(4);

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
        gridTypeToImg.put(Public.GridType_BombExploded, imgLoader.loadImage(parentPath + "explosion/centre.png"));
        //加载玩家的动画
        String []directionNames=new String[]{"_up","","_left","_right"};
        for(int i=0;i<Public.DirectionKeyCodes.length;i++){
            String directionName=directionNames[i];
            PImage[] temp=new PImage[4];
            for(int j=0;j<4;j++){
                String path=String.format(parentPath + "player/player%s%d.png",directionName,j+1);
                PImage img=imgLoader.loadImage(path);
                if(img!=null){
                    temp[j]=img;
                }else {
                    System.out.println(path+"load img fail");
                }
            }
            playerAllActionPImages.put(Public.DirectionKeyCodes[i],temp);
        }
    }

    PImage getImgByGridType(int GridType) {
        return gridTypeToImg.get(GridType);
    }
    Map<Integer,PImage[]> getPlayerMotionImgs(){
        return playerAllActionPImages;
    }
}
