package arena;

public class Cell {
    public final int x, y, cellX, cellY, cellSize;
    private boolean isObstacle, isVirtualWall, isExplored, isMovedOver;

    public Cell(int row, int col){
        this.x = col;
        this.y = row;
        this.cellSize = CellConstants.CELL_SIZE - (CellConstants.BORDER*2);
        this.cellX = col * CellConstants.CELL_SIZE +
                CellConstants.BORDER + ArenaConstants.X_OFFSET;
        this.cellY = ArenaConstants.HEIGHT -
                (row*CellConstants.CELL_SIZE - CellConstants.BORDER);
    }
    public boolean getIsObstacle(){ return this.isObstacle; }
    public boolean getIsExplored(){ return this.isExplored; }
    public boolean getIsVirtualWall(){ return this.isVirtualWall; }
    public boolean getIsMovedOver(){ return this.isMovedOver; }

    public void setIsObstacle(boolean value){ this.isObstacle = value; }
    public void setIsExplored(boolean value){ this.isExplored = value; }
    public void setVirtualWall(boolean value){
        if (value)
            this.isVirtualWall = true;
        else if(this.y != 1 && this.y != ArenaConstants.ROWS &&
                this.x != 1 && this.x != ArenaConstants.COLS)
            this.isVirtualWall = false;
    }
    public void setIsMovedOver(boolean value){ this.isMovedOver = value; }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
