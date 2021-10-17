package demolition;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

//公共基建或者配置
class Public {
    public static ImgCenter imgCenter = null;
    public static final int x_count = 15;  //水平格子数
    public static final int y_count = 13;  //锤直格子数
    public static int GridWith = App.WIDTH/x_count;  //480/15=32
    final static int TopSpaceHeightPx = Public.GridWith * 2;
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
    static final int GridType_BombExploded = -2; //爆炸的炸弹
    // Direction 方向常量定义
    static final int DirectionKeyCode_Up = java.awt.event.KeyEvent.VK_UP;
    static final int DirectionKeyCode_Down = java.awt.event.KeyEvent.VK_DOWN;
    static final int DirectionKeyCode_Left = java.awt.event.KeyEvent.VK_LEFT;
    static final int DirectionKeyCode_Right = java.awt.event.KeyEvent.VK_RIGHT;
    static final int[] DirectionKeyCodes = new int[]{DirectionKeyCode_Up, DirectionKeyCode_Down, DirectionKeyCode_Left, DirectionKeyCode_Right};
    //
    static final int KeyCode_ReleaseBomb = java.awt.event.KeyEvent.VK_SPACE;

    //是否为方向键取值，是方向键返回true，不是返回false
    static final boolean isDirectionKeyCode(int keyCode) {
        for (int i = 0; i < DirectionKeyCodes.length; i++) {
            if (keyCode == DirectionKeyCodes[i]) {
                return true;
            }
        }
        return false;
    }
    //随机数生成器
    public static Random random=new Random();
    //定时任务执行器
    static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//    //按一定次数间隔执行
//    public static void RepartRunWithLimt(Runnable command,long periodMs,int runTimesLimit,Runnable finishedTask){
//        //以播放动画为例
//        //如果动画有 4 张图（a,b,c,d），静止时在第一张 a
//        //动起来: 开始 a 1s b 1s c 1s d 1s 取消动画/重复 ，共需要执行四次切换：切a，切b，切c，切d
//        //需要切换三次：静态 1s 切 1s 切 1s 切 1s 还原
//        command.run(); //第0秒执行 a
//        for(int i=1;i<runTimesLimit;i++){ //runTimesLimit=4, //i=1,2,3
//            //第 1，2，3 秒执行 b,c,d
//            scheduledExecutorService.schedule(command,periodMs*(i),TimeUnit.MILLISECONDS);
//        }
//        //第4秒执行取消
//        scheduledExecutorService.schedule(finishedTask,periodMs*(runTimesLimit+1),TimeUnit.MILLISECONDS);
//    }
}
