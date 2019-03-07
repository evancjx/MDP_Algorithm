package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import org.json.JSONObject;
import robot.RbtConstants.*;
import robot.Robot;
import simulator.Simulator;
import utils.CommMgr;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static utils.MapDescriptor.generateArenaHex;

public class Exploration {
    private final Arena explored, arena;
    private final Robot robot;
    private final int coverageLimit, timeLimit;
    private long startTime, endTime;
    private boolean realRun;

    public Exploration(Arena explored, Arena arena, Robot robot, int coverageLimit, int timeLimit, boolean realRun){
        this.explored = explored;
        this.arena = arena;
        this.robot = robot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
        this.realRun = realRun;
    }

    public void execute(){
        System.out.println("Starting exploration...");
        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit* 1000);
        if(realRun){
            System.out.println("Doing calibration");
            CommMgr.getCommMgr().sendMsg("C",CommMgr.MSG_TYPE_ARDUINO);
            System.out.println("calibration not done yet!");
            while(true){
                if(CommMgr.getCommMgr().recvMsg().equals("Done")){
                    break;
                }
            }
            System.out.println("done with calibration");
        }

        CommMgr.getCommMgr().sendMsg("S",CommMgr.MSG_TYPE_ARDUINO);
        senseSurrounding();
        System.out.println("done sense Surrounding");
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

            String[] mapValues = generateArenaHex(arena);
            CommMgr commMgr = CommMgr.getCommMgr();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("explored", mapValues[0]);
            jsonObject.put("obstacle", mapValues[1]);
            commMgr.sendMsg(jsonObject.toString(), CommMgr.MSG_TYPE_ANDROID);

            if(robot.getPosX() == initX && robot.getPosY() ==initY && areaExplored > 290) break;
            else if (robot.getCalledHome()) break;

        } while (areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime);

        goBackStart();

        System.out.println("Exploration complete!");
        areaExplored = calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.println(", " + areaExplored + " Cells");
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");


        System.out.println("Finish exploration");
    }

    private void nextMove(){
        int rbtX = robot.getPosX();
        int rbtY = robot.getPosY();
        if(lookRightEmpty(rbtX, rbtY)){
            moveBot(MOVEMENT.RIGHT);
            if(lookForward(rbtX, rbtY)) moveBot(MOVEMENT.FORWARD);
        } else if (lookForward(rbtX, rbtY)){
            moveBot(MOVEMENT.FORWARD);
        } else if (lookLeftEmpty(rbtX, rbtY)){
            moveBot(MOVEMENT.LEFT);
            if(lookForward(rbtX, rbtY)) moveBot(MOVEMENT.FORWARD);
        } else {
            moveBot(MOVEMENT.LEFT); //Depends on which rotation LEFT or RIGHT is better
            moveBot(MOVEMENT.LEFT);
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

    private void moveBot(MOVEMENT movement){
        robot.move(movement);
        Simulator.refresh();
        senseSurrounding();
    }

    private void turnToDirection(DIRECTION target){
        int numOfTurn = Math.abs(robot.getDirection().ordinal() - target.ordinal());
        if (numOfTurn > 2) numOfTurn%=2;
        if (numOfTurn == 1){
            if (DIRECTION.getNext(robot.getDirection()) == target){
                moveBot(MOVEMENT.LEFT);
            }
            else {
                moveBot(MOVEMENT.RIGHT);
            }
        }
        else if (numOfTurn == 2){
            moveBot(MOVEMENT.LEFT);
            moveBot(MOVEMENT.LEFT);
        }
    }

    private void goBackStart(){
        if(!robot.getHasCrossGoal()){
            FastestPath goToGoal = new FastestPath(explored);
            ArrayList<MOVEMENT> movements = goToGoal.get(robot, ArenaConstants.GOAL_X, ArenaConstants.GOAL_Y);
            goToGoal.executeMovements(movements, robot);
        }

        FastestPath returnToStart = new FastestPath(explored);
        ArrayList<MOVEMENT> movements = returnToStart.get(robot, ArenaConstants.START_X, ArenaConstants.START_Y);
        returnToStart.executeMovements(movements, robot);

        turnToDirection(DIRECTION.UP); //return to UP ward direction
    }
}
