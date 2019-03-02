package robot;

import arena.Arena;
import robot.RbtConstants.DIRECTION;

public class Sensor {
    private int posX, posY;
    private DIRECTION direction;
    private final String id;
    private final int lowerLimit;
    private final int upperLimit;

    public Sensor(int posX, int posY, DIRECTION direction, String id, int lowerLimit, int upperLimit){
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
        this.id = id;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public void setSensor(int posX, int posY, DIRECTION direction){
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
    }

    public int sense(Arena exploration, Arena arena){
        switch (this.direction){
            case UP:
                return getSensorVal(exploration, arena, 0, 1);
            case LEFT:
                return getSensorVal(exploration, arena, -1, 0);
            case DOWN:
                return getSensorVal(exploration, arena, 0, -1);
            case RIGHT:
                return getSensorVal(exploration, arena, 1, 0);
        }
        return -1; //Error. Will never happen
    }

    private int getSensorVal(Arena exploration, Arena arena, int incX, int incY){
        if (lowerLimit > 1){
            for(int i = 1; i< this.lowerLimit; i++){
                int x = this.posX + (incX * i);
                int y = this.posY + (incY * i);
                if(!exploration.checkValidCoord(x, y)) return i; //WALL
                if(arena.getCell(x, y).getIsObstacle()) return i; //OBSTACLE
            }
        }

        for(int i = this.lowerLimit; i <= this.upperLimit; i++){
            int x = this.posX + (incX * i);
            int y = this.posY + (incY * i);

            if(!exploration.checkValidCoord(x, y)) return i; //WALL

            exploration.getCell(x, y).setIsExplored(true); //Explored cell

            if (arena.getCell(x, y).getIsObstacle()){
                exploration.setObstacle(x, y, true);
                return i;
            }
        }
        return -1; //Error. Will never happen
    }

    public void senseReal(Arena exploredMap, int sensorVal){
        switch (direction) {
            case UP:
                processSensorVal(exploredMap, sensorVal, 1, 0);
                break;
            case RIGHT:
                processSensorVal(exploredMap, sensorVal, 0, 1);
                break;
            case DOWN:
                processSensorVal(exploredMap, sensorVal, -1, 0);
                break;
            case LEFT:
                processSensorVal(exploredMap, sensorVal, 0, -1);
                break;
        }
    }

    private void processSensorVal(Arena exploredMap, int sensorVal, int rowInc, int colInc) {
        if (sensorVal == 0) return;  // return value for LR sensor if obstacle before lowerRange

        // If above fails, check if starting point is valid for sensors with lowerRange > 1.
        for (int i = 1; i < this.lowerLimit; i++) {
            int row = this.posX + (rowInc * i);
            int col = this.posY + (colInc * i);

            if (!exploredMap.checkValidCoord(row, col)) return;
            if (exploredMap.getCell(row, col).getIsObstacle()) return;
        }

        // Update map according to sensor's value.
        for (int i = this.lowerLimit; i <= this.upperLimit; i++) {
            int row = this.posX + (rowInc * i);
            int col = this.posY + (colInc * i);

            if (!exploredMap.checkValidCoord(row, col)) continue;

            exploredMap.getCell(row, col).setIsExplored(true);

            if (sensorVal == i) {
                exploredMap.setObstacle(row, col, true);
                break;
            }

            // Override previous obstacle value if front sensors detect no obstacle.
            if (exploredMap.getCell(row, col).getIsObstacle()) {
                if (id.equals("SRFL") || id.equals("SRFC") || id.equals("SRFR")) {
                    exploredMap.setObstacle(row, col, false);
                } else {
                    break;
                }
            }
        }
    }


}
