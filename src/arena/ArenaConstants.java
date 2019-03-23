package arena;

public class ArenaConstants {
    public static final int ROWS = 20;      // total num of rows
    public static final int COLS = 15;      // total num of cols
    public static final int SIZE = ROWS * COLS;     // total num of cells
    public static final int START_X = 2, START_Y = 2;
    public static final int GOAL_X = COLS - 1;      // row no. of goal cell
    public static final int GOAL_Y = ROWS - 1;      // col no. of goal cell

    public static final int X_OFFSET = 100;

    public static final int HEIGHT = CellConstants.CELL_SIZE*ArenaConstants.ROWS;
}