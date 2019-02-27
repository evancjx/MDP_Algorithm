package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import robot.Robot;

import java.util.ArrayList;
import java.util.HashMap;

public class FastestPath {
    private ArrayList<Cell> openList, closedList;
    private HashMap<Cell, Cell> parents;
    private Robot robot;
    private Arena explored;
    private double[][] gCost;

    public FastestPath(Arena explored, Robot robot){
        initialize(explored, robot);
    }

    private void initialize(Arena explored, Robot robot){
        this.explored = explored;
        this.robot = robot;
        this.openList = new ArrayList<>();
        this.closedList = new ArrayList<>();
        this.parents = new HashMap<>();
        this.gCost = new double[ArenaConstants.ROWS][ArenaConstants.COLS];

        // Initialise gCosts array
        for(Cell[] row: this.explored.grid){
            for(Cell cell: row){
                if(!canBeVisited(cell))
                    gCost[cell.y - 1][cell.x - 1] = 9999;
                else
                    gCost[cell.y - 1][cell.x - 1] = 0;
            }
        }
    }

    private boolean canBeVisited(Cell c) {
        return c.getIsExplored() && !c.getIsObstacle() && !c.getIsVirtualWall();
    }
}
