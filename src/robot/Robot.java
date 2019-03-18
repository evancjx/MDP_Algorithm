package robot;

import arena.Arena;
import arena.ArenaConstants;
import org.json.JSONObject;
import robot.RbtConstants.*;
import utils.CommMgr;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// @formatter:off
/**
 * Represents the robot moving in the arena.
 * The robot is represented by a 3 x 3 cell space as below:
 *          ^   ^   ^
 *         SR  SR  SR
 *   < SR [X] [X] [X]
 *   < LR [X] [X] [X] SR >
 *        [X] [X] [X]
 *
 * SR = Short Range Sensor, LR = Long Range Sensor
 */
// @formatter:on

public class Robot{
    private int posX, posY, frontX, frontY, speed;
    private DIRECTION direction;
    private final Sensor
        SRFrontLeft,
        SRFrontCenter,
        SRFrontRight,
        SRLeft,
        LRLeft,
        SRRight;
    private boolean isAlert, hasCrossGoal ,calledHome;
    private boolean realRobot, fastestMode;

    public Robot(int posX, int posY, DIRECTION direction, boolean realRobot, boolean fastestMode){
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
        setRobotFront(direction);
        this.speed = RbtConstants.SPEED;
        this.isAlert = false;
        this.calledHome = false;
        this.realRobot = realRobot;
        this.fastestMode = fastestMode;

        SRFrontLeft = new Sensor(this.posX - 1, this.posY + 1, this.direction,
            "SRFL", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);
        SRFrontCenter = new Sensor(this.posX, this.posY + 1, this.direction,
            "SRFC", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);
        SRFrontRight = new Sensor(this.posX + 1, this.posY + 1, this.direction,
            "SRFR", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);
        SRLeft = new Sensor(this.posX - 1, this.posY + 1, DIRECTION.getNext(this.direction),
            "SRL", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);
        LRLeft = new Sensor(this.posX - 1, this.posY, DIRECTION.getNext(this.direction),
            "LRL", RbtConstants.SEN_LONG_L, RbtConstants.SEN_LONG_U);
        SRRight = new Sensor(this.posX + 1, this.posY, DIRECTION.getPrev(this.direction),
            "SRR", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);

    }

    public int getPosX(){ return this.posX; }
    public int getPosY(){ return this.posY; }
    public int getFrontX(){ return this.frontX; }
    public int getFrontY(){ return this.frontY; }
    public int getSpeed(){ return this.speed; }
    public boolean isRealRobot(){
        return realRobot;
    }
    public DIRECTION getDirection(){return this.direction; }
    public Boolean getCalledHome(){ return this.calledHome; }
    public Boolean getHasCrossGoal(){ return this.hasCrossGoal; }

