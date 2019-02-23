package robot;

import arena.Arena;

public class Sensor {
    private int posX, posY, direction;
    private final String id;
    private final int lowerLimit;
    private final int upperLimit;

    public Sensor(int posX, int posY, int direction, String id, int lowerLimit, int upperLimit){
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
        this.id = id;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public void setSensor(int posX, int posY, int direction){
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
    }

    public int sense(Arena exploration, Arena arena){
        switch (this.direction){
            case 1:
                return getSensorVal(exploration, arena, 0, 1);
            case 2:
                return getSensorVal(exploration, arena, 0, -1);
            case 3:
                return getSensorVal(exploration, arena, -1, 0);
            case 4:
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


}
