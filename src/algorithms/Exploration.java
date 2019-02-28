package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import robot.RbtConstants.*;
import robot.Robot;
import simulator.Simulator;

import java.util.concurrent.TimeUnit;

public class Exploration {
    private final Arena explored, arena;
    private final Robot robot;
    private final int coverageLimit, timeLimit;
    private long endTime;

    public Exploration(Arena explored, Arena arena, Robot robot, int coverageLimit, int timeLimit){
        this.explored = explored;
        this.arena = arena;
        this.robot = robot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }

    public void execute(){
        System.out.println("Starting exploration...");
        endTime = System.currentTimeMillis() + (timeLimit* 1000);
        senseSurrounding();
        loopRun(robot.getPosX(), robot.getPosY());
    }

    private void senseSurrounding(){
        robot.setSenors();
        robot.sense(explored, arena);
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
        int areaExplored;
        do{
            areaExplored = calculateAreaExplored();
            System.out.println("Explored Area: " + areaExplored);
            nextMove();

            if(robot.getPosX() == initX && robot.getPosY() ==initY)
                if (areaExplored > 290) break;


            if(true){
                try {
                    TimeUnit.MILLISECONDS.sleep(robot.getSpeed());
                } catch (InterruptedException e) {
//                    System.out.println("Something went wrong in Robot.move()!");
                    break;
                }
            }
        } while (areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime);
        turnToDirection(DIRECTION.UP);
        System.out.println("finish run");
    }

    private void nextMove(){
        int rbtX = robot.getPosX();
        int rbtY = robot.getPosY();
        if(lookRightEmpty(rbtX, rbtY)){
            moveBot(DIRECTION.RIGHT);
            if(lookForward(rbtX, rbtY)) moveBot(DIRECTION.UP);
        } else if (lookForward(rbtX, rbtY)){
            moveBot(DIRECTION.UP);
        } else if (lookLeftEmpty(rbtX, rbtY)){
            moveBot(DIRECTION.LEFT);
            if(lookForward(rbtX, rbtY)) moveBot(DIRECTION.UP);
        } else {
            moveBot(DIRECTION.LEFT); //Depends on which rotation LEFT or RIGHT is better
            moveBot(DIRECTION.LEFT);
        }
    }

    private boolean lookForward(int rbtX, int rbtY){
        switch (robot.getDirection()){
            case UP: //Face UP
                return upFree(rbtX, rbtY);
            case LEFT: //Face LEFT
                return leftFree(rbtX, rbtY);
            case DOWN: //Face DOWN
                return downFree(rbtX, rbtY);
            case RIGHT: //Face RIGHT
                return rightFree(rbtX, rbtY);
        }
        return false;
    }

    private boolean lookLeftEmpty(int rbtX, int rbtY){
        switch (robot.getDirection()){
            case UP: //Face UP
                return leftFree(rbtX, rbtY);
            case LEFT: //Face LEFT
                return downFree(rbtX, rbtY);
            case DOWN: //Face DOWN
                return rightFree(rbtX, rbtY);
            case RIGHT: //Face RIGHT
                return upFree(rbtX, rbtY);
        }
        return false;
    }

    private boolean lookRightEmpty(int rbtX, int rbtY){
        switch (robot.getDirection()){
            case UP: //Face UP
                return rightFree(rbtX, rbtY);
            case LEFT: //Face LEFT
                return upFree(rbtX, rbtY);
            case DOWN: //Face DOWN
                return leftFree(rbtX, rbtY);
            case RIGHT: //Face RIGHT
                return downFree(rbtX, rbtY);
        }
        return false;
    }

    private boolean upFree(int rbtX, int rbtY){
        return  isExploredNotObstacle(rbtX - 1, rbtY + 1) &&
                isExploredAndFree(rbtX, rbtY + 1) &&
                isExploredNotObstacle(rbtX + 1, rbtY + 1);
    }

    private boolean downFree(int rbtX, int rbtY){
        return  isExploredNotObstacle(rbtX - 1, rbtY - 1) &&
                isExploredAndFree(rbtX, rbtY - 1) &&
                isExploredNotObstacle(rbtX + 1, rbtY - 1);
    }

    private boolean leftFree(int rbtX, int rbtY){
        return  isExploredNotObstacle(rbtX -1, rbtY - 1) &&
                isExploredAndFree(rbtX -1, rbtY) &&
                isExploredNotObstacle(rbtX - 1, rbtY + 1);
    }

    private boolean rightFree(int rbtX, int rbtY){
        return  isExploredNotObstacle(rbtX + 1, rbtY + 1) &&
                isExploredAndFree(rbtX + 1, rbtY) &&
                isExploredNotObstacle(rbtX + 1, rbtY - 1);
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
            return temp.getIsExplored() && !temp.getIsObstacle() && !temp.getIsVirtualWall();
        }
        return false;
    }

    private void moveBot(DIRECTION direction){
        robot.move(direction);
        Simulator.refresh();
        senseSurrounding();
    }

    private void turnToDirection(DIRECTION target){
        int numOfTurn = Math.abs(robot.getDirection().ordinal() - target.ordinal());
        if (numOfTurn > 2) numOfTurn%=2;
        if (numOfTurn == 1){
            if (DIRECTION.getNext(robot.getDirection()) == target){
                moveBot(DIRECTION.LEFT);
            }
            else {
                moveBot(DIRECTION.RIGHT);
            }
        }
        else if (numOfTurn == 2){
            moveBot(DIRECTION.LEFT);
            moveBot(DIRECTION.LEFT);
        }
    }
}
