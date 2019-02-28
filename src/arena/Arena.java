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
                    grid[row][col].setVirtualWall(true);
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
//                else if (cell.getIsMovedOver()) cellColor = Color.MAGENTA;
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
        if (obstacle && inStartZone(x, y) || inGoalZone(x, y)) return;

        grid[y - 1][x - 1].setIsObstacle(obstacle);

        if (y >= 2){
            grid[y - 2][x - 1].setVirtualWall(obstacle);            //Cell's bottom

            if (x < ArenaConstants.COLS){
                grid[y - 2][x].setVirtualWall(obstacle);            //Cell's bottom-right
            }
            if (x > 1){
                grid[y - 2][x - 2].setVirtualWall(obstacle);        //Cell's bottom-left
            }
        }
        if (y < ArenaConstants.ROWS){
            grid[y][x - 1].setVirtualWall(obstacle);                  //Cell's top
            if (x < ArenaConstants.COLS - 1){
                grid[y][x].setVirtualWall(obstacle);                  //Cell's top-right
            }
            if (x >= 2){
                grid[y][x - 2].setVirtualWall(obstacle);              //Cell's top-left
            }
        }
        if (x >= 2){
            grid[y - 1][x - 2].setVirtualWall(obstacle);              //Cell's left
        }
        if (x < ArenaConstants.COLS){
            grid[y - 1][x].setVirtualWall(obstacle);                  //Cell's right
        }
    }

    public void clearArena(){
        for (Cell row[] : this.grid){
            for (Cell cell : row){
                cell.setIsObstacle(false);
                if (inStartZone(cell.posX(), cell.posY()) || inGoalZone(cell.posX(), cell.posY())) {
                    cell.setIsExplored(true);
                }
                else {
                    cell.setIsExplored(false);
                    cell.setVirtualWall(false);
                }
                cell.setWayPoint(false);
            }
        }
    }

    public boolean checkValidCoord(int posX, int posY) {
        return posY > 0 && posX > 0 &&
            posY <= ArenaConstants.ROWS && posX <= ArenaConstants.COLS;
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
