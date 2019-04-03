package arena;

import robot.RbtConstants;
import robot.Robot;

import javax.swing.*;
import java.awt.*;

public class Arena extends JPanel {
    public final Cell[][] grid;
    private final Robot robot;

    public Arena(Robot robot){
        this.robot = robot;
        this.grid = new Cell[ArenaConstants.ROWS][ArenaConstants.COLS];

        for(short row = 0; row < grid.length; row++) {
            for (short col = 0; col < grid[0].length; col++) {
                grid[row][col] = new Cell(row + 1, col + 1);

                if (row == 0 || col == 0 ||
                    row == ArenaConstants.ROWS - 1 ||
                    col == ArenaConstants.COLS - 1) {
                    grid[row][col].setIsVirtualWall(true);
                }
            }
        }
    }

    public void paintComponent(Graphics g){
        for (Cell row[] : grid){
            for (Cell cell : row){
                Color cellColor;
                if (inStartZone(cell.posX(), cell.posY())) cellColor = Color.GREEN;
                else if (inGoalZone(cell.posX(), cell.posY())) cellColor = Color.RED;
                else if (!cell.getIsExplored()) cellColor = Color.LIGHT_GRAY;
                else if (cell.getIsObstacle()) cellColor = Color.BLACK;
//                else if (cell.getIsVirtualWall()) cellColor = Color.YELLOW;
                else if (cell.getIsFastestPath()) cellColor = Color.MAGENTA;
                else if (cell.getIsWayPoint()) cellColor = Color.CYAN;
                else cellColor = Color.WHITE;

                g.setColor(cellColor);
                g.fillRect(cell.cellX(), cell.cellY(), cell.cellSize(), cell.cellSize());
            }
        }
        //ROBOT
        g.setColor(Color.BLUE);
        int x = robot.getPosX();
        int y = robot.getPosY();
        g.fillOval(
            (x - 1) * CellConstants.CELL_SIZE + ArenaConstants.X_OFFSET + RbtConstants.X_OFFSET,
            ArenaConstants.HEIGHT - (y * CellConstants.CELL_SIZE + RbtConstants.Y_OFFSET),
            RbtConstants.WIDTH, RbtConstants.HEIGHT);
        //ROBOT's direction
        g.setColor(Color.WHITE);
        g.fillOval(
                robot.getFrontX() * CellConstants.CELL_SIZE + RbtConstants.FRONT_X_OFFSET,
                ArenaConstants.HEIGHT - (robot.getFrontY() * CellConstants.CELL_SIZE - RbtConstants.FRONT_Y_OFFSET),
                RbtConstants.FRONT_SIZE, RbtConstants.FRONT_SIZE);
    }

    private boolean inStartZone(int x, int y){
        return x > 0 && y > 0 &&
            x <= ArenaConstants.START_X + 1 && y <= ArenaConstants.START_Y + 1;
    }

    private boolean inGoalZone(int x, int y){
        return x >= ArenaConstants.GOAL_X - 1 && y >= ArenaConstants.GOAL_Y - 1 &&
            x <= ArenaConstants.COLS && y <= ArenaConstants.ROWS;
    }

    public void setObstacle(int x, int y, boolean obstacle){
        System.out.println("setObstacle: " + x + "," + y + " " + obstacle);
        if (obstacle && inStartZone(x, y) || inGoalZone(x, y)) return;
        grid[y-1][x-1].setIsObstacle(obstacle);

        for(int i = -1; i<=1; ++i){
            for(int j = -1; j<=1; ++j){
                System.out.println(""+(x+i)+","+(y+j));
                if(i==0&&j==0) continue;
                if(x+i<1 || x+i>15 || y+j<1 || y+j>20) continue;
                if(obstacle){
                    grid[y+j-1][x+i-1].setIsVirtualWall(obstacle);
                }
                else{
                    if(!hasBlockAround(x, y)) grid[y+i-1][x+j-1].setIsVirtualWall(obstacle);
                }

            }
        }
    }

    private boolean hasBlockAround(int x, int y){
        for(int i = -1; i<=1; i+=1){
            for(int j = -1; j<=1; j+=1){
                if(i==0&&j==0) continue;
                if(x+i<1 || x+i>15 || y+j<1 || y+j>20) continue;
                if(grid[y+j-1][x+i-1].getIsObstacle()){
                    return true;
                }
            }
        }
        return false;
    }

    public void clearArena(){
        for (Cell row[] : this.grid){
            for (Cell cell : row){
                cell.setIsObstacle(false);
                if (inStartZone(cell.posX(), cell.posY()) || inGoalZone(cell.posX(), cell.posY())) {
                    cell.setIsExplored(true);
                }
                else {
                    cell.setIsObstacle(false);
                    cell.setIsExplored(false);
                    cell.setIsVirtualWall(false);
                }
                cell.setIsWayPoint(false);
            }
        }
    }

    public boolean checkValidCoord(int posX, int posY) {
        return posY > 0 && posX > 0 && posY <= ArenaConstants.ROWS && posX <= ArenaConstants.COLS;
    }

    public boolean checkWayPointCoord(int posX, int posY){
        return checkValidCoord(posX, posY) && !getCell(posX, posY).getIsVirtualWall() && !getCell(posX, posY).getIsObstacle();
    }

    public Cell getCell(int posX, int posY){

        return grid[posY - 1][posX - 1];
    }

    public void setAllExplored(){
        for (Cell row[] : grid) {
            for (Cell cell : row) {
                cell.setIsExplored(true);
            }
        }
    }

}
