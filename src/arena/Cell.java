package arena;

public class Cell {
    private final int x, y, cellX, cellY, cellSize;
    private boolean isObstacle, isVirtualWall, isExplored, isWayPoint, isFastestPath;

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
    public boolean getIsWayPoint(){ return this.isWayPoint; }
    public boolean getIsFastestPath(){ return this.isFastestPath; }

    public void setIsObstacle(boolean value){ this.isObstacle = value; }
    public void setIsExplored(boolean value){ this.isExplored = value; }
    public void setIsVirtualWall(boolean value){
        if (value)
            this.isVirtualWall = true;
        else if(this.y != 1 && this.y != ArenaConstants.ROWS &&
                this.x != 1 && this.x != ArenaConstants.COLS)
            this.isVirtualWall = false;
    }
    public void setIsWayPoint(boolean value){ this.isWayPoint = value; }
    public void setIsFastestPath(boolean value){ this.isFastestPath = value;}

    public int posX(){ return this.x; }
    public int posY(){ return this.y; }
    public int cellX(){ return this.cellX; }
    public int cellY(){ return this.cellY; }
    public int cellSize(){ return this.cellSize; }
}
