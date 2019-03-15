package robot;

import arena.ArenaConstants;

public class RbtConstants {
    public static final int
        START_X = 2, START_Y = 2,
        WIDTH = 70, HEIGHT = 70,
        X_OFFSET = 10, Y_OFFSET = 20,
        FRONT_SIZE = 10, SPEED = 50,
        FRONT_X_OFFSET = ArenaConstants.X_OFFSET + 10, FRONT_Y_OFFSET = 10,
        SEN_SHORT_L = 1, SEN_SHORT_U = 3,
        SEN_LONG_L = 4, SEN_LONG_U = 6;

    public enum DIRECTION{
        UP, LEFT, DOWN, RIGHT;

        public static DIRECTION getNext(DIRECTION curDir){ return values()[(curDir.ordinal() + 1) % values().length]; }

        public static DIRECTION getPrev(DIRECTION curDir){ return values()[(curDir.ordinal() + values().length - 1) % values().length]; }

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

        public static DIRECTION getDirection(int d){
            switch(d){
                case 1:
                    return UP;
                case 2:
                    return DOWN;
                case 3:
                    return LEFT;
                case 4:
                    return RIGHT;
                default:
                    return UP;
            }
        }

    }

    public enum MOVEMENT{
        FORWARD, LEFT, RIGHT, BACKWARD, CALIBRATE;
        public static char getChar(MOVEMENT m, boolean fastestMode){
            switch(m){
                case FORWARD:
                    if(fastestMode) return 'W';
                    else return 'F';
                case LEFT:
                    if(fastestMode) return 'A';
                    else return 'L';
                case BACKWARD:
                    return 'B';
                case RIGHT:
                    if(fastestMode) return 'D';
                    else return 'R';
                case CALIBRATE:
                    if(fastestMode) return 'X';
                    else return 'C';
                default:
                    return 'E';
            }
        }
        public static MOVEMENT getNextMovement(DIRECTION from, DIRECTION to){
            switch (from){
                case UP:
                    switch (to){
                        // no case UP
                        case LEFT:
                            return MOVEMENT.LEFT;
                        case DOWN:
                            return MOVEMENT.LEFT;
                        case RIGHT:
                            return MOVEMENT.RIGHT;
                    }
                    break;
                case LEFT:
                    switch (to){
                        case UP:
                            return MOVEMENT.RIGHT;
                        // no case LEFT
                        case DOWN:
                            return MOVEMENT.LEFT;
                        case RIGHT:
                            return MOVEMENT.LEFT;
                    }
                    break;
                case DOWN:
                    switch (to){
                        case UP:
                            return MOVEMENT.LEFT;
                        case LEFT:
                            return MOVEMENT.RIGHT;
                        // no case DOWN
                        case RIGHT:
                            return MOVEMENT.LEFT;
                    }
                    break;
                case RIGHT:
                    switch (to){
                        case UP:
                            return MOVEMENT.LEFT;
                        case LEFT:
                            return MOVEMENT.LEFT;
                        case DOWN:
                            return MOVEMENT.RIGHT;
                    }

            }
            return null;
        }
    }
}
