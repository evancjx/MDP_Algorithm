package robot;

// @formatter:off

import arena.Arena;
import robot.RbtConstants.*;

/**
 * Represents the robot moving in the arena.
 *
 * The robot is represented by a 3 x 3 cell space as below:
 *
 *          ^   ^   ^
 *         SR  SR  SR
 *   < SR [X] [X] [X]
 *   < LR [X] [X] [X] SR >
 *        [X] [X] [X]
 *
 * SR = Short Range Sensor, LR = Long Range Sensor
 *
 *
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
    private boolean isAlert;

    public Robot(int posX, int posY, DIRECTION direction){
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
        setRobotFront(direction);
        this.speed = RbtConstants.SPEED;

        SRFrontLeft = new Sensor(this.posX - 1, this.posY + 1, this.direction,
            "SRFL", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);
        SRFrontCenter = new Sensor(this.posX, this.posY + 1, this.direction,
            "SRFC", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);
        SRFrontRight = new Sensor(this.posX + 1, this.posY + 1, this.direction,
            "SRFC", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);
        SRLeft = new Sensor(this.posX - 1, this.posY + 1, DIRECTION.LEFT,
            "SRL", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);
        LRLeft = new Sensor(this.posX - 1, this.posY, DIRECTION.LEFT,
            "LRL", RbtConstants.SEN_LONG_L, RbtConstants.SEN_LONG_U);
        SRRight = new Sensor(this.posX + 1, this.posY, DIRECTION.RIGHT,
            "SRR", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);

    }

    public int getPosX(){ return this.posX; }
    public int getPosY(){ return this.posY; }
    public DIRECTION getDirection(){ return this.direction; }
    public int getFrontX(){ return this.frontX; }
    public int getFrontY(){ return this.frontY; }
    public int getSpeed(){ return this.speed; }

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
    public void setRobotPos(int posX, int posY){
        this.posX = posX;
        this.posY = posY;
    }
    public void setRobotSpeed(int speed){
        this.speed = 1000/speed;
    }

    public void setSenors(){
        switch (this.direction){
            case UP: //UP
                SRFrontLeft.setSensor(this.posX - 1, this.posY + 1, DIRECTION.UP);
                SRFrontCenter.setSensor(this.posX, this.posY + 1, DIRECTION.UP);
                SRFrontRight.setSensor(this.posX + 1, this.posY + 1, DIRECTION.UP);
                SRLeft.setSensor(this.posX - 1, this.posY + 1, DIRECTION.LEFT);
                LRLeft.setSensor(this.posX - 1, this.posY, DIRECTION.LEFT);
                SRRight.setSensor(this.posX + 1, this.posY, DIRECTION.LEFT);
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
    public int[] sense(Arena explored, Arena arena){
        int[] result = new int[6];

        result[0] = SRFrontLeft.sense(explored, arena);
        result[1] = SRFrontCenter.sense(explored, arena);
        result[2] = SRFrontRight.sense(explored, arena);
        result[3] = SRLeft.sense(explored, arena);
        result[4] = LRLeft.sense(explored, arena);
        result[5] = SRRight.sense(explored, arena);

        return result;
    }
    public void move(DIRECTION direction){
        this.move(direction, true);//send move to android
    }
    public void move(DIRECTION move, boolean sendToAndroid){
        switch (move){
            case UP: //Forward
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
            case DOWN: //Reverse
                switch (this.direction) {
                    case UP: //UP
                        this.posY--; break;
                    case LEFT: //LEFT
                        this.posX++; break;
                    case DOWN: //DOWN
                        this.posY++; break;
                    case RIGHT: //RIGHT
                        this.posX--; break;
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
    }

    //Improved Algorithm
    public boolean getIsAlert(){ return this.isAlert; }
    public void setIsAlert(boolean value){ this.isAlert = value;}
}
