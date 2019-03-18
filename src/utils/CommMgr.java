package utils;

import simulator.Simulator;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommMgr {
    private static CommMgr _commMgr = null;

    // For communication with the Raspberry-Pi
    private static final String HOST = "192.168.1.1";
    private static final int PORT = 5555;

    private static Socket _conn = null;

    private static BufferedOutputStream _bos = null;
    private static OutputStreamWriter _osw = null;
    private static BufferedReader _br = null;

    public static final String
            MSG_TYPE_ANDROID = "b",
            MSG_TYPE_RPI = "i",
            MSG_TYPE_ARDUINO = "a";

    //Singleton class is used. Only one CommMgr is present at any time
    private CommMgr() {}

    public static CommMgr getCommMgr() {
        if (_commMgr == null) {
            _commMgr = new CommMgr();
        }

        return _commMgr;
    }

    public boolean setConnection() {
        try {
            _conn = new Socket();
            _conn.connect(new InetSocketAddress(HOST, PORT));

            _bos = new BufferedOutputStream(_conn.getOutputStream());
            _osw = new OutputStreamWriter(_bos, "US-ASCII");
            _br = new BufferedReader(new InputStreamReader(
                    _conn.getInputStream()));

            // Successful connection, return true
            System.out.println("setConnection() -> Connection established successfully!");

            return true;
        } catch (UnknownHostException e) {
            System.out.println("setConnection() -> Unknown Host Exception");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("setConnection() -> IO Exception");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("setConnection() -> Exception");
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection() {
        try {
            if (_bos != null) _bos.close();
            if (_osw != null) _osw.close();
            if (_br != null) _br.close();

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

            if(_osw==null) _osw = new OutputStreamWriter(_bos, "US-ASCII");
            _osw.write(outputMsg+"|"); // Something requested by rpi to denote end of msg (ability to tokenise msg)
            _osw.flush();
//            System.out.println("Sent out msg: " + outputMsg);
            Simulator.setExplorationStatus("Sent out msg: " + outputMsg);
            return true;
        } catch (IOException e) {
            System.out.println("sendMsg() -> IOException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("sendMsg() -> Exception");
            e.printStackTrace();
        }

        return false;
    }

    public String recvMsg() {
        try {
            for(int i = 0; i < 10; i++){
                String inputMsg = _br.readLine();
                if (inputMsg != null && inputMsg.length() > 0) {
//                    System.out.println("Message received: " + inputMsg);
                    return inputMsg;
                }
            }
        } catch (IOException e) {
            System.out.println("recvMsg() -> IO exception");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("recvMsg() -> Exception");
            e.printStackTrace();
        }

        return null;
    }
}
