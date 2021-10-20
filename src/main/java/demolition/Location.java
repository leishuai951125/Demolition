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

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

class Rectangle { //矩形
    Location leftTopLocation,rightBottomLocation ;
    Rectangle(Location leftTopLocation,Location rightBottomLocation){
        this.leftTopLocation=leftTopLocation;
        this.rightBottomLocation=rightBottomLocation;
    }

    //是否有重叠
    boolean isOverlap(Rectangle rectangle){
        if(equals(rectangle)){ //完全重叠
            return true;
        }
        int width=rectangle.rightBottomLocation.x-rectangle.leftTopLocation.x;
        int height=rectangle.rightBottomLocation.y-rectangle.leftTopLocation.y;
        //左右方向两个矩形有重叠
        if(this.leftTopLocation.x>rectangle.leftTopLocation.x
                && this.leftTopLocation.x<rectangle.leftTopLocation.x+width
                && this.leftTopLocation.y==rectangle.leftTopLocation.y){
            return true;
        }
        //垂直方向两个矩形有重叠

        if(this.leftTopLocation.y>rectangle.leftTopLocation.y
                && this.leftTopLocation.y<rectangle.leftTopLocation.y+height
                && this.leftTopLocation.x==rectangle.leftTopLocation.x){
            return true;
        }

        return false;
    }
    boolean equals(Rectangle rectangle){
        return this.leftTopLocation.equals(rectangle.leftTopLocation) &&
                this.rightBottomLocation.equals(rectangle.rightBottomLocation);
    }
}
