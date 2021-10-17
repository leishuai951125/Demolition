package demolition;

//目前没地方用到
class Bomb {
    int status=BombStatus_Init; // 1 初始化 2 爆炸中 3 已失效
    final static int BombStatus_Init = 1;
    final static int BombStatus_Boom = 2;
    final static int BombStatus_Invalid = 3;
    Location location;
    Bomb(Location location){
        this.location=location;
    }
}
