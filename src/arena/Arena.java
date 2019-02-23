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
            }
        }
    }

    public void paintComponent(Graphics g){
        for (Cell row[] : grid){
            for (Cell cell : row){
                Color cellColor;
                if (inStartZone(cell.x, cell.y)) cellColor = Color.GREEN;
                else if (inGoalZone(cell.x, cell.y)) cellColor = Color.RED;
                else if (cell.getIsObstacle()) cellColor = Color.BLACK;
                else cellColor = Color.WHITE;

                g.setColor(cellColor);
                g.fillRect(cell.cellX, cell.cellY, cell.cellSize, cell.cellSize);
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
        return x >= 0 && y >=0 &&
            x <= ArenaConstants.START_X + 1 && y <= ArenaConstants.START_Y + 1;
    }

    private boolean inGoalZone(int x, int y){
        return x >= 13 && y >= 18 &&
            x <= 20 && y <= 20;
    }

    public void setObstacle(int x, int y, boolean obstacle){
        if (obstacle && inStartZone(x, y) || inGoalZone(x, y)) return;

        grid[y][x].setIsObstacle(obstacle);
    }

    public void clearArena(){
        for (Cell row[] : this.grid){
            for (Cell cell : row){
                cell.setIsObstacle(false);
                if (inStartZone(cell.x, cell.y) || inGoalZone(cell.x, cell.y)) {
                    cell.setIsExplored(true);
                }
                else {
                    cell.setIsExplored(false);
                }

            }
        }
    }
}
