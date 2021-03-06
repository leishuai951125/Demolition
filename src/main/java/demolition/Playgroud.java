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
    YEnemy yEnemy = null;
    private int playLife =5;
    volatile int countdown = 180; //倒计时，单位秒
    //    ArrayList<REnemy> redEnemys = new ArrayList<REnemy>();
    List<REnemy> redEnemys = new CopyOnWriteArrayList<REnemy>();
    Playgroud() {
        initAllGradsByConfigFile();
        startLoop();
    }
    void startLoop() {
        //敌军行动
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            moveAllRedEnemy();
            moveYellowEnemy();
//            bombCheck();
        }, 0, 100, TimeUnit.MILLISECONDS);
        //倒计时
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            countdown--;
        }, 0, 1000, TimeUnit.MILLISECONDS);
        //敌军自动扔炸弹
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            enemyAutoReleaseBomb();
        }, 0, 3300, TimeUnit.MILLISECONDS);
        Public.scheduledExecutorService.scheduleAtFixedRate(this::enemyAutoReleaseBomb, 0, 3300, TimeUnit.MILLISECONDS);
    }

    private void enemyAutoReleaseBomb() {
        int randomIndex= Public.random.nextInt(redEnemys.size());
        redEnemys.get(randomIndex).releaseBomb(this);
        yEnemy.releaseBomb(this);
    }

    private void moveAllRedEnemy() {
        for(Human redEnemy:redEnemys){
//            int rand = Public.random.nextInt(4); //生成0到4随机数
//            int randomDirectionKeyCode = Public.DirectionKeyCodes[rand];
//            //移动敌军
//            moveHuman(redEnemy, randomDirectionKeyCode);
            moveOneRedEnemy(redEnemy);
        }
    }

    private void moveOneRedEnemy(Human redEnemy) {
        if(redEnemy.isMotion){
            return;
        }
        //查上一步的方向
        //尝试走上一步的方向（判断原方向是否可以继续走）

        //如果可以，则走原方向。 ---直线运动

        //如果不可以，则随机找一个可以走的新方向，找到后，朝新方向走一步。 ---转弯

        int lastDirection=redEnemy.nowDirect; //查原方向
        Location newlocation = getNewLocation(redEnemy.location, lastDirection);//计算原方向是否可以走
        if(!newlocation.equals(redEnemy.location)){ //如果可以走
            //沿着原方向做一次运动
            redEnemy.doOnceMove(newlocation, lastDirection);
        }else { //如果不能走
            while (true){
                int randInx=Public.random.nextInt(4);//0到4的随机数
                int tryNextDirect=Public.directsCycle[randInx];
                newlocation = getNewLocation(redEnemy.location, tryNextDirect);
                if(!newlocation.equals(redEnemy.location)){ //能走
                    redEnemy.doOnceMove(newlocation, tryNextDirect);
                    break;
                }
            }
        }
    }

    private void moveYellowEnemy(){
        if(yEnemy==null){
            return;
        }
        if(yEnemy.isMotion){
            return;
        }
        //查上一步的方向
        //尝试走上一步的方向（判断原方向是否可以继续走）

        //如果可以，则走原方向。 ---直线运动

        //如果不可以，则按顺时针找一个可以走的新方向，找到后，朝新方向走一步。 ---转弯

        int lastDirection=yEnemy.nowDirect; //查原方向
        Location newlocation = getNewLocation(yEnemy.location, lastDirection);//计算原方向是否可以走
        if(!newlocation.equals(yEnemy.location)){ //如果可以走
            //沿着原方向做一次运动
            yEnemy.doOnceMove(newlocation, lastDirection);
        }else { //如果不能走
            int nowIndex=-1;
            for(int i=0;i<4;i++){
                if(lastDirection==Public.directsCycle[i]){
                    nowIndex=i;
                    break;
                }
            }

            for(int i=1;i<4;i++){
                int nextDirectIndex=(nowIndex+i)%4;
                int tryNextDirect=Public.directsCycle[nextDirectIndex];
                newlocation = getNewLocation(yEnemy.location, tryNextDirect);
                if(!newlocation.equals(yEnemy.location)){ //能走
                    yEnemy.doOnceMove(newlocation, tryNextDirect);
                    break;
                }
            }
        }
    }

    //给玩家用的
    void moveHuman(Human human, int directionKeyCode) {
        if (human.isMotion) { //运动中
            //不做任何事
            return;
        }
        Location newlocation = getNewLocation(human.location, directionKeyCode);
        if (!newlocation.equals(human.location)) {//位置有变化
            //做一次移动
            human.doOnceMove(newlocation, directionKeyCode);
        }
    }


    //炸弹效果碰撞检测(复杂的碰撞检测)
    private void bombCheck() {
//        System.out.println("碰撞检测start");
        for(int y=0;y<y_count;y++){
            for(int x=0;x<x_count;x++){
                int gradType=AllGrads[y][x];
                if(gradType== Public.GridType_BombExploded){ //此处是炸弹
//                    Iterator<REnemy> iterator=redEnemys.iterator();
//                    while (iterator.hasNext()){
//                        Human human=iterator.next();
//                        //爆炸范围矩形
//                        if (checkBombIsCoverEnemy(new Location(x,y),human)){
//                            redEnemys.remove(human);
//                        }
//                    }
                    if(yEnemy!=null&&checkBombIsCoverEnemy(new Location(x,y),yEnemy)){
                        yEnemy=null; //死掉
                    }
                }
            }
        }
//        System.out.println("碰撞检测end");
    }

    //判断炸弹是否炸到了敌人
    boolean checkBombIsCoverEnemy(Location bombLocation,Human human) {
        Location leftTopPxLocation = getGradPxLocation(bombLocation.x, bombLocation.y);
        Location rightBottomLocation = leftTopPxLocation.clone();
        rightBottomLocation.x += Public.GridWidth;
        rightBottomLocation.y += Public.GridWidth;
        Rectangle coverRectangle = new Rectangle(leftTopPxLocation, rightBottomLocation);
        //人的矩形
        Location humanPxLocation = human.getPxLocation();
        Location humanRightBottomLocation = humanPxLocation.clone();
        humanRightBottomLocation.x += Public.GridWidth;
        humanRightBottomLocation.y += Public.GridWidth;
        Rectangle humanRectangle = new Rectangle(humanPxLocation, humanRightBottomLocation);
        return humanRectangle.isOverlap(coverRectangle); //能被炸到
    }

    //根据方向和可达性，判断一个位置是否可以到达，是则返回新位置，否则范围旧位置
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


    void movePlayer(int directionKeyCode) {
        moveHuman(player, directionKeyCode);
    }

    void drawTitle(PApplet canvas){
        canvas.textSize(Public.GridWidth *2/3);
        canvas.color(Public.GridWidth *2/3);
        int textTopPx=Public.GridWidth *2/3;
        int imgTopPx=Public.GridWidth /2;
        //生命
        int playerLifeImgLeftPx=Public.GridWidth *4;
        canvas.image(Public.imgCenter.getPlayerLifeImg(),playerLifeImgLeftPx,imgTopPx,Public.GridWidth,Public.GridWidth);
        int playerLifeTextLeftPx=Public.GridWidth *5+Public.GridWidth /3;
        canvas.text("x "+Integer.toString(playLife),playerLifeTextLeftPx,textTopPx,Public.GridWidth *4,Public.GridWidth *3);
        //时间
        int clockImgLeftPx=Public.GridWidth *8;
        int clockTextLeftPx=Public.GridWidth *9+Public.GridWidth /3;
        canvas.image(Public.imgCenter.getClockImg(),clockImgLeftPx,imgTopPx,Public.GridWidth,Public.GridWidth);
        canvas.text(Integer.toString(countdown),clockTextLeftPx,textTopPx,Public.GridWidth *5,Public.GridWidth);
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
        for (REnemy redEnemy : redEnemys) {
            redEnemy.draw(canvas);
        }

        //绘制玩家
        player.draw(canvas);
        if(yEnemy!=null){ //活着（有黄色敌军）
            yEnemy.draw(canvas);
        }
    }

    Location getGradPxLocation(int x,int y){
        return new Location(x * Public.GridWidth,y * Public.GridWidth + Public.TopSpaceHeightPx);
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
                    canvas.image(emptyImg, x, y, Public.GridWidth, Public.GridWidth);
                }
                canvas.image(img, x, y, Public.GridWidth, Public.GridWidth);
            }
        }
    }

    //按配置文件生成地图
    void initAllGradsByConfigFile() {
        String parentPath = "/Users/leishuai/Downloads/Demolition/";
//        String parentPath = "/USYD/INFO1113/DemolitionProject/";
        String level1MapFilePath = parentPath + "level1.txt";
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
                put('Y', Public.GridType_YellowEnemy); //敌军
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
                        redEnemys.add(new REnemy(new Location(x, y)));
                        break;
                    }
                    case Public.GridType_YellowEnemy: {
                        yEnemy=new YEnemy(new Location(x, y));
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
