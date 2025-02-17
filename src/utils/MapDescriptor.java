package utils;

import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MapDescriptor {
    public static void loadArenaObstacle(Arena arena, String fileLocation){
        try{
            InputStream is = new FileInputStream(fileLocation);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = br.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null){
                sb.append(line);
                line = br.readLine();
            }
            String grid = sb.toString();
            boolean isHEX = false;
            for(char c : grid.toCharArray()){
                if(c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f'){
                    isHEX = true;
                    break;
                }
            }
            int ptr = 0;
            String binary, partial, tempBin;
            if (isHEX){
                binary = "";
                while (grid.length() - ptr > 0){//pointer != inputHex.length()
                    partial = grid.substring(ptr, ptr + 1); //every character in the input
                    tempBin = Integer.toBinaryString(Integer.parseInt(partial,16));
                    for (int i = 0; i < 4 - tempBin.length(); i++) binary = binary.concat("0");
                    binary = binary.concat(tempBin);
                    ptr++;
                }
            }
            else {
                binary = grid;
            }
            ptr = 0;
            for (int y = ArenaConstants.ROWS; y > 0 ; y--){
                for (int x = 1; x <= ArenaConstants.COLS; x++){
                    if(binary.charAt(ptr) == '1')
                        arena.setObstacle(x, y, true);
                    ptr++;
                }
            }
            arena.setAllExplored();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String[] generateArenaHex(Arena arena){
        String[] ArenaDescriptor = new String[2];

        StringBuilder P1_bin = new StringBuilder();
        StringBuilder P1_hex = new StringBuilder();
        StringBuilder P2_bin = new StringBuilder();
        StringBuilder P2_hex = new StringBuilder();

        P1_bin.append("11");

        for(Cell[] row: arena.grid){
            for (Cell cell: row){
                if(cell.getIsExplored()){
                    P1_bin.append("1");
                    if(cell.getIsObstacle()) P2_bin.append("1");
                    else P2_bin.append("0");
                    if(P2_bin.length() == 4){
                        P2_hex.append(toHex(P2_bin.toString()));
                        P2_bin.setLength(0);
                    }
                }
                else{
                    P1_bin.append("0");
                }
                if(P1_bin.length() == 4 ){
                    P1_hex.append(toHex(P1_bin.toString()));
                    P1_bin.setLength(0);
                }
            }
        }
        P1_bin.append("11");
        P1_hex.append(toHex(P1_bin.toString()));
        ArenaDescriptor[0] = P1_hex.toString();
        if(P2_bin.length() > 0) P2_hex.append(toHex(P2_bin.toString()));
        ArenaDescriptor[1] = P2_hex.toString();

        return ArenaDescriptor;
    }

    private static String toHex (String binary){
        return Integer.toHexString(Integer.parseInt(binary, 2));
    }

    public static void writeFile(String[] MD){
        FileWriter fr = null;
        try{
            String path = new File(".").getCanonicalPath();
            DateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmm");
            Date date = new Date();
            fr = new FileWriter(path + "\\MD\\" + dateFormat.format(date) + "_MD.txt");
            StringBuilder sb = new StringBuilder();
            for(String row: MD){
                sb.append(row);
                sb.append("\n");
            }
            fr.write(sb.toString());
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try{
                fr.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

    }
}
