package demolition;

class Location {
    int x, y; //x 水平方向坐标,y 竖直方向坐标

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Location clone() {
        return new Location(x, y);
    }

    public boolean equals(Location o) {
        return this.x == o.x && this.y == o.y;
    }
}
