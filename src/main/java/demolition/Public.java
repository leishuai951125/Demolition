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
    public static int GridWidth = App.WIDTH / x_count;  //480/15=32
    final static int TopSpaceHeightPx = Public.GridWidth * 2;
    public static int HumanOverstep = Public.GridWidth / 2;
    // GridType 常量定义
    static final int GridType_Empty = 0; //
    static final int GridType_Stone = 1; //石头
    static final int GridType_Brick = 2; //砖
    static final int GridType_Destination = 3; //终点
    static final int GridType_Player = 4; //玩家
    static final int GridType_RedEnemy = 5; //红色敌军
    static final int GridType_YellowEnemy = 6; //黄色敌军
    static final int GridType_Bomb = -1; //炸弹
    static final int GridType_BombExploded = -2; //爆炸的炸弹
    // Direction 方向常量定义
    static final int DirectionKeyCode_Up = java.awt.event.KeyEvent.VK_UP;
    static final int DirectionKeyCode_Down = java.awt.event.KeyEvent.VK_DOWN;
    static final int DirectionKeyCode_Left = java.awt.event.KeyEvent.VK_LEFT;
    static final int DirectionKeyCode_Right = java.awt.event.KeyEvent.VK_RIGHT;
    static final int[] DirectionKeyCodes = new int[]{DirectionKeyCode_Up, DirectionKeyCode_Down, DirectionKeyCode_Left, DirectionKeyCode_Right};
    static final int[] directsCycle = new int[]{DirectionKeyCode_Down, DirectionKeyCode_Left, DirectionKeyCode_Up, DirectionKeyCode_Right};
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
    public static Random random = new Random();
    //定时任务执行器
//    static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    static scheduledExecutorService scheduledExecutorService = scheduledExecutorServiceImpl.getInstance();
}

interface scheduledExecutorService{
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit);
    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay, TimeUnit unit);

}

class scheduledExecutorServiceImpl implements scheduledExecutorService{
    //最简单的单例模式
    static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private scheduledExecutorServiceImpl(){
    }
    static scheduledExecutorServiceImpl getInstance(){
        return new scheduledExecutorServiceImpl();
    }
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit){
        return scheduledExecutorService.scheduleAtFixedRate(()->{
            try {
                command.run();
            }catch (Exception e){
                e.printStackTrace();
            }
        },initialDelay,period,unit);
    }
    @Override
    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay, TimeUnit unit){
       return scheduledExecutorService.schedule(()->{
            try {
                command.run();
            }catch (Exception e){
                e.printStackTrace();
            }
        },delay,unit);
    }
}

class scheduledExecutorServiceImplV2 implements scheduledExecutorService{
    //最简单的单例模式
    private scheduledExecutorServiceImplV2() {
    }
    static scheduledExecutorServiceImplV2 getInstance() {
        return new scheduledExecutorServiceImplV2();
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        new Thread(() -> {
            try {
                long sleepMs = period;
                if (unit == TimeUnit.SECONDS) {
                    sleepMs *= 1000;
                }
                while (true){
                    long start=System.currentTimeMillis();
                    command.run();
                    long cost=System.currentTimeMillis()-start;
                    if(sleepMs-cost>0){
                        Thread.sleep(sleepMs-cost);
                    }else {
                        System.out.printf("cost time so much,sleepMs is %d,cost is %d\n",
                                sleepMs,cost);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return null;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay, TimeUnit unit) {
        new Thread(() -> {
            try {
                long sleepMs = delay;
                if (unit == TimeUnit.SECONDS) {
                    sleepMs *= 1000;
                }
                Thread.sleep(sleepMs);
                command.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return null;
    }
}