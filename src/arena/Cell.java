package arena;

public class Cell {
    public final int x, y, cellX, cellY, cellSize;
    private boolean isObstacle, isVirtualWall, isExplored;

    public Cell(int row, int col){
        this.x = col;
        this.y = row;
        this.cellSize = CellConstants.CELL_SIZE - (CellConstants.BORDER*2);
        this.cellX = col * CellConstants.CELL_SIZE +
                CellConstants.BORDER + ArenaConstants.X_OFFSET;
        this.cellY = ArenaConstants.HEIGHT -
                (row*CellConstants.CELL_SIZE - CellConstants.BORDER);
    }

    public int getX(){ return this.x; }
    public int getY(){ return this.y; }
    public int getCellSize(){ return this.cellSize; }
    public boolean getIsObstacle(){ return this.isObstacle; }
    public boolean getIsExplored(){ return this.isExplored; }
    public boolean getIsVirualWall(){ return this.isVirtualWall; }

    public void setIsObstacle(boolean value){ this.isObstacle = value; }
    public void setIsExplored(boolean value){ this.isExplored = value; }
    public void setVirtualWall(boolean value){
        if (value)
            this.isVirtualWall = true;
        else if(this.y != 1 && this.y != ArenaConstants.ROWS &&
                this.x != 1 && this.x != ArenaConstants.COLS)
            this.isVirtualWall = false;
    }

}
