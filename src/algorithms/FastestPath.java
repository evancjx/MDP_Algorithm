package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import robot.Robot;
import robot.RbtConstants.*;
import simulator.Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class FastestPath {
    private ArrayList<Cell> openList, closedList;
    private HashMap<Cell, Cell> parents;
    private Arena explored;
    private double[][] gCost;
    private Cell curCell;
    private DIRECTION curDir;
    private Cell[] neighbors;
    private int loop;
    private Boolean explorationMode;

    public FastestPath(Arena explored){
        this.explorationMode = false;
        this.explored = explored;
    }

    public FastestPath(Arena explored, boolean explorationMode){
        this.explorationMode = explorationMode;
        this.explored = explored;
    }

    private void initialize(){
        this.openList = new ArrayList<>();
        this.closedList = new ArrayList<>();
        this.parents = new HashMap<>();
        this.neighbors = new Cell[4];
        this.gCost = new double[ArenaConstants.ROWS][ArenaConstants.COLS];
        // Initialise gCosts array
        for(Cell[] row: this.explored.grid){
            for(Cell cell: row){
                if(!canBeVisited(cell))
                    gCost[cell.posY() - 1][cell.posX() - 1] = 9999;
                else
                    gCost[cell.posY() - 1][cell.posX() - 1] = 0;
            }
        }
    }

    private boolean canBeVisited(Cell c) {
        return c.getIsExplored() && !c.getIsObstacle() && !c.getIsVirtualWall();
    }

    public ArrayList<MOVEMENT> get(Robot robot, int targetX, int targetY){
        initialize();
        int initX = robot.getPosX(), initY = robot.getPosY();
        DIRECTION initDir = robot.getDirection();
        this.curCell = explored.getCell(robot.getPosX(), robot.getPosY());
        this.curDir = robot.getDirection();
        openList.add(curCell);
        gCost[robot.getPosY()][robot.getPosX()] = 0;
        System.out.println("Calculating fastest path from (" + curCell.posX() + ", " + curCell.posY() + ") to goal (" + targetX + ", " + targetY + ")...");

        Stack<Cell> path;
        try{
            do{
                loop++;
                curCell = minCostCell(targetX, targetY);

                if(parents.containsKey(curCell)){
                    curDir = getTargetDir(
                            parents.get(curCell).posX(), parents.get(curCell).posY(),
                            curDir, curCell);
                }

                closedList.add(curCell);
                openList.remove(curCell);

                if(closedList.contains(explored.getCell(targetX, targetY))){
                    System.out.println("Goal visited. Path found!");
                    path = getPath(targetX, targetY);
                    printFastestPath(path);
                    robot.setRobotPos(initX, initY);
                    robot.setDirection(DIRECTION.UP);
                    robot.setRobotFront(DIRECTION.UP);
                    ArrayList<MOVEMENT> movements = getPathMovements(path, robot, targetX, targetY);
                    robot.setRobotPos(initX, initY);
                    robot.setDirection(DIRECTION.UP);
                    robot.setRobotFront(DIRECTION.UP);
                    return movements;
                }

                //TOP
                if(explored.checkValidCoord(curCell.posX(), curCell.posY() + 1)){
                    neighbors[0] = explored.getCell(curCell.posX(), curCell.posY() + 1);
                    if(!canBeVisited(neighbors[0])) neighbors[0] = null;
                }
                //BOTTOM
                if(explored.checkValidCoord(curCell.posX(), curCell.posY() - 1)){
                    neighbors[1] = explored.getCell(curCell.posX(), curCell.posY() - 1 );
                    if(!canBeVisited(neighbors[1])) neighbors[1] = null;
                }
                //LEFT
                if(explored.checkValidCoord(curCell.posX() - 1, curCell.posY())){
                    neighbors[2] = explored.getCell(curCell.posX() - 1, curCell.posY());
                    if(!canBeVisited(neighbors[2])) neighbors[2] = null;
                }
                //RIGHT
                if(explored.checkValidCoord(curCell.posX() + 1, curCell.posY())){
                    neighbors[3] = explored.getCell(curCell.posX() + 1, curCell.posY());
                    if(!canBeVisited(neighbors[3])) neighbors[3] = null;
                }
                double currentG, newG;
                for(Cell cell : neighbors){
                    if (cell != null){
                        if (closedList.contains(cell)) continue;

                        if (!openList.contains(cell)){
                            parents.put(cell, curCell);
                            gCost[cell.posY()-1][cell.posX()-1] = gCost[curCell.posY()-1][curCell.posX()-1] + costG(curCell, cell, curDir);
                            openList.add(cell);
                        }
                        else {
                            currentG = gCost[cell.posY()-1][cell.posX()-1];
                            newG = gCost[curCell.posY()-1][curCell.posX()-1] + costG(curCell, cell, curDir);
                            if( newG < currentG){
                                gCost[cell.posY()-1][cell.posX()-1] = newG;
                                parents.put(cell, curCell);
                            }
                        }
                    }
                }

            } while (!openList.isEmpty());
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println("Path not found!");
        return null;
    }

    private Cell minCostCell(int targetX, int targetY){
        int size = openList.size();
        double minCost = 9999;
        Cell result = null;
        double gCost, cost;
        for (int i = size - 1; i >= 0; i--){
            gCost = this.gCost[openList.get(i).posY() - 1][openList.get(i).posX() - 1];
            cost = gCost + heuCost(openList.get(i), targetX, targetY);
            if (cost < minCost){
                minCost = cost;
                result = openList.get(i);
            }
        }

        return result;
    }

    private double heuCost(Cell cell, int targetX, int targetY){
        double movementCost = (
            Math.abs(targetY - cell.posY()) +
            Math.abs(targetX - cell.posX()) )* 10;

        if (movementCost == 0) return 0;

        double turnCost = 0;
        if(targetX - cell.posX() != 0 || targetY - cell.posY() != 0)
            turnCost = 20;

        return movementCost + turnCost;
    }

    private DIRECTION getTargetDir(int rbtX, int rbtY, DIRECTION rbtDir, Cell target){
        if(rbtX - target.posX() > 0){
            return DIRECTION.LEFT;
        } else if (target.posX() - rbtX > 0){
            return DIRECTION.RIGHT;
        } else {
            if (rbtY - target.posY() > 0){
                return DIRECTION.DOWN;
            } else if (target.posY() - rbtY > 0){
                return DIRECTION.UP;
            } else {
                return rbtDir;
            }
        }
    }

    private double costG(Cell from, Cell to, DIRECTION fromDir){
        double moveCost = 10;
        DIRECTION targetDir = getTargetDir(from.posX(), from.posY(), fromDir, to);
        return getTurnCost(fromDir, targetDir) + moveCost;
    }

    private double getTurnCost(DIRECTION from, DIRECTION to){
        int numOfTurn = Math.abs(from.ordinal() - to.ordinal());
        if (numOfTurn > 2) numOfTurn%=2;
        return numOfTurn;
    }

    private Stack<Cell> getPath(int targetX, int targetY) {
        Stack<Cell> actualPath = new Stack<>();
        Cell temp = explored.getCell(targetX, targetY);

        while (true) {
            actualPath.push(temp);
            temp = parents.get(temp);
            if (temp == null)break;
        }

        return actualPath;
    }

    private void printFastestPath(Stack<Cell> path) {
        System.out.println("\nLooped " + loop + " times.");
        System.out.println("The number of steps is: " + (path.size() - 1) + "\n");

        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
        Cell temp;
        System.out.println("Path:");
        while (!pathForPrint.isEmpty()) {
            temp = pathForPrint.pop();
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.posY() + ", " + temp.posX() + ")\n --> ");
            else System.out.print("(" + temp.posY() + ", " + temp.posX() + ")");
        }

        System.out.println("\n");
    }

    private ArrayList<MOVEMENT> getPathMovements(Stack<Cell> path, Robot robot, int targetX, int targetY) {
        StringBuilder pathString = new StringBuilder();

        Cell tempCell = path.pop();
        Robot tempBot = new Robot(robot.getPosX(), robot.getPosY(), robot.getDirection(), false);
        tempBot.setRobotSpeed(1000);
        DIRECTION targetDir;

        ArrayList<MOVEMENT> movements = new ArrayList<>();

        while(tempBot.getPosX() != targetX || tempBot.getPosY() != targetY){
            if(tempBot.getPosX() == tempCell.posX() && tempBot.getPosY() == tempCell.posY())
                tempCell = path.pop();

            targetDir = getTargetDir(tempBot.getPosX(), tempBot.getPosY(), tempBot.getDirection(), tempCell);

            MOVEMENT nextMovement;
            if(tempBot.getDirection() != targetDir){
                nextMovement = getNextMovement(tempBot.getDirection(), targetDir);
            }
            else {
                nextMovement = MOVEMENT.FORWARD;
            }

            System.out.println("Movement " + nextMovement + "\nfrom (" + tempBot.getPosX() + ", " + tempBot.getPosY() + ") to (" + tempCell.posX() + ", " + tempCell.posY() + ")");


            tempBot.move(nextMovement);
            movements.add(nextMovement);
            pathString.append(nextMovement + " ");
        }
        System.out.println("\nMovements: " + pathString.toString());
        return movements;
    }

    public void executeMovements(ArrayList<MOVEMENT> movements, Robot robot){
        int count = 0;
        if(!robot.isRealRobot()){
            for(MOVEMENT move: movements){
                robot.move(move);
                Simulator.refresh();
            }
        }
        else{
            for (MOVEMENT move: movements){
                if(move==MOVEMENT.FORWARD){
                    count+=1;
                    continue;
                }
                else{
                    if(count != 0 ){
                        robot.moveForwardMultiple(count);
                        count = 0;
                    }
                    robot.move(move);
                }
                Simulator.refresh();
            }
        }

    }

    private MOVEMENT getNextMovement(DIRECTION from, DIRECTION to){
        switch (from){
            case UP:
                switch (to){
                    // no case UP
                    case LEFT:
                        return MOVEMENT.LEFT;
                    case DOWN:
                        return MOVEMENT.LEFT;
                    case RIGHT:
                        return MOVEMENT.RIGHT;
                }
                break;
            case LEFT:
                switch (to){
                    case UP:
                        return MOVEMENT.RIGHT;
                    // no case LEFT
                    case DOWN:
                        return MOVEMENT.LEFT;
                    case RIGHT:
                        return MOVEMENT.LEFT;
                }
                break;
            case DOWN:
                switch (to){
                    case UP:
                        return MOVEMENT.LEFT;
                    case LEFT:
                        return MOVEMENT.RIGHT;
                    // no case DOWN
                    case RIGHT:
                        return MOVEMENT.LEFT;
                }
                break;
            case RIGHT:
                switch (to){
                    case UP:
                        return MOVEMENT.LEFT;
                    case LEFT:
                        return MOVEMENT.LEFT;
                    case DOWN:
                        return MOVEMENT.RIGHT;
                }

        }
        return null;
    }

}
