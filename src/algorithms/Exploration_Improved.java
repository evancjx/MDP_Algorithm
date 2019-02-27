package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import robot.Robot;
import simulator.Simulator;

import java.util.concurrent.TimeUnit;

public class Exploration_Improved {
    private final Arena explored, arena;
    private final Robot robot;
    private final int coverageLimit, timeLimit;
    private long startTime, endTime;
    private int areaExplored;

    public Exploration_Improved(Arena explored, Arena arena, Robot robot,
               int coverageLimit, int timeLimit){
        this.explored = explored;
        this.arena = arena;
        this.robot = robot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }

    public void execute(){
        System.out.println("Starting exploration...");
        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit* 1000);
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
        do{
            areaExplored = calculateAreaExplored();
            System.out.println("Explored Area: " + areaExplored);

            //Improved Algorithm
            if(robot.getPosX() == 2 && robot.getPosY() < 10 && robot.getDirection() == 3 && areaExplored < 275){
                robot.setIsAlert(true);
            }
            nextMove();

            if(robot.getPosX() == initX && robot.getPosY() ==initY){
                if (areaExplored > 290){
                    break;
                }
            }
            if(true){
                try {
                    TimeUnit.MILLISECONDS.sleep(robot.getSpeed());
                } catch (InterruptedException e) {
                    return;
                }
            }
        } while (areaExplored <= coverageLimit);
        System.out.println("Finished run");
        System.out.println("Explored Area: " + areaExplored);
    }

    private void nextMove(){
        int rbtX = robot.getPosX();
        int rbtY = robot.getPosY();
//        System.out.println("Rbt PosX: " + rbtX + " PosY: " + rbtY);
        if(robot.getIsAlert()){
            System.out.println("Alerted");
            if(lookLeftEmpty(rbtX, rbtY)){
                moveBot(2, rbtX, rbtY);
                if(lookForward(rbtX, rbtY)) moveBot(1, rbtX, rbtY);
            } else if (lookForward(rbtX, rbtY)){
                moveBot(1, rbtX, rbtY);
            } else if (lookRightEmpty(rbtX, rbtY)){
                moveBot(4, rbtX, rbtY);
                if(lookForward(rbtX, rbtY)) moveBot(1, rbtX, rbtY);
            } else
                moveBot(3, rbtX, rbtY);
        }
        else {
            if (lookRightEmpty(rbtX, rbtY)){
                moveBot(4, rbtX, rbtY);
                if(lookForward(rbtX, rbtY)) moveBot(1, rbtX, rbtY);
            } else if (lookForward(rbtX, rbtY)){
                moveBot(1, rbtX, rbtY);
            } else if (lookLeftEmpty(rbtX, rbtY)){
                moveBot(2, rbtX, rbtY);
                if(lookForward(rbtX, rbtY)) moveBot(1, rbtX, rbtY);
            } else {
                moveBot(2, rbtX, rbtY); //Depends on which rotation LEFT or RIGHT is better
                moveBot(2, rbtX, rbtY);
            }
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
//        System.out.println("upFree");
        if (robot.getIsAlert()){
            return  isExploredNotObstacle(rbtX - 1, rbtY + 1) &&
                    isExploredAndFree(rbtX, rbtY + 1) &&
                    isExploredNotObstacle(rbtX + 1, rbtY + 1) &&
                    !movedOver(rbtX, rbtY + 3);
        }
        else {
            return  isExploredNotObstacle(rbtX - 1, rbtY + 1) &&
                    isExploredAndFree(rbtX, rbtY + 1) &&
                    isExploredNotObstacle(rbtX + 1, rbtY + 1);
        }
    }

    private boolean downFree(int rbtX, int rbtY){
//        System.out.println("downFree");
        if(robot.getIsAlert()){
            return  isExploredNotObstacle(rbtX - 1, rbtY - 1) &&
                    isExploredAndFree(rbtX, rbtY - 1) &&
                    isExploredNotObstacle(rbtX + 1, rbtY - 1) &&
                    !movedOver(rbtX, rbtY - 3);
        }
        else {
            return  isExploredNotObstacle(rbtX - 1, rbtY - 1) &&
                    isExploredAndFree(rbtX, rbtY - 1) &&
                    isExploredNotObstacle(rbtX + 1, rbtY - 1);
        }
    }

    private boolean leftFree(int rbtX, int rbtY){
//        System.out.println("leftFree");
        if(robot.getIsAlert()){
            return  isExploredNotObstacle(rbtX -1, rbtY - 1) &&
                    isExploredAndFree(rbtX -1, rbtY) &&
                    isExploredNotObstacle(rbtX - 1, rbtY + 1) &&
                    !movedOver(rbtX - 3, rbtY);
        }
        else {
            return  isExploredNotObstacle(rbtX -1, rbtY - 1) &&
                    isExploredAndFree(rbtX -1, rbtY) &&
                    isExploredNotObstacle(rbtX - 1, rbtY + 1);
        }

    }

    private boolean rightFree(int rbtX, int rbtY){
//        System.out.println("rightFree");
        if(!robot.getIsAlert()) {
            return isExploredNotObstacle(rbtX + 1, rbtY + 1) &&
                    isExploredAndFree(rbtX + 1, rbtY) &&
                    isExploredNotObstacle(rbtX + 1, rbtY - 1);
        }
        else{
            return  isExploredNotObstacle(rbtX + 1, rbtY + 1) &&
                    isExploredAndFree(rbtX + 1, rbtY) &&
                    isExploredNotObstacle(rbtX + 1, rbtY - 1) &&
                    !movedOver(rbtX + 3, rbtY);
        }

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

    private boolean movedOver(int posX, int posY){
        if(explored.checkValidCoord(posX, posY)){
            Cell temp = explored.getCell(posX, posY);
            return temp.getIsMovedOver();
//            System.out.println("PosX: " + posX + " PosY: " + posX + " Explored: " + temp.getIsExplored());
        }
        return false;
    }

    private void moveBot(int movement, int rbtX, int rbtY){
        robot.move(movement);
        if (movement == 1){
            if(!(rbtX > 0 && rbtY > 0 && rbtX <= ArenaConstants.START_X + 1 && rbtY <= ArenaConstants.START_Y + 1)  && !robot.getIsAlert()){
                explored.getCell(rbtX, rbtY).setIsMovedOver(true);
            }
        }
        Simulator.refresh();
        senseSurrounding();
    }

//    goHome();
}
