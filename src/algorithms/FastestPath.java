package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import javafx.scene.control.skin.TextInputControlSkin;
import robot.Robot;
import robot.RbtConstants.*;
import simulator.Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class FastestPath {
    private ArrayList<Cell> openList, closedList;
    private HashMap<Cell, Cell> parents;
    private Cell[] neighbors;
    private Robot robot;
    private Arena explored;
    private double[][] gCost;
    private Cell currentCell;
    private DIRECTION curDir;

    public FastestPath(Arena explored, Robot robot){
        initialize(explored, robot);
    }

    private void initialize(Arena explored, Robot robot){
        this.explored = explored;
        this.robot = robot;
        this.openList = new ArrayList<>();
        this.closedList = new ArrayList<>();
        this.parents = new HashMap<>();
        this.neighbors = new Cell[4];
        this.gCost = new double[ArenaConstants.ROWS][ArenaConstants.COLS];
        this.currentCell = explored.getCell(robot.getPosX(), robot.getPosY());
        this.curDir = robot.getDirection();

        // Initialise gCosts array
        for(Cell[] row: this.explored.grid){
            for(Cell cell: row){
                if(!canBeVisited(cell))
                    gCost[cell.y - 1][cell.x - 1] = 9999;
                else
                    gCost[cell.y - 1][cell.x - 1] = 0;
            }
        }
        openList.add(currentCell);
    }

    public Stack<Cell> FindFastestPath(int targetX, int targetY) {
        Stack<Cell> path = new Stack<>();
        System.out.println("Finding Fastest path to Goal.....");
        do {
            currentCell = minCostCell(targetX, targetY);
            if (parents.containsKey(currentCell)) {
                curDir = getTargetDir(parents.get(currentCell).x, parents.get(currentCell).y, curDir, currentCell);
            }
            openList.remove(currentCell);
            closedList.add(currentCell);
            // Check Target reached
            if (closedList.contains(explored.getCell(targetX, targetY))) {
                System.out.println("Goal Reached");
                Cell temp = explored.getCell(targetX, targetY);
                while (true) {
                    path.push(temp);
                    temp = parents.get(temp);
                    if (temp == null) {
                        break;
                    }
                }
                return path;
            }
            // Find valid neighbors
            if(explored.checkValidCoord(currentCell.x, currentCell.y + 1)){
                neighbors[0] = explored.getCell(currentCell.x, currentCell.y + 1);
                if(!canBeVisited(neighbors[0])){neighbors[0] = null;}
            }
            //DOWN
            if(explored.checkValidCoord(currentCell.x, currentCell.y - 1)){
                neighbors[1] = explored.getCell(currentCell.x, currentCell.y - 1);
                if(!canBeVisited(neighbors[1])){neighbors[1] = null;}
            }
            //LEFT
            if(explored.checkValidCoord(currentCell.x - 1, currentCell.y)){
                neighbors[2] = explored.getCell(currentCell.x - 1, currentCell.y);
                if(!canBeVisited(neighbors[2])){neighbors[2] = null;}
            }
            //RIGHT
            if(explored.checkValidCoord(currentCell.x + 1, currentCell.y)){
                neighbors[3] = explored.getCell(currentCell.x + 1, currentCell.y);
                if(!canBeVisited(neighbors[3])){neighbors[3] = null;}
            }
            // Generate Children for hash map
            for (int i = 0; i < 4; i++) {
                if (neighbors[i] != null) {
                    if (openList.contains(neighbors[i])) {
                        continue;
                    }

                    if (!(openList.contains(neighbors[i]))&&!(closedList.contains(neighbors[i]))) {
                        parents.put(neighbors[i], currentCell);
                        System.out.println(neighbors[i].x + "," + neighbors[i].y + " First " +currentCell.x + "," +currentCell.y);
                        gCost[neighbors[i].y - 1][neighbors[i].x - 1] = gCost[currentCell.y - 1][currentCell.x - 1] + recalCost(currentCell, neighbors[i], curDir);
                        openList.add(neighbors[i]);
                    } else {
                        double currentGScore = gCost[neighbors[i].y - 1][neighbors[i].x - 1];
                        double newGScore = gCost[neighbors[i].y - 1][neighbors[i].x - 1] + recalCost(currentCell, neighbors[i], curDir);
                        if (newGScore < currentGScore) {
                            gCost[neighbors[i].y - 1][neighbors[i].x - 1] = newGScore;
                            parents.put(neighbors[i], currentCell);
                            //System.out.println(neighbors[i].getPosition().toString() + " Second " +current.getPosition().toString());
                        }
                    }
                }
            }
        } while (!openList.isEmpty());
        System.out.println("Path not Found");
        return null;

    }

    private double recalCost(Cell currentCell, Cell neighbor, DIRECTION curDir) {
        double movCost = 10; // one movement to neighbor

        double turnCost;
        DIRECTION targetDir;
        if (currentCell.x - neighbor.x > 0)
            targetDir = DIRECTION.LEFT;
        else if (neighbor.x - currentCell.x  > 0)
            targetDir =DIRECTION.RIGHT;
        else {
            if (currentCell.y - neighbor.y > 0)
                targetDir =DIRECTION.DOWN;
            else if (neighbor.y - currentCell.y  > 0)
                targetDir = DIRECTION.UP;
            else
                targetDir = curDir;

        }
        int numOfTurn = Math.abs(curDir.ordinal() - targetDir.ordinal());
        if (numOfTurn > 2)
            numOfTurn = numOfTurn % 2;
        turnCost = numOfTurn * 20;

        return (movCost + turnCost);
    }

    private Cell minCostCell(int targetX, int targetY) {
        double minCost = 9999;
        Cell minCell = null;
        for(int i = openList.size()-1; i >= 0; i--){
            double cost = gCost[openList.get(i).y-1][openList.get(i).x-1] + hCost(openList.get(i), targetX, targetY);
            if(cost < minCost){
                minCost = cost;
                minCell = openList.get(i);
            }
        }
        return minCell;
    }

    private double hCost(Cell cell, int targetX, int targetY) {
        double movCost = (Math.abs(targetX - cell.x) + Math.abs(targetY - cell.y)) * 10;
        if (movCost == 0){return 0;}
        double turnCost = 0;
        if(targetX - cell.x != 0 || targetY - cell.y != 0){turnCost = 20;}
        return (movCost + turnCost);
    }

    private DIRECTION getTargetDir(int botX, int botY, DIRECTION botDir, Cell target) {
        if (botX - target.x > 0) {
            return DIRECTION.LEFT;
        } else if (target.x - botX > 0) {
            return DIRECTION.RIGHT;
        } else {
            if (botY - target.y > 0) {
                return DIRECTION.DOWN;
            } else if (target.y - botY > 0) {
                return DIRECTION.UP;
            } else {
                return botDir;
            }
        }
    }

    private boolean canBeVisited(Cell c) {
        return c.getIsExplored() && !c.getIsObstacle() && !c.getIsVirtualWall();
    }

    public static void printFastestPath(Stack<Cell> path) {
        System.out.println("The number of steps is: " + (path.size() - 1) + "\n");

        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
        Cell temp;
        System.out.println("Path:");
        while (!pathForPrint.isEmpty()) {
            temp = pathForPrint.pop();
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.x + ", " + temp.y + ") --> ");
            else System.out.print("(" + temp.x + ", " + temp.y + ")");
        }

        System.out.println("\n");
    }
//    public void printGCosts() {
//        for (int i = 1; i <= 20; i++) {
//            for (int j = 1; j <= 15; j++) {
//                System.out.print(gCost[i-1][j-1]);
//                System.out.print(";");
//            }
//            System.out.println("\n");
//        }
//
//    }
//    public void execute(Stack<Cell> path){
//        Cell moveStep = path.pop();
//        robot.move(getTargetDir(robot.getPosX(),robot.getPosY(), robot.getDirection(), moveStep));
//        Simulator.refresh();
//        moveStep.setIsFastestPath(true);
//        explored.repaint();
//    }
}

