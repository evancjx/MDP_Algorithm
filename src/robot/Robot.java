package robot;

// @formatter:off

import arena.Arena;

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

public class Robot {
    private int posX, posY, direction, frontX, frontY;
    public int speed;
    private final Sensor
        SRFrontLeft,
        SRFrontCenter,
        SRFrontRight,
        SRLeft,
        LRLeft,
        SRRight;
    private boolean isAlert;

    public Robot(int posX, int posY, int direction){
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
        SRLeft = new Sensor(this.posX - 1, this.posY + 1, 3,
                "SRL", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);
        LRLeft = new Sensor(this.posX - 1, this.posY, 3,
                "LRL", RbtConstants.SEN_LONG_L, RbtConstants.SEN_LONG_U);
        SRRight = new Sensor(this.posX + 1, this.posY, 4,
                "SRR", RbtConstants.SEN_SHORT_L, RbtConstants.SEN_SHORT_U);

    }

    public int getPosX(){ return this.posX; }
    public int getPosY(){ return this.posY; }
    public int getDirection(){ return this.direction; }
    public int getFrontX(){ return this.frontX; }
    public int getFrontY(){ return this.frontY; }

    public void setRobotFront(int d){
        switch (d){
            case 1: //face up
                this.frontX = posX;
                this.frontY = posY + 1;
                break;
            case 2: //face reverse
                this.frontX = posX;
                this.frontY = posY - 1;
                break;
            case 3: //face left
                this.frontX = posX - 1; //x coord
                frontY = posY;
                break;
            case 4: //face right
                this.frontX = posX + 1; //x coord
                this.frontY = posY;
                break;
        }
    }
    public void setRobotPos(int posX, int posY){
        this.posX = posX;
        this.posY = posY;
    }

    public void setSenors(){
        switch (this.direction){
            case 1: //UP
                SRFrontLeft.setSensor(this.posX - 1, this.posY + 1, 1);
                SRFrontCenter.setSensor(this.posX, this.posY + 1, 1);
                SRFrontRight.setSensor(this.posX + 1, this.posY + 1, 1);
                SRLeft.setSensor(this.posX - 1, this.posY + 1, 3);
                LRLeft.setSensor(this.posX - 1, this.posY, 3);
                SRRight.setSensor(this.posX + 1, this.posY, 4);
                break;
            case 2: //Down
                SRFrontLeft.setSensor(this.posX + 1, this.posY - 1, 2);
                SRFrontCenter.setSensor(this.posX, this.posY - 1, 2);
                SRFrontRight.setSensor(this.posX - 1, this.posY - 1, 2);
                SRLeft.setSensor(this.posX + 1, this.posY - 1, 4);
                LRLeft.setSensor(this.posX + 1, this.posY, 4);
                SRRight.setSensor(this.posX - 1, this.posY, 3);
                break;
            case 3: //Left
                SRFrontLeft.setSensor(this.posX - 1, this.posY - 1, 3);
                SRFrontCenter.setSensor(this.posX - 1, this.posY, 3);
                SRFrontRight.setSensor(this.posX - 1, this.posY + 1, 3);
                SRLeft.setSensor(this.posX - 1, this.posY - 1, 2);
                LRLeft.setSensor(this.posX, this.posY - 1, 2);
                SRRight.setSensor(this.posX, this.posY + 1, 1);
                break;
            case 4: //Right
                SRFrontLeft.setSensor(this.posX + 1, this.posY + 1, 4);
                SRFrontCenter.setSensor(this.posX + 1, this.posY, 4);
                SRFrontRight.setSensor(this.posX + 1, this.posY - 1, 4);
                SRLeft.setSensor(this.posX + 1, this.posY + 1, 1);
                LRLeft.setSensor(this.posX, this.posY + 1, 1);
                SRRight.setSensor(this.posX, this.posY - 1, 2);
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
    public void move(int direction){
        this.move(direction, true);//send move to android
    }
    public void move(int move, boolean sendToAndroid){
        switch (move){
            case 1: //Forward
                switch (this.direction){
                    case 1: //UP
                        this.posY++; break;
                    case 2: //DOWN
                        this.posY--; break;
                    case 3: //LEFT
                        this.posX--; break;
                    case 4: //RIGHT
                        this.posX++; break;
                }
                break;
            case 2: //Reverse
                switch (this.direction) {
                    case 1: //UP
                        this.posY--; break;
                    case 2: //DOWN
                        this.posY++; break;
                    case 3: //LEFT
                        this.posX++; break;
                    case 4: //RIGHT
                        this.posX--; break;
                }
                break;
            case 3: //Turn Left
                switch (this.direction){
                    case 1: //UP
                        this.direction = 3; break;
                    case 2: //DOWN
                        this.direction = 4; break;
                    case 3: //LEFT
                        this.direction = 2; break;
                    case 4: //RIGHT
                        this.direction = 1; break;
                }
                break;
            case 4: //Turn Right
                switch (this.direction){
                    case 1: //UP
                        this.direction = 4; break;
                    case 2: //DOWN
                        this.direction = 3; break;
                    case 3: //LEFT
                        this.direction = 1; break;
                    case 4: //RIGHT
                        this.direction = 2; break;
                }
                break;
        }
        this.setRobotFront(this.direction);
    }

    //Improved Algorithm
    public boolean getIsAlert(){ return this.isAlert; }
    public void setIsAlert(boolean value){ this.isAlert = value;}
}
