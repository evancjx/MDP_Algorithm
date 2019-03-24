package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import robot.Robot;
import robot.RbtConstants.*;
import robot.RbtConstants.MOVEMENT;
import simulator.Simulator;
import utils.CommMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class FastestPath {
    private ArrayList<Cell> openList, closedList;
    private HashMap<Cell, Cell> parents;
    private Arena explored;
    private double[][] gCost;
    private Cell[] neighbors;
    private int loop;
    private DIRECTION robotLastDirection = null;

    public FastestPath(Arena explored){
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
        return (c.getIsExplored() && !c.getIsObstacle() && !c.getIsVirtualWall());
    }

    public ArrayList<MOVEMENT> get(Robot robot, int targetX, int targetY){
        initialize();
        Cell curCell = explored.getCell(robot.getPosX(), robot.getPosY());
        DIRECTION curDir;

        if(robotLastDirection != null) curDir = robotLastDirection;
        else curDir = robot.getDirection();

        openList.add(curCell);
        gCost[robot.getPosY()][robot.getPosX()] = 0;
        System.out.print("Calculating fastest path from (" + curCell.posX() + ", " + curCell.posY() + ") to goal (" + targetX + ", " + targetY + ")...");

        Stack<Cell> path;
        try{
            do{
                loop++;
                curCell = minCostCell(targetX, targetY);

                if(parents.containsKey(curCell)){
                    curDir = getTargetDir(
                        parents.get(curCell).posX(),
                        parents.get(curCell).posY(),
                        curDir, curCell);
                }

                closedList.add(curCell);
                openList.remove(curCell);

                if(closedList.contains(explored.getCell(targetX, targetY))){
                    System.out.println("Goal visited. Path found!");
                    path = getPath(targetX, targetY);
//                    printFastestPath(path);
                    return getPathMovements(path, robot, targetX, targetY);
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
                            if(newG < currentG){
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
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.posX() + ", " + temp.posY() + ") --> ");
            else System.out.print("(" + temp.posX() + ", " + temp.posY() + ")");
        }

        System.out.println("\n");
    }

    private ArrayList<MOVEMENT> getPathMovements(Stack<Cell> path, Robot robot, int targetX, int targetY) {
        StringBuilder pathString = new StringBuilder();

        Cell tempCell = path.pop();
        Robot simulatedRobot = new Robot(robot.getPosX(), robot.getPosY(), robot.getDirection(), false);
        simulatedRobot.setRobotSpeed(1000);
        DIRECTION targetDir;

        ArrayList<MOVEMENT> movements = new ArrayList<>();
        while(simulatedRobot.getPosX() != targetX || simulatedRobot.getPosY() != targetY){
            if(simulatedRobot.getPosX() == tempCell.posX() && simulatedRobot.getPosY() == tempCell.posY())
                tempCell = path.pop();

            targetDir = getTargetDir(simulatedRobot.getPosX(), simulatedRobot.getPosY(), simulatedRobot.getDirection(), tempCell);

            MOVEMENT nextMovement;
            if(simulatedRobot.getDirection() != targetDir){
                nextMovement = MOVEMENT.getNextMovement(simulatedRobot.getDirection(), targetDir);
            }
            else {
                nextMovement = MOVEMENT.FORWARD;
            }

//            System.out.println("Movement " + nextMovement + "\nfrom (" + simulatedRobot.getPosX() + ", " + simulatedRobot.getPosY() + ") to (" + tempCell.posX() + ", " + tempCell.posY() + ")");

            simulatedRobot.move(nextMovement);
            movements.add(nextMovement);
            pathString.append(nextMovement + " ");
        }

        System.out.println("\nMovements: " + pathString.toString());
        if(robotLastDirection == null) robotLastDirection = simulatedRobot.getDirection();
        return movements;
    }

    public ArrayList<MOVEMENT> combineMovements(ArrayList<MOVEMENT> firstSequence, ArrayList<MOVEMENT> secondSequence){

        switch (robotLastDirection){
            case LEFT:
                switch (secondSequence.get(0)){
                    case FORWARD:
                        firstSequence.add(MOVEMENT.RIGHT);
                        break;
                    case LEFT:
                        secondSequence.remove(0);
                        break;
                    case RIGHT:
                        firstSequence.add(MOVEMENT.RIGHT);
                        break;
                }
                break;
            case DOWN:
                switch (secondSequence.get(0)){
                    case FORWARD:
                        firstSequence.add(MOVEMENT.LEFT);
                        firstSequence.add(MOVEMENT.LEFT);
                        break;
                    case LEFT:
                        firstSequence.add(MOVEMENT.RIGHT);
                        secondSequence.remove(0);
                        break;
                    case RIGHT:
                        firstSequence.add(MOVEMENT.LEFT);
                        secondSequence.remove(0);
                        break;
                }
                break;
            case RIGHT:
                switch (secondSequence.get(0)){
                    case FORWARD:
                        firstSequence.add(MOVEMENT.LEFT);
                        break;
                    case LEFT:
                        firstSequence.add(MOVEMENT.LEFT);
                        break;
                    case RIGHT:
                        secondSequence.remove(0);
                        break;
                }
                break;
        }
        firstSequence.addAll(secondSequence);

        return firstSequence;
    }

    public void executeMovements(ArrayList<MOVEMENT> movements, Robot robot){
        int forwardCount = 0;
        long startTime = System.currentTimeMillis();
        StringBuilder commands = new StringBuilder();
        CommMgr commMgr = CommMgr.getCommMgr();

        /*  To string all the commands in one single command to Arduino
            Arduino would then split it up. */
        if(robot.getIsRealRobot()){
            for(MOVEMENT move: movements){
                if(move == MOVEMENT.FORWARD){
                    forwardCount+=1;
                    if(forwardCount == 5){
                        commands.append("W"+forwardCount*10+"]");
                        forwardCount = 0;
                    }
                }
                else{
                    if(forwardCount !=0){
                        commands.append("W"+forwardCount*10+"]");
                        forwardCount = 0;
                    }
                    commands.append(MOVEMENT.getChar(move, true)+"]");
                }
            }
            if(forwardCount != 0)commands.append("W"+forwardCount*10+"]");
            commMgr.sendMsg(commands.toString(), CommMgr.MSG_TYPE_ARDUINO);
            System.out.println(commands.toString());
        }

        //For simulating robot movements on display
        robot.setRealRobot(false);
        if(Simulator.realRun) robot.setRobotSpeed(3);
        String moved = null;
        forwardCount = 0;
        for(MOVEMENT move: movements) {
            robot.move(move);
            System.out.println("Move: " + move);
            Simulator.setFastestPathStatus("Current time: " + (System.currentTimeMillis() - startTime) / 1000.0 + "seconds");
            Simulator.refresh();
            if(!Simulator.realRun) continue;
            if(move == MOVEMENT.FORWARD){
                forwardCount+=1;
            }
            else{
                if(forwardCount != 0)
                    while(moved == null) moved = commMgr.receiveMsg();
                //Movement after forward movement
                while(moved == null) moved = commMgr.receiveMsg();
            }
        }
        String message = "<html>", newLineBreak = "<br/>";
        System.out.println("Fastest Path run complete!");
        message += "Fastest Path run complete!" + newLineBreak;
        message += (System.currentTimeMillis() - startTime) / 1000.0 + " Seconds" + newLineBreak;
        System.out.println((System.currentTimeMillis() - startTime) / 1000.0 + " Seconds");
        message += "</html>";

        Simulator.setFastestPathStatus(message);
    }
}
