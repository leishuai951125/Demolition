package demolition;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

interface GameConfig {
    int AppPxWidth =720; //屏幕宽度
    int FPS=20;
    int PlayerLife=2; //玩家生命
    int MoveOnceCostMs=300; //移动一次的耗时（单位毫秒）
    int CountDown=200; //倒计时
    String ProjectResourcesPath =Public.getProjectResourcesPath();
//    String ProjectResourcesPath= "/USYD/INFO1113/DemolitionProject/";
}

class Playgroud {
    int[][] AllGrads = null; //value 是 GradType,前面是y，后面是x
    final int x_count = Public.x_count;  //水平格子数
    final int y_count = Public.y_count;  //锤直格子数
    Player player ; //玩家
    private int nowLevel ;
    private int maxLevel ;
    private int gameStatus ;
    volatile int countdown ; //倒计时，单位秒
    Map<Human, String> redEnemys = null;
    Map<Human, String> yEnemys = null;
    Location destinationLocation = null;

    Playgroud() {
        player=new Player(null);
        gameStatus = Public.GameStatus_Playing;
//        gameStatus=Public.GameStatus_PassAll;
        nowLevel = 1;
        maxLevel = 2;
        reset();
        loadAllGradsByConfigFile();
        startLoop();
    }

    //重置
    void reset() {
        AllGrads = new int[y_count][x_count];
        redEnemys = new ConcurrentHashMap<>();
        yEnemys = new ConcurrentHashMap<>();
        countdown = GameConfig.CountDown+1;
    }

    void playerDie() { //玩家死亡
        player.life--;
        if (player.life==0 ) { //游戏终结
            gameStatus = Public.GameStatus_Fail;
        }else{
            restartGound();
        }
    }

    void checkPassOneLevel(){
        boolean isArriveDest=player != null && player.location.equals(destinationLocation);
        if (isArriveDest) { //玩家是否为终点
            if (nowLevel == maxLevel) { //通关
                gameStatus = Public.GameStatus_PassAll;
            } else { //下一关
                startNextGound();
            }
        }
    }

    //重玩同一关
    void restartGound(){
        //重置所有对象的值
        reset();
        //按关卡加载地图
        loadAllGradsByConfigFile();
    }

    //开始下一关
    void startNextGound() {
        //重置所有对象的值
        reset();
        //关卡加1
        nowLevel++;
        //按关卡加载地图
        loadAllGradsByConfigFile();
    }

    //只是打个日志
    void doFunc(Runnable runnable, String name) {
        long start = System.currentTimeMillis();
        runnable.run();
        long cost = System.currentTimeMillis() - start;
        if (cost > 600) {
            System.out.printf("doFunc cost:%d,name:%s\n", cost, name);
        }
    }

