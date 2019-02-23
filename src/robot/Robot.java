package robot;

// @formatter:off
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

    public Robot(int posX, int posY, int direction){
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
        robotFront(direction);
    }
    public void robotFront(int d){
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

    public int getPosX(){ return this.posX; }
    public int getPosY(){ return this.posY; }
    public int getDirection(){ return this.direction; }
    public int getFrontX(){ return this.frontX; }
    public int getFrontY(){ return this.frontY; }
}
