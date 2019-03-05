package robot;

import arena.ArenaConstants;

public class RbtConstants {
    public static final int
        START_X = 2, START_Y = 2,
        WIDTH = 70, HEIGHT = 70,
        X_OFFSET = 10, Y_OFFSET = 20,
        FRONT_SIZE = 10, SPEED = 50,
        FRONT_X_OFFSET = ArenaConstants.X_OFFSET + 10, FRONT_Y_OFFSET = 10,
        SEN_SHORT_L = 1, SEN_SHORT_U = 2,
        SEN_LONG_L = 3, SEN_LONG_U = 4;

    public enum DIRECTION{
        UP, LEFT, DOWN, RIGHT;

        public static DIRECTION getNext(DIRECTION curDir){
            return values()[(curDir.ordinal() + 1) % values().length];
        }

        public static DIRECTION getPrev(DIRECTION curDir){
            return values()[(curDir.ordinal() + values().length - 1) % values().length];
        }

        public static int getInt(DIRECTION direction){
            switch(direction){
                case UP:
                    return 1;
                case DOWN:
                    return 2;
                case LEFT:
                    return 3;
                default:
                    return 4;
            }
        }

    }

    public enum MOVEMENT{
        FORWARD, LEFT, RIGHT, BACKWARD;
        public static char getChar(MOVEMENT m){
            switch(m){
                case FORWARD:
                    return 'F';
                case LEFT:
                    return 'L';
                case BACKWARD:
                    return 'B';
                case RIGHT:
                    return 'R';
                default:
                    return 'E';
            }
        }
    }
}
