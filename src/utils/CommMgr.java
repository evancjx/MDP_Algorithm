package utils;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommMgr {

    private static CommMgr _commMgr = null;

    // For communication with the Raspberry-Pi
    private static final String HOST = "192.168.1.1";
    private static final int PORT = 5555;

    public static final String MSG_TYPE_ANDROID = "";
    public static final String MSG_TYPE_RPI = "i";
    public static final String MSG_TYPE_ARDUINO = "a";

    private static Socket _conn = null;

    private BufferedReader reader;
    private BufferedWriter writer;


    //Singleton class is used. Only one CommMgr is present at any time
    private CommMgr() {}

    public static CommMgr getCommMgr() {
        if (_commMgr == null) {
            _commMgr = new CommMgr();
        }
        System.out.println(_commMgr==null);

        return _commMgr;
    }

    public boolean setConnection() {

        try {

            _conn = new Socket(HOST, PORT);

            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(_conn.getOutputStream())));
            reader = new BufferedReader(new InputStreamReader(_conn.getInputStream()));

            // Successful connection, return true
            System.out.println("setConnection() -> Connection established successfully!");

            return true;

        } catch (UnknownHostException e) {
            System.out.println("setConnection() -> Unknown Host Exception");
        } catch (IOException e) {
            System.out.println("setConnection() -> IO Exception");
        } catch (Exception e) {
            System.out.println("setConnection() -> Exception");
            e.printStackTrace();
        }
        finally {
            System.out.println("failed to set connection");
        }

        return false;
    }

    public void closeConnection() {
        try {
            reader.close();
            writer.close();
            if (_conn != null) {
                _conn.close();
                _conn = null;
            }
            System.out.println("connection closed successfully");
        } catch (IOException e) {
            System.out.println("closeConnection() -> IO Exception");
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("closeConnection() -> Null Pointer Exception");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("closeConnection() -> Exception");
            e.printStackTrace();
        }
    }

    public boolean sendMsg(String msg, String msgType) {
        try {
            String outputMsg = msgType + msg;
            System.out.println("Sending out msg: " + outputMsg);
            writer.write(outputMsg);
            writer.flush();
            return true;
        } catch (IOException e) {
            System.out.println("sendMsg() -> IOException");
        } catch (Exception e) {
            System.out.println("sendMsg() -> Exception");
            e.printStackTrace();
        }

        return false;
    }

    public String recvMsg() {
        try {
            String inputMsg = reader.readLine();
            if (inputMsg != null && inputMsg.length() > 0) {
                // Fox debug - print out received msg
                System.out.println("Received message is " + inputMsg);
                return inputMsg;
            }
        } catch (IOException e) {
            System.out.println("recvMsg() -> IO exception");
        } catch (Exception e) {
            System.out.println("recvMsg() -> Exception");
        }

        return null;
    }

    public boolean isConnected() {
        return _conn.isConnected();
    }


}
