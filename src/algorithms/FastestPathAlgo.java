package algorithms;

import arena.Arena;
import arena.Cell;
import robot.Robot;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class FastestPathAlgo {
    public enum Direction {UP, LEFT, DOWN, RIGHT};
    private ArrayList<Cell> openList;       //array of Cells with gCost calculated
    private ArrayList<Cell> closedList;     //array of Cells already included in path
    private Cell current;                   //current Cell
    private HashMap<Cell,Cell> parents;
    private Cell[] neighbors;               //array of 4 neighbours of current Cell
    private int curDir;                      //current direction of robot
    private double gCosts[][];               //array of real cost from start pos to [x][y]
    private Robot bot;
    private Arena exploredArena;               //explored Arena
    public FastestPathAlgo(Arena exploredMap, Robot bot) {
        this.exploredArena = exploredMap;
        init(this.exploredArena,bot);
    }
    public void init(Arena exploredMap, Robot bot){
        this.bot = bot;
        this.exploredArena = exploredMap;
        this.openList = new ArrayList<>();
        this.closedList = new ArrayList<>();
        this.parents = new HashMap<>();
        this.neighbors = new Cell[4];
        this.current = exploredMap.getCell(bot.getPosX(),bot.getPosY()); // starts in the start zone, robot center.
        this.curDir = bot.getDirection();
        this.gCosts = new double[20][15];

        // Initialise gCosts array
        for (int i = 1; i <=20; i++) {
            for (int j = 1; j <= 15; j++) {
                if (!(exploredMap.getCell(i,j).getIsVirtualWall()&& exploredMap.getCell(i,j).getIsObstacle())) {
                    gCosts[i][j] = 0;
                } else {
                    gCosts[i][j] = 9999;
                }
            }
        }
        openList.add(current);

        // Initialise starting point
        gCosts[1][1] = 0;
    }

    public Stack<Cell> FindFastestPath(Robot bot, int x, int y){
        Stack<Cell> actualPath = new Stack<Cell>();
        System.out.println("Finding Fastest path to Goal.....");
        do{
            current=minCostCell(x,y);

            if (parents.containsKey(current)) {
                switch(getTargetDir(parents.get(current).getX(), parents.get(current).getY(), curDir, current)){
                    case UP: curDir=1; break;
                    case DOWN: curDir=2; break;
                    case LEFT: curDir=3; break;
                    case RIGHT: curDir=4; break;
                }
            }
            openList.remove(current);
            closedList.add(current);

            // Check if goal is reached
            if(closedList.contains(exploredArena.getCell(x,y))){
                while (true) {
                    actualPath.push(current);
                    current = parents.get(current);
                    if (current == exploredArena.getCell(2,2)) {
                        break;
                    }
                }
                System.out.println("Goal Reached");
                return actualPath;
            }
            // Find Neighbors
            //UP
            if (!((current.getX() + 1)>=20)&& !((current.getX()+1)<0)){
                neighbors[0] = exploredArena.getCell(current.getX() + 1, current.getY());
                if (neighbors[0].getIsVirtualWall() && neighbors[0].getIsObstacle() && neighbors[0].getIsExplored()) {
                    neighbors[0] = null;
                }
            }
            //DOWN
            if (!((current.getX()-1)>=20)&& !((current.getX()-1)<0)){
                neighbors[1] = exploredArena.getCell(current.getX() - 1, current.getY());
                if (neighbors[1].getIsVirtualWall() && neighbors[1].getIsObstacle() && neighbors[1].getIsExplored()) {
                    neighbors[1] = null;
                }
            }
            //LEFT
            if (!((current.getY()-1)>=15)&&!((current.getY() - 1)<0)){
                neighbors[2] = exploredArena.getCell(current.getX(), current.getY() - 1);
                if (neighbors[2].getIsVirtualWall() && neighbors[2].getIsObstacle() && neighbors[2].getIsExplored()) {
                    neighbors[2] = null;
                }
            }
            //RIGHT
            if (!((current.getY() + 1)>=15) && !((current.getY()+1)<0)) {
                neighbors[3] = exploredArena.getCell(current.getX(), current.getY() + 1);
                if (neighbors[3].getIsVirtualWall() && neighbors[3].getIsObstacle() && neighbors[3].getIsExplored()) {
                    neighbors[3] = null;
                }
            }

            // Generate Children
            for (int i = 0; i < 4; i++) {
                if (neighbors[i] != null) {
                    if (openList.contains(neighbors[i])) {
                        continue;
                    }

                    if (!(openList.contains(neighbors[i]))) {
                        if(!(parents.containsKey(neighbors[i]))){
                            parents.put(neighbors[i], current);
                            //System.out.println(neighbors[i].toString() + " First " +current.toString());
                        }
                        gCosts[neighbors[i].getX()][neighbors[i].getY()] = gCosts[current.getX()][current.getY()] + costG(current, neighbors[i], curDir);
                        openList.add(neighbors[i]);
                    } else {
                        double currentGScore = gCosts[neighbors[i].getX()][neighbors[i].getY()];
                        double newGScore = gCosts[current.getX()][current.getY()] + costG(current, neighbors[i], curDir);
                        if (newGScore < currentGScore) {
                            gCosts[neighbors[i].getX()][neighbors[i].getY()] = newGScore;
                            parents.put(neighbors[i], current);
                            //System.out.println(neighbors[i].toString() + " Second " +current.toString());
                        }
                    }
                }
            }
        }while(!openList.isEmpty());

        System.out.println("Path not found!");
        return null;
    }

    private Cell minCostCell(int goalX, int goalY) {
        double minCost = 9999;
        Cell bestCell = null;
        for(int i=openList.size()-1; i>=0;i--){
            double gCost = gCosts[(openList.get(i).getX())][(openList.get(i).getY())];
            double cost = gCost + Hcost(openList.get(i), goalX, goalY);
            if (cost < minCost) {
                minCost = cost;
                bestCell = openList.get(i);
            }
        }
        return bestCell;
    }

    private double Hcost(Cell Cell, int goalX, int goalY) {
        // calculate heuristic based on distance from goal.
        double movCost = (Math.abs(goalX - Cell.getX()) + Math.abs(goalY - Cell.getY())) * 10;
        if (movCost == 0)
            return 0;

        // turn cost must be added to heuristic based on number of turns
        double turnCost = 0;
        if (goalX - Cell.getX() != 0 || goalY - Cell.getY() != 0) {
            turnCost = 20;
        }
        return (movCost +turnCost);
    }

    private Direction getTargetDir(int botX, int botY, int botDir, Cell target) {
        if (botX - target.getX() > 0) {
            return Direction.LEFT;
        } else if (target.getX() - botX > 0) {
            return  Direction.RIGHT;
        } else {
            if (botY - target.getY() > 0) {
                return Direction.UP;
            } else if (target.getY() - botY > 0) {
                return Direction.DOWN;
            } else {
                switch(botDir){
                    case 1: return Direction.UP;
                    case 2: return Direction.DOWN;
                    case 3: return Direction.LEFT;
                    case 4: return Direction.RIGHT;
                }
            }
        }
        return null;
    }

    /**
     * Calculate the actual cost of moving from Cell a to Cell b (assuming both are neighbors).
     */
    private double costG(Cell current, Cell target, int curDir) {
        double movCost = 10; // one movement to neighbor
        Direction curD;
        double turnCost;
        Direction targetDir;
        switch(curDir){
            case 1: curD=Direction.UP; break;
            case 2: curD=Direction.DOWN; break;
            case 3: curD=Direction.LEFT; break;
            case 4: curD=Direction.RIGHT; break;
            default: curD=Direction.UP; break;
        }
        if (current.getX() - target.getX() > 0)
            targetDir = Direction.LEFT;
        else if (target.getX() - current.getX() > 0)
            targetDir = Direction.RIGHT;
        else {
            if (current.getY() - target.getY() > 0)
                targetDir = Direction.DOWN;
            else if (target.getY() - current.getY() > 0)
                targetDir = Direction.UP;
            else
                targetDir = curD;

        }
        int numOfTurn = Math.abs(curD.ordinal() - targetDir.ordinal());
        if (numOfTurn > 2)
            numOfTurn = numOfTurn % 2;
        turnCost = numOfTurn * 20;

        return movCost + turnCost;
    }
    public void printFastestPath(Stack<Cell> path) {
        System.out.println("The number of steps is: " + (path.size() - 1) + "\n");

        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
        Cell temp;
        System.out.println("Path:");
        while (!pathForPrint.isEmpty()) {
            temp = pathForPrint.pop();
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.getX() + ", " + temp.getY() + ") --> ");
            else System.out.print("(" + temp.getX() + ", " + temp.getY() + ")");
        }

        System.out.println("\n");
    }


}

