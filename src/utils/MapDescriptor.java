package utils;

import arena.Arena;
import arena.ArenaConstants;

import java.io.*;

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
            String temp = sb.toString();
            int ptr = 0;
//            String binary = "", partial, tempBin;
//            while (temp.length() - ptr > 0){//pointer != inputHex.length()
//                partial = temp.substring(ptr, ptr + 1); //every character in the input
//                tempBin = Integer.toBinaryString(Integer.parseInt(partial,16));
//                for (int i = 0; i < 4 - tempBin.length(); i++) binary = binary.concat("0");
//                binary = binary.concat(tempBin);
//                ptr++;
//            }
            String binary = temp;
            ptr = 0;
            for (int y = ArenaConstants.ROWS-1; y >= 0 ; y--){
                for (int x = 0; x <= ArenaConstants.COLS-1; x++){
                    if(binary.charAt(ptr) == '1')
                        arena.setObstacle(x, y, true);
                    ptr++;
                }
            }
            //set all explored?
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
