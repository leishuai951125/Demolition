package demolition;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.HashMap;

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
        gridTypeToImg.put(Public.GridType_BombExploded, imgLoader.loadImage(parentPath + "explosion/centre.png"));
    }

    PImage getImgByGridType(int GridType) {
        return gridTypeToImg.get(GridType);
    }
}
