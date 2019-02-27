package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
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
                    return;
                }
            }
        } while (areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime);
        System.out.println("finish run");
    }

    private void nextMove(){
        int rbtX = robot.getPosX();
        int rbtY = robot.getPosY();
        if(lookRightEmpty(rbtX, rbtY)){
            moveBot(4);
            if(lookForward(rbtX, rbtY)) moveBot(1);
        } else if (lookForward(rbtX, rbtY)){
            moveBot(1);
        } else if (lookLeftEmpty(rbtX, rbtY)){
            moveBot(2);
            if(lookForward(rbtX, rbtY)) moveBot(1);
        } else {
            moveBot(2); //Depends on which rotation LEFT or RIGHT is better
            moveBot(2);
        }
    }

    private boolean lookForward(int rbtX, int rbtY){
        switch (robot.getDirection()){
            case 1: //Face UP
                return upFree(rbtX, rbtY);
            case 2: //Face LEFT
                return leftFree(rbtX, rbtY);
            case 3: //Face DOWN
                return downFree(rbtX, rbtY);
            case 4: //Face RIGHT
                return rightFree(rbtX, rbtY);
        }
        return false;
    }

    private boolean lookLeftEmpty(int rbtX, int rbtY){
        switch (robot.getDirection()){
            case 1: //Face UP
                return leftFree(rbtX, rbtY);
            case 2: //Face LEFT
                return downFree(rbtX, rbtY);
            case 3: //Face DOWN
                return rightFree(rbtX, rbtY);
            case 4: //Face RIGHT
                return upFree(rbtX, rbtY);
        }
        return false;
    }

    private boolean lookRightEmpty(int rbtX, int rbtY){
        switch (robot.getDirection()){
            case 1: //Face UP
                return rightFree(rbtX, rbtY);
            case 2: //Face LEFT
                return upFree(rbtX, rbtY);
            case 3: //Face DOWN
                return leftFree(rbtX, rbtY);
            case 4: //Face RIGHT
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

    private void moveBot(int direction){
        robot.move(direction);
        Simulator.refresh();
        senseSurrounding();
    }
}