    void startLoop() {
        //倒计时
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            if(--countdown==0){
                playerDie();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        //敌军行动
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            doFunc(this::checkPassOneLevel, "checkPass"); //通过检测
            doFunc(this::bombCheck, "bombCheck"); //炸弹效果检测
            doFunc(this::moveAllYEnemy, "moveAllYEnemy");
            doFunc(this::moveAllRedEnemy, "moveAllRedEnemy");
//            bombCheck();
//            moveAllYEnemy();
//            moveAllRedEnemy();
//            enemyAutoReleaseBomb();
        }, 0, 101, TimeUnit.MILLISECONDS);
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            doFunc(() -> {
                enemyAutoReleaseBomb(yEnemys);
            }, "enemyAutoReleaseBomb.yEnemys");
        }, 0, 4150, TimeUnit.MILLISECONDS);
        Public.scheduledExecutorService.scheduleAtFixedRate(() -> {
            doFunc(() -> {
                enemyAutoReleaseBomb(redEnemys);
            }, "enemyAutoReleaseBomb.redEnemys");
        }, 0, 6150, TimeUnit.MILLISECONDS);
    }

    private void enemyAutoReleaseBomb(Map<Human, String> enemys) {
        int size = enemys.size();
        if (size <= 0) {
            return;
        }
        int randomIndex = Public.random.nextInt(size);
        int i = 0;
        for (Map.Entry<Human, String> kv : enemys.entrySet()) {
            if (i == randomIndex) {
                kv.getKey().releaseBomb(this);
                break;
            }
            i++;
        }
    }

    private void moveAllRedEnemy() {
        for (Map.Entry<Human, String> kv : redEnemys.entrySet()) {
            moveOneRedEnemy(kv.getKey());
        }
    }

    private void moveAllYEnemy() {
        for (Map.Entry<Human, String> kv : yEnemys.entrySet()) {
            moveYellowEnemy(kv.getKey());
        }
    }

    private void moveOneRedEnemy(Human redEnemy) {
        if (redEnemy.isMotion) {
            return;
        }
        //查上一步的方向
        //尝试走上一步的方向（判断原方向是否可以继续走）

        //如果可以，则走原方向。 ---直线运动

        //如果不可以，则随机找一个可以走的新方向，找到后，朝新方向走一步。 ---转弯

        int lastDirection = redEnemy.nowDirect; //查原方向
        Location newlocation = getNewLocation(redEnemy.location, lastDirection);//计算原方向是否可以走
        if (!newlocation.equals(redEnemy.location)) { //如果可以走
            //沿着原方向做一次运动
            redEnemy.doOnceMove(newlocation, lastDirection);
        } else { //如果不能走
            int tryTimes = 10;
            for (; tryTimes > 0; tryTimes--) {
                int randInx = Public.random.nextInt(4);//0到4的随机数
                int tryNextDirect = Public.directsCycle[randInx];
                newlocation = getNewLocation(redEnemy.location, tryNextDirect);
                if (!newlocation.equals(redEnemy.location)) { //能走
                    redEnemy.doOnceMove(newlocation, tryNextDirect);
                    break;
                }
            }
            if (tryTimes == 0) {
                cycleChangeDirectMoveOnce(redEnemy);
            }
        }
    }

    private void moveYellowEnemy(Human yEnemy) {
        if (yEnemy == null) {
            return;
        }
        if (yEnemy.isMotion) {
            return;
        }
        //查上一步的方向
        //尝试走上一步的方向（判断原方向是否可以继续走）

        //如果可以，则走原方向。 ---直线运动

        //如果不可以，则按顺时针找一个可以走的新方向，找到后，朝新方向走一步。 ---转弯

        int lastDirection = yEnemy.nowDirect; //查原方向
        Location newlocation = getNewLocation(yEnemy.location, lastDirection);//计算原方向是否可以走
        if (!newlocation.equals(yEnemy.location)) { //如果可以走
            //沿着原方向做一次运动
            yEnemy.doOnceMove(newlocation, lastDirection);
        } else { //如果不能走
            cycleChangeDirectMoveOnce(yEnemy);
        }
    }

    private void cycleChangeDirectMoveOnce(Human yEnemy) {
        int lastDirection = yEnemy.nowDirect; //查原方向
        Location newlocation;
        int nowIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (lastDirection == Public.directsCycle[i]) {
                nowIndex = i;
                break;
            }
        }
        for (int i = 1; i < 4; i++) {
            int nextDirectIndex = (nowIndex + i) % 4;
            int tryNextDirect = Public.directsCycle[nextDirectIndex];
            newlocation = getNewLocation(yEnemy.location, tryNextDirect);
            if (!newlocation.equals(yEnemy.location)) { //能走
                yEnemy.doOnceMove(newlocation, tryNextDirect);
                break;
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


    //炸弹效果碰撞检测
    private void bombCheck() {
//        System.out.println("碰撞检测start");
        for (int y = 0; y < y_count; y++) {
            for (int x = 0; x < x_count; x++) {
                if (AllGrads[y][x] != Public.GridType_BombExploded) {
                    continue;
                }
                Location coverLocation = new Location(x, y);
                if(player.location.equals(coverLocation)){ //玩家被炸了
                    playerDie();
                    return;
                }
                //简单的碰撞检测
                for (Map.Entry<Human, String> kv : redEnemys.entrySet()) {
                    Human enemy = kv.getKey();
                    if (enemy.location.equals(coverLocation)) {
                        System.out.println("red敌军死亡");
                        redEnemys.remove(enemy);
                    }
                }
                for (Map.Entry<Human, String> kv : yEnemys.entrySet()) {
                    Human enemy = kv.getKey();
                    if (enemy.location.equals(coverLocation)) {
                        System.out.println("yello敌军死亡");
                        yEnemys.remove(enemy);
                    }
                }
            }
        }
//        System.out.println("碰撞检测end");
    }

    //判断炸弹是否炸到了敌人，这是一个复杂的碰撞检测
    boolean checkBombIsCoverEnemy(Location bombLocation, Human human) {
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

    void drawTitle(PApplet canvas) {
        canvas.textSize(Public.GridWidth * 2 / 3);
//        canvas.color(Public.GridWidth * 2 / 3);
        int textTopPx = Public.GridWidth * 2 / 3;
        int imgTopPx = Public.GridWidth / 2;
        //生命
        int playerLifeImgLeftPx = Public.GridWidth * 4;
        canvas.image(Public.imgCenter.getPlayerLifeImg(), playerLifeImgLeftPx, imgTopPx, Public.GridWidth, Public.GridWidth);
        int playerLifeTextLeftPx = Public.GridWidth * 5 + Public.GridWidth / 3;
        canvas.text("x " + player.life, playerLifeTextLeftPx, textTopPx, Public.GridWidth * 4, Public.GridWidth * 3);
        //时间
        int clockImgLeftPx = Public.GridWidth * 8;
        int clockTextLeftPx = Public.GridWidth * 9 + Public.GridWidth / 3;
        canvas.image(Public.imgCenter.getClockImg(), clockImgLeftPx, imgTopPx, Public.GridWidth, Public.GridWidth);
        canvas.text(Integer.toString(countdown), clockTextLeftPx, textTopPx, Public.GridWidth * 5, Public.GridWidth);
    }

    void resetBackground(PApplet canvas) {
        canvas.clear();
        int yellow = canvas.color(238, 129, 0);
        canvas.background(yellow);//黄色背景
    }

    void drawEnd(PApplet canvas) {
        canvas.textSize(Public.GridWidth);
        int textTopPx = Public.GridWidth * y_count / 2;
        int LeftPx = Public.GridWidth * 5;
        String contentText = gameStatus == Public.GameStatus_Fail ? "GAME OVER" : "YOU WIN";
        canvas.text(contentText, LeftPx, textTopPx, Public.GridWidth * 7, Public.GridWidth * 3);

    }

    void draw(PApplet canvas) {
        //重置背景
        resetBackground(canvas);
        //结束
        if (gameStatus == Public.GameStatus_Fail || gameStatus == Public.GameStatus_PassAll) {
            drawEnd(canvas);
            return;
        }
        //-----以下是游戏中----
        //标题
        drawTitle(canvas);
        //绘制地图，包括：草地、墙、砖、炸弹
        drawGrads(canvas);
        //绘制敌人
        for (Map.Entry<Human, String> kv : redEnemys.entrySet()) {
            kv.getKey().draw(canvas);
        }
        for (Map.Entry<Human, String> kv : yEnemys.entrySet()) {
            kv.getKey().draw(canvas);
        }
        //绘制玩家
        player.draw(canvas);
    }

    Location getGradPxLocation(int x, int y) {
        return new Location(x * Public.GridWidth, y * Public.GridWidth + Public.TopSpaceHeightPx);
    }

    //绘制地图,包括：草地、墙、砖、炸弹
    private void drawGrads(PApplet canvas) {
        //终点
        PImage destImg = Public.imgCenter.getImgByGridType(Public.GridType_Destination);
        Location destPxLocation = getGradPxLocation(destinationLocation.x, destinationLocation.y);
        canvas.image(destImg, destPxLocation.x, destPxLocation.y, Public.GridWidth, Public.GridWidth);
        //地图
        PImage emptyImg = Public.imgCenter.getImgByGridType(Public.GridType_Empty);
        for (int y_index = 0; y_index < y_count; y_index++) {
            for (int x_index = 0; x_index < x_count; x_index++) {
                int gridType = AllGrads[y_index][x_index];
                if (gridType == Public.GridType_Empty && new Location(x_index, y_index).equals(destinationLocation)) {
                    //空白地图不应该覆盖终点的图标
                    continue;
                }
                //绘制
                PImage img = Public.imgCenter.getImgByGridType(gridType);
                Location pxLocation = getGradPxLocation(x_index, y_index);
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

    //根据关卡从配置文件生成地图
    void loadAllGradsByConfigFile() {
        String level1MapFilePath = GameConfig.ProjectResourcesPath + String.format("level%d.txt", nowLevel);
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
        sc.close();
        for (int y = 0; y < allLine.size(); y++) {
            for (int x = 0; x < allLine.get(y).length(); x++) {
                char c = allLine.get(y).charAt(x);
                int gradType = charToGradType.get(c);
                switch (gradType) {
                    case Public.GridType_Player: {
                        player.location=new Location(x, y);
                        break;
                    }
                    case Public.GridType_RedEnemy: {
                        redEnemys.put(new REnemy(new Location(x, y)), "");
                        break;
                    }
                    case Public.GridType_YellowEnemy: {
                        yEnemys.put(new YEnemy(new Location(x, y)), "");
                        break;
                    }
                    case Public.GridType_Destination: {
                        destinationLocation = new Location(x, y);
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