    public void setRobotPos(int posX, int posY){
        this.posX = posX;
        this.posY = posY;
    }
    public void setRobotFront(DIRECTION dir){
        this.direction = dir;
        switch (dir){
            case UP: //face UP
                this.frontX = posX;
                this.frontY = posY + 1;
                break;
            case LEFT: //face LEFT
                this.frontX = posX - 1;
                this.frontY = posY;
                break;
            case DOWN: //face DOWN
                this.frontX = posX;
                this.frontY = posY - 1;
                break;
            case RIGHT: //face right
                this.frontX = posX + 1;
                this.frontY = posY;
                break;
        }
    }
    public void setRobotSpeed(int speed){
        this.speed = 1000/speed;
    }
    public void setRobotExplored(boolean mode) {this.fastestMode = mode; }
    public void setSenors(){
        switch (this.direction){
            case UP: //UP
                SRFrontLeft.setSensor(this.posX - 1, this.posY + 1, DIRECTION.UP);
                SRFrontCenter.setSensor(this.posX, this.posY + 1, DIRECTION.UP);
                SRFrontRight.setSensor(this.posX + 1, this.posY + 1, DIRECTION.UP);
                SRLeft.setSensor(this.posX - 1, this.posY + 1, DIRECTION.LEFT);
                LRLeft.setSensor(this.posX - 1, this.posY, DIRECTION.LEFT);
                SRRight.setSensor(this.posX + 1, this.posY, DIRECTION.RIGHT);
                break;
            case LEFT: //Left
                SRFrontLeft.setSensor(this.posX - 1, this.posY - 1, DIRECTION.LEFT);
                SRFrontCenter.setSensor(this.posX - 1, this.posY, DIRECTION.LEFT);
                SRFrontRight.setSensor(this.posX - 1, this.posY + 1, DIRECTION.LEFT);
                SRLeft.setSensor(this.posX - 1, this.posY - 1, DIRECTION.DOWN);
                LRLeft.setSensor(this.posX, this.posY - 1, DIRECTION.DOWN);
                SRRight.setSensor(this.posX, this.posY + 1, DIRECTION.UP);
                break;
            case DOWN: //Down
                SRFrontLeft.setSensor(this.posX + 1, this.posY - 1, DIRECTION.DOWN);
                SRFrontCenter.setSensor(this.posX, this.posY - 1, DIRECTION.DOWN);
                SRFrontRight.setSensor(this.posX - 1, this.posY - 1, DIRECTION.DOWN);
                SRLeft.setSensor(this.posX + 1, this.posY - 1, DIRECTION.RIGHT);
                LRLeft.setSensor(this.posX + 1, this.posY, DIRECTION.RIGHT);
                SRRight.setSensor(this.posX - 1, this.posY, DIRECTION.LEFT);
                break;
            case RIGHT: //Right
                SRFrontLeft.setSensor(this.posX + 1, this.posY + 1, DIRECTION.RIGHT);
                SRFrontCenter.setSensor(this.posX + 1, this.posY, DIRECTION.RIGHT);
                SRFrontRight.setSensor(this.posX + 1, this.posY - 1, DIRECTION.RIGHT);
                SRLeft.setSensor(this.posX + 1, this.posY + 1, DIRECTION.UP);
                LRLeft.setSensor(this.posX, this.posY + 1, DIRECTION.UP);
                SRRight.setSensor(this.posX, this.posY - 1, DIRECTION.DOWN);
                break;
        }
    }
    public void setCalledHome(Boolean value){ this.calledHome = value; }
    public void setDirection(DIRECTION direction) {
        if(realRobot){
            MOVEMENT changeDirectionMove = MOVEMENT.getNextMovement(this.direction, direction);
            if (changeDirectionMove != null) move(changeDirectionMove);
        }
        else {
            this.direction = direction;
        }
        this.setRobotFront(this.direction);
    }
    private void crossGoal(){
        if(this.getPosX() == ArenaConstants.GOAL_X && this.getPosY() == ArenaConstants.GOAL_Y)
            this.hasCrossGoal = true;
    }

    public int[] sense(Arena explored, Arena arena){
        int[] result = new int[6];
        if(!realRobot){

            result[0] = SRFrontLeft.sense(explored, arena);
            result[1] = SRFrontCenter.sense(explored, arena);
            result[2] = SRFrontRight.sense(explored, arena);
            result[3] = SRLeft.sense(explored, arena);
            result[4] = LRLeft.sense(explored, arena);
            result[5] = SRRight.sense(explored, arena);
        }
        else{
            CommMgr commMgr = CommMgr.getCommMgr();
            String msg = commMgr.recvMsg();
            String[] sensorValues = msg.split(":");
            // Front Center:Front Left: Front Right: RIGHT: Left FRONT: LEFT BACK
            result[0] = Integer.parseInt(sensorValues[0]);
            result[1] = Integer.parseInt(sensorValues[1]);
            result[2] = Integer.parseInt(sensorValues[2]);
            result[3] = Integer.parseInt(sensorValues[3]);
            result[4] = Integer.parseInt(sensorValues[4]);
            result[5] = Integer.parseInt(sensorValues[5]);
            for(int i = 0; i < result.length; i++){
                result[i] = (result[i] + 5)/10;
            }
            System.out.println("========================>");
            System.out.println(Arrays.toString(result));

            SRFrontCenter.senseReal(explored, result[0]);
            SRFrontLeft.senseReal(explored, result[1]);
            SRFrontRight.senseReal(explored, result[2]);
            SRRight.senseReal(explored, result[3]);
            SRLeft.senseReal(explored, result[4]);
            LRLeft.senseReal(explored, result[5]);
        }
        return result;
    }

