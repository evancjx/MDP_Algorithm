package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import robot.Robot;
import simulator.Simulator;

public class Exploration {
    private final Arena explored, arena;
    private final Robot robot;
    private final int coverageLimit, timeLimit;
    private int areaExplored;

    public Exploration(Arena explored, Arena arena, Robot robot,
               int coverageLimit, int timeLimit){
        this.explored = explored;
        this.arena = arena;
        this.robot = robot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }

    public void execute(){
        System.out.println("Starting exploration...");
        senseSurrounding();
//        System.out.println("Calculating area explored");
        areaExplored = calculateAreaExplored();
        System.out.println("Explored Area: " + areaExplored);
        loopRun(robot.getPosX(), robot.getPosY());
    }

    private void senseSurrounding(){
        System.out.println("Sensing surrounding");
        robot.setSenors();
        robot.sense(explored, arena);
        System.out.println("Repaint");
        explored.repaint();
    }

    private int calculateAreaExplored(){
        int result = 0;
        for (int y = 1; y <= ArenaConstants.ROWS; y++) {
            for (int x = 1; x <=  ArenaConstants.COLS; x++) {
                if (explored.getCell(x, y).getIsExplored()) {
                    result++;
                }
            }
        }
        return result;
    }

    private void loopRun(int initX, int initY){
        do{
            nextMove();
            areaExplored = calculateAreaExplored();
            System.out.println("Explored Area: " + areaExplored);
            if(robot.getPosX() == initX && robot.getPosY() ==initY){
                if (areaExplored > 290){
                    break;
                }
            }
        } while (areaExplored <= coverageLimit);
        System.out.println("finish run");
    }

    private void nextMove(){
        int rbtX = robot.getPosX();
        int rbtY = robot.getPosY();
        System.out.println("Rbt PosX: " + rbtX + " PosY: " + rbtY);
        if(lookRightEmpty(rbtX, rbtY)){
            moveBot(4);
            if(lookForward(rbtX, rbtY)) moveBot(1);
        } else if (lookForward(rbtX, rbtY)){

            moveBot(1);
        } else if (lookLeftEmpty(rbtX, rbtY)){
            moveBot(3);
            if(lookForward(rbtX, rbtY)) moveBot(1);
        } else {
            moveBot(4); //Depends on which rotation LEFT or RIGHT is better
            moveBot(4);
        }
    }

    private boolean lookForward(int rbtX, int rbtY){
        switch (robot.getDirection()){
            case 1: //Face UP
                return upFree(rbtX, rbtY);
            case 2: //Face DOWN
                return downFree(rbtX, rbtY);
            case 3: //Face LEFT
                return leftFree(rbtX, rbtY);
            case 4: //Face RIGHT
                return rightFree(rbtX, rbtY);
        }
        return false;
    }

    private boolean lookLeftEmpty(int rbtX, int rbtY){
        switch (robot.getDirection()){
            case 1: //Face UP
                return leftFree(rbtX, rbtY);
            case 2: //Face DOWN
                return rightFree(rbtX, rbtY);
            case 3: //Face LEFT
                return downFree(rbtX, rbtY);
            case 4: //Face RIGHT
                return upFree(rbtX, rbtY);
        }
        return false;
    }

    private boolean lookRightEmpty(int rbtX, int rbtY){
        switch (robot.getDirection()){
            case 1:
                return rightFree(rbtX, rbtY);
            case 2: //Face DOWN
                return leftFree(rbtX, rbtY);
            case 3: //Face LEFT
                return upFree(rbtX, rbtY);
            case 4: //Face RIGHT
                return downFree(rbtX, rbtY);
        }
        return false;
    }

    private boolean upFree(int rbtX, int rbtY){
        System.out.println("upFree");
        return  isExploredNotObstacle(rbtX - 1, rbtY + 1) &&
                isExploredAndFree(rbtX, rbtY + 1) &&
                isExploredNotObstacle(rbtX + 1, rbtY + 1) &&
                explored(rbtX, rbtY + 2);
    }

    private boolean downFree(int rbtX, int rbtY){
        System.out.println("downFree");
        return  isExploredNotObstacle(rbtX - 1, rbtY - 1) &&
                isExploredAndFree(rbtX, rbtY - 1) &&
                isExploredNotObstacle(rbtX + 1, rbtY - 1) &&
                explored(rbtX, rbtY - 2);
    }

    private boolean leftFree(int rbtX, int rbtY){
        System.out.println("leftFree");
        return  isExploredNotObstacle(rbtX -1, rbtY - 1) &&
                isExploredAndFree(rbtX -1, rbtY) &&
                isExploredNotObstacle(rbtX - 1, rbtY + 1) &&
                explored(rbtX - 2, rbtY);
    }

    private boolean rightFree(int rbtX, int rbtY){
        System.out.println("rightFree");
        return  isExploredNotObstacle(rbtX + 1, rbtY + 1) &&
                isExploredAndFree(rbtX + 1, rbtY) &&
                isExploredNotObstacle(rbtX + 1, rbtY - 1) &&
                explored(rbtX + 2, rbtY);
    }

    private boolean isExploredNotObstacle(int posX, int posY){
        if(explored.checkValidCoord(posX,posY)){
            Cell temp = explored.getCell(posX, posY);
            return temp.getIsExplored() && !temp.getIsObstacle();
        }
        return false;
    }

    private boolean isExploredAndFree(int posX, int posY){
        if(explored.checkValidCoord(posX, posY)){
            Cell temp = explored.getCell(posX, posY);
            return temp.getIsExplored() && !temp.getIsObstacle() && !temp.getIsVirualWall();
        }
        return false;
    }

    private boolean explored(int posX, int posY){
        if(explored.checkValidCoord(posX, posY)){
            Cell temp = explored.getCell(posX, posY);
            System.out.println("PosX: " + posX + " PosY: " + posX + " Explored: " + temp.getIsExplored());
        }
        return true;
    }


    private void moveBot(int direction){
        robot.move(direction);
        Simulator.refresh();
        senseSurrounding();
    }

//    goHome();
}
