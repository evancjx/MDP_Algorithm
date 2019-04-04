package algorithms;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import org.json.JSONObject;
import robot.RbtConstants.*;
import robot.Robot;
import simulator.Simulator;
import utils.CommMgr;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static utils.MapDescriptor.generateArenaHex;

public class Exploration {
    private final Arena explored, arena;
    private final Robot robot;
    private final int coverageLimit, timeLimit;
    private long startTime;
    private boolean realRun;

    private int countRight;
    private String checkDirectionLog = "";

    public Exploration(Arena explored, Arena arena, Robot robot, int coverageLimit, int timeLimit, boolean realRun){
        this.explored = explored;
        this.arena = arena;
        this.robot = robot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
        this.realRun = realRun;
    }

    private void senseSurrounding(){
        robot.setSenors();
        robot.sense(explored, arena);
        explored.repaint();
    }

    private int calculateAreaExplored(){
        int result = 0;
        for (Cell[] row : explored.grid)
            for (Cell cell : row)
                if(cell.getIsExplored()) result++;
        return result;
    }

    public void execute(){
        int areaExplored;
        startTime = System.currentTimeMillis(); long endTime = startTime + (timeLimit * 1000);
        System.out.println("Starting exploration...");
        if(realRun) CommMgr.getCommMgr().sendMsg("S",CommMgr.MSG_TYPE_ARDUINO);
        senseSurrounding();
        do{
            areaExplored = calculateAreaExplored();
            //System.out.println("Explored Area: " + areaExplored);
            nextMove();

            Simulator.setExplorationStatus("Current time: " + (System.currentTimeMillis() - startTime) / 1000.0 + "seconds");

            if(realRun){
                String[] mapValues = generateArenaHex(explored);
                CommMgr commMgr = CommMgr.getCommMgr();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("explored", mapValues[0]);
                jsonObject.put("obstacle", mapValues[1]);
                commMgr.sendMsg(jsonObject.toString(), CommMgr.MSG_TYPE_ANDROID);
            }

            if(robot.getPosX() == ArenaConstants.START_X && robot.getPosY() == ArenaConstants.START_Y) break;
            else if (robot.getCalledHome()) break;

        } while (System.currentTimeMillis() <= endTime); //areaExplored <= coverageLimit &&

        goBackStart(areaExplored);

//        if (areaExplored < 300 && !robot.getCalledHome() && System.currentTimeMillis() <= endTime)
//            goToUnexplored();

        String message = "<html>", newLineBreak = "<br/>";
        System.out.println("Exploration complete!");
        message += "Exploration complete!" + newLineBreak;
        areaExplored = calculateAreaExplored();
        message += String.format("%.2f%%", (areaExplored / 300.0) * 100.0) + " Coverage";
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        message += ", " + areaExplored + " Cells" + newLineBreak;
        System.out.println(", " + areaExplored + " Cells");
        message += (System.currentTimeMillis() - startTime) / 1000.0 + " seconds" + newLineBreak;
        System.out.println((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
        message += "</html>";

        Simulator.setExplorationStatus(message);
        //System.out.println("\nMovements:\n" + checkDirectionLog);
    }

    private void nextMove(){
        int rbtX = robot.getPosX(), rbtY = robot.getPosY();
        if (countRight == 5) {
            moveBot(MOVEMENT.LEFT);
            countRight = 0;
        }
        else if (lookRightEmpty(rbtX, rbtY)){
            checkDirectionLog += "Right Empty\n";
            moveBot(MOVEMENT.RIGHT);
            countRight++;
            if(lookForward(rbtX, rbtY)){
                checkDirectionLog += "Forward Empty\n";
                moveBot(MOVEMENT.FORWARD);
            }
        } else if (lookForward(rbtX, rbtY)){
            takeImage(this.robot.getDirection(), rbtX, rbtY);
            checkDirectionLog += "Forward Empty\n";
            countRight = 0;
            moveBot(MOVEMENT.FORWARD);
        } else if (lookLeftEmpty(rbtX, rbtY)){
            takeImage(this.robot.getDirection(), rbtX, rbtY);
            checkDirectionLog += "Left Empty\n";
            countRight = 0;
            moveBot(MOVEMENT.LEFT);
            if(lookForward(rbtX, rbtY)) moveBot(MOVEMENT.FORWARD);
        } else {
            takeImage(this.robot.getDirection(), rbtX, rbtY);
            checkDirectionLog += "Only Back Empty\n";
            countRight = 0;
            moveBot(MOVEMENT.RIGHT); //Depends on which rotation LEFT or RIGHT is better
            moveBot(MOVEMENT.RIGHT);
        }
    }

    private void takeImage(DIRECTION d, int x, int y){
        switch(d) {
            case UP:
                x = x + 2;
                if(x>15) return;
                break;
            case LEFT:
                y = y + 2;
                if(y>20) return;
                break;
            case DOWN:
                x = x - 2;
                if(x<1) return;
                break;
            default:
                y = y - 2;
                if(y<1) return;
        }
        d = DIRECTION.getNext(d);
        int dir = DIRECTION.getInt(d);
        CommMgr comMgr = CommMgr.getCommMgr();
        String s = String.format("%d,%d,%d", x, y, dir);
        comMgr.sendMsg(s, CommMgr.MSG_TYPE_RPI);
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
//        Simulator.printRobotPosition();
//        System.out.println("move: " + movement + "\tcountRight="+countRight);
        robot.move(movement);
        Simulator.refresh();
        senseSurrounding();
    }

    private void turnToDirection(DIRECTION target){
        int numOfTurn = Math.abs(robot.getDirection().ordinal() - target.ordinal());
        if (numOfTurn > 2) numOfTurn%=2;
        if (numOfTurn == 1){
            if (DIRECTION.getNext(robot.getDirection()) == target) moveBot(MOVEMENT.LEFT);
            else moveBot(MOVEMENT.RIGHT);
        }
        else if (numOfTurn == 2){
            moveBot(MOVEMENT.LEFT);
            moveBot(MOVEMENT.LEFT);
        }
    }

    private void goToUnexplored(){
        System.out.println("Checking paths to all unexplored grid");
        ArrayList<Cell> unexploredCells = new ArrayList<>();
        for(int y = ArenaConstants.ROWS; y >= 1; y--)
            for(int x = ArenaConstants.COLS; x >= 1; x--)
                if(!explored.getCell(x, y).getIsExplored())
                    unexploredCells.add(explored.getCell(x, y));
        FastestPath fastestPath = new FastestPath(explored);
        ArrayList<MOVEMENT> fPathUnexplored;
        int incX = 0, incY = 0;
        boolean foundPath = false;
        for(Cell cell: unexploredCells){
            for(int i = 1; i <= 24; i ++){
                switch (i){
                    case 1: //TOP CELL
                        incX = 0; incY = 1;
                        break;
                    case 2: //BOTTOM CELL
                        incX = 0; incY = -1;
                        break;
                    case 3: //LEFT CELL
                        incX = -1; incY = 0;
                        break;
                    case 4: //RIGHT CELL
                        incX = 1; incY = 0;
                        break;
                    case 5: //TOP LEFT CELL
                        incX = -1; incY = 1;
                        break;
                    case 6: //TOP RIGHT CELL
                        incX = 1; incY = 1;
                        break;
                    case 7: //BOTTOM RIGHT CELL
                        incX = 1; incY = -1;
                        break;
                    case 8: //BOTTOM LEFT CELL
                        incX = -1; incY = -1;
                        break;
//                    case 9: //BOTTOM BOTTOM LEFT LEFT CELL
//                        incX = -2; incY = -2;
//                        break;
//                    case 10: //BOTTOM BOTTOM LEFT CELL
//                        incX = -1; incY = -2;
//                        break;
//                    case 11: //BOTTOM BOTTOM CELL
//                        incX = 0; incY = -2;
//                        break;
//                    case 12: //BOTTOM BOTTOM RIGHT CELL
//                        incX = 1; incY = -2;
//                        break;
//                    case 13: //BOTTOM BOTTOM RIGHT RIGHT CELL
//                        incX = 2; incY = -2;
//                        break;
//                    case 14: //BOTTOM RIGHT RIGHT CELL
//                        incX = 2; incY = -1;
//                        break;
//                    case 15: //RIGHT RIGHT CELL
//                        incX = 2; incY = 0;
//                        break;
//                    case 16: //TOP RIGHT RIGHT CELL
//                        incX = 2; incY = 1;
//                        break;
//                    case 17: //TOP TOP RIGHT RIGHT CELL
//                        incX = 2; incY = 2;
//                        break;
//                    case 18: //TOP TOP RIGHT CELL
//                        incX = 1; incY = 2;
//                        break;
//                    case 19: //TOP TOP CELL
//                        incX = 0; incY = 2;
//                        break;
//                    case 20: //TOP TOP LEFT CELL
//                        incX = -1; incY = 2;
//                        break;
//                    case 21: //TOP TOP LEFT LEFT CELL
//                        incX = -2; incY = 2;
//                        break;
//                    case 22: //TOP LEFT LEFT CELL
//                        incX = -2; incY = 1;
//                        break;
//                    case 23: //LEFT LEFT CELL
//                        incX = -2; incY = 0;
//                        break;
//                    case 24: //BOTTOM LEFT LEFT CELL
//                        incX = -2; incY = -1;
//                        break;
                }
                if(!explored.checkWayPointCoord(cell.posX() + incX, cell.posY() + incY)) continue;
                Cell tempCell = explored.getCell(cell.posX() + incX, cell.posY() + incY);
                if(!tempCell.getIsExplored() && tempCell.getIsObstacle() && tempCell.getIsVirtualWall()) continue;
                fPathUnexplored = fastestPath.get(robot, cell.posX() + incX, cell.posY() + incY);
                if(fPathUnexplored != null) {
                    foundPath = true;
                    for(MOVEMENT move: fPathUnexplored){
                        Simulator.setExplorationStatus("Current time: " + (System.currentTimeMillis() - startTime) / 1000.0 + "seconds");
                        moveBot(move);
                    }
                    turnToDirection(DIRECTION.UP);
                    goBackStart(calculateAreaExplored());
                    System.out.println("Returned");
                    turnToDirection(DIRECTION.UP);
                    break;
                }
            }
            if(foundPath) break;
        }
    }

    private void goBackStart(int areaExplored){
        Simulator.printRobotPosition();
        if(!robot.getHasCrossGoal() && areaExplored > 100){
            FastestPath goToGoal = new FastestPath(explored);
            ArrayList<MOVEMENT> movements = goToGoal.get(robot, ArenaConstants.GOAL_X, ArenaConstants.GOAL_Y);
            goToGoal.executeMovements(movements, robot);
        }
        if((robot.getPosX() != ArenaConstants.START_X) && (robot.getPosY() != ArenaConstants.START_Y)){
            System.out.println("Return to Start ZONE");
            FastestPath returnToStart = new FastestPath(explored);
            do{
                ArrayList<MOVEMENT> movements = returnToStart.get(robot, ArenaConstants.START_X, ArenaConstants.START_Y);
                for(MOVEMENT move: movements) {
                    Simulator.setExplorationStatus("Current time: " + (System.currentTimeMillis() - startTime) / 1000.0 + "seconds");
                    int rbtX = robot.getPosX(), rbtY = robot.getPosY();
                    if(move == MOVEMENT.FORWARD && lookForward(rbtX, rbtY)){
                        moveBot(move);
                    }
                    else if (move == MOVEMENT.LEFT && lookLeftEmpty(rbtX, rbtY)){
                        moveBot(move);
                    }
                    else if (move == MOVEMENT.RIGHT && lookRightEmpty(rbtX, rbtY)){
                        moveBot(move);
                    }
                }
            } while (robot.getPosX() != ArenaConstants.START_X && robot.getPosY() != ArenaConstants.START_Y);
        }

        turnToDirection(DIRECTION.UP); //return to UP ward direction
    }
}