    public void move(MOVEMENT movement){
        if(!realRobot){
            try {
                TimeUnit.MILLISECONDS.sleep(this.getSpeed());
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.move()!");
            }
        }
        switch (movement){//Changing robot position on display only
            case FORWARD: //Forward
                switch (this.direction){
                    case UP: //UP
                        this.posY++; break;
                    case LEFT: //LEFT
                        this.posX--; break;
                    case DOWN: //DOWN
                        this.posY--; break;
                    case RIGHT: //RIGHT
                        this.posX++; break;
                }
                break;
            case LEFT: //Turn Left
                switch (this.direction){
                    case UP: //UP
                        this.direction = DIRECTION.LEFT; break;
                    case LEFT: //LEFT
                        this.direction = DIRECTION.DOWN; break;
                    case DOWN: //DOWN
                        this.direction = DIRECTION.RIGHT; break;
                    case RIGHT: //RIGHT
                        this.direction = DIRECTION.UP; break;
                }
                break;
            case RIGHT: //Turn Right
                switch (this.direction){
                    case UP: //UP
                        this.direction = DIRECTION.RIGHT; break;
                    case LEFT: //LEFT
                        this.direction = DIRECTION.UP; break;
                    case DOWN: //DOWN
                        this.direction = DIRECTION.LEFT; break;
                    case RIGHT: //RIGHT
                        this.direction = DIRECTION.DOWN; break;
                }
                break;
        }
        this.setRobotFront(this.direction);

        crossGoal();
        if(realRobot){
            System.out.println("Movement: " + movement);
            CommMgr commMgr = CommMgr.getCommMgr();
            commMgr.sendMsg(MOVEMENT.getChar(movement, this.fastestMode)+"", CommMgr.MSG_TYPE_ARDUINO);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("robotPosition", Arrays.asList(posX, posY, DIRECTION.getInt(direction)));
            commMgr.sendMsg(jsonObject.toString(), CommMgr.MSG_TYPE_ANDROID);
        }
    }
    public void moveForwardMultiple(int count){
        switch(direction){
            case UP:
                posY+=count;
                break;
            case LEFT:
                posX-=count;
                break;
            case DOWN:
                posY-=count;
                break;
            default:
                posX+=count;
        }
//        String command = "";
//        for (int i = 0; i < count; i++){
//            command+="F]";
//        }
        CommMgr commMgr = CommMgr.getCommMgr();
        if (count>5){
            while (count > 5){
                commMgr.sendMsg("W"+5*10, CommMgr.MSG_TYPE_ARDUINO);
                count = count - 5;
                if (count <= 5) {
                    commMgr.sendMsg("W" + (count) * 10, CommMgr.MSG_TYPE_ARDUINO);
                    count = 0;
                }
            }
        }
        else{
            commMgr.sendMsg("W"+count*10, CommMgr.MSG_TYPE_ARDUINO);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("robotPosition", Arrays.asList(posX, posY, DIRECTION.getInt(direction)));
        commMgr.sendMsg(jsonObject.toString(), CommMgr.MSG_TYPE_ANDROID);
        while(commMgr.recvMsg()==null){}
    }

    //Improved Algorithm
    public boolean getIsAlert(){ return this.isAlert; }
    public void setIsAlert(boolean value){ this.isAlert = value;}
}
