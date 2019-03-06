package simulator;

import algorithms.Exploration;
import algorithms.FastestPath;
import arena.Arena;
import arena.ArenaConstants;
import arena.Cell;
import org.json.JSONObject;
import robot.Robot;
import robot.RbtConstants;
import robot.RbtConstants.DIRECTION;
import utils.CommMgr;
import utils.MapDescriptor;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.DirectoryIteratorException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;

import static utils.MapDescriptor.generateArenaHex;

public class Simulator {
    private static JFrame appFrame = null;
    private static JPanel arenaPanel = null, btnPanel = null;
    private static Container contentPane;

    private static Arena arena = null, explored = null;

    private static Robot robot;

    private static int timeLimit = 180;
    private static int coverageLimit = ArenaConstants.ROWS * ArenaConstants.COLS;
    private static int robotSpeed = 2; //Number of steps per second

    private static int wayPointX = 0;
    private static int wayPointY = 0;

    private static ArrayList<RbtConstants.MOVEMENT> fPathWayPoint;
    private static ArrayList<RbtConstants.MOVEMENT> fPathGoal;

    private static boolean  pressedFastest = false;

    public static void main(String[] args){
//        CommMgr commMgr = CommMgr.getCommMgr();
//        commMgr.setConnection(100);
//        Scanner sc = new Scanner(System.in);
//        while(true){
//            System.out.println("enter your command:");
//            System.out.println("1.forward");
//            System.out.println("2.left");
//            System.out.println("3.right");
//            System.out.println("4.receive");
//            int input = sc.nextInt();
//            if(input == 1){
//                commMgr.sendMsg("F",CommMgr.MSG_TYPE_ARDUINO);
//            }
//            else if(input==2){
//                commMgr.sendMsg("L", CommMgr.MSG_TYPE_ARDUINO);
//            }
//            else if(input==3){
//                commMgr.sendMsg("R", CommMgr.MSG_TYPE_ARDUINO);
//            }
//            else{
//                commMgr.recvMsg();
//            }
//        }
//        String tmp = null;
//        while(tmp == null){
//            tmp = CommMgr.getCommMgr().recvMsg();
//        }
//        JSONObject startParameters = new JSONObject(tmp);
//        int[] array = (int[])startParameters.get("robotPosition");
//        wayPointX = array[0];
//        wayPointY = array[1];
//        int directionNum = array[2];
//        DIRECTION direction;
//        switch(directionNum){
//            case 1:
//                direction = DIRECTION.UP;
//                break;
//            case 2:
//                direction = DIRECTION.DOWN;
//                break;
//            case 3:
//                direction = DIRECTION.LEFT;
//                break;
//            case 4:
//                direction = DIRECTION.RIGHT;
//            default:
//                direction = DIRECTION.RIGHT;
//        }
        CommMgr commMgr = CommMgr.getCommMgr();
        commMgr.setConnection();
        robot = new Robot(RbtConstants.START_X, RbtConstants.START_Y, DIRECTION.UP, true);
        createDisplay();
    }

    public static void refresh(){
        appFrame.repaint();
    }
    private static void createDisplay(){
        appFrame = new JFrame();
        appFrame.setTitle("MDP Group 1 Simulator");
        appFrame.setSize(new Dimension(690, 700));
        appFrame.setResizable(false);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        appFrame.setLocation(dim.width / 2 - appFrame.getSize().width / 2, dim.height / 2 - appFrame.getSize().height / 2);

        arenaPanel = new JPanel(new CardLayout());
        btnPanel = new JPanel(new GridLayout());

        contentPane = appFrame.getContentPane();
        contentPane.add(arenaPanel, BorderLayout.CENTER);
        contentPane.add(btnPanel, BorderLayout.PAGE_END);

        setupArena();
        setupButtons();

        appFrame.setVisible(true);
        appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private static void setupArena(){
        arena =  new Arena(robot);
        arena.clearArena();
        explored = new Arena(robot);
        explored.clearArena();
        arenaPanel.add(arena, "Arena");
        arenaPanel.add(explored, "Explore");

        CardLayout cl = (CardLayout) arenaPanel.getLayout();
        cl.show(arenaPanel,"Arena");
    }

    private static void setupButtons(){

        class Fastest extends SwingWorker<Integer, String>{
            protected Integer doInBackground() throws Exception{
                explored.repaint();

                FastestPath fastestPath = new FastestPath(explored);
                if (fPathWayPoint != null && pressedFastest){
                    fastestPath.executeMovements(fPathWayPoint, robot);
                    robot.setDirection(DIRECTION.UP);
                }
                if (fPathGoal != null && pressedFastest){
                    System.out.println("Robot [position: ("+robot.getPosX()+", "+robot.getPosY()+") direction:"+robot.getDirection()+"]");
                    fastestPath.executeMovements(fPathGoal, robot);
                }
                if((wayPointX != 0 || wayPointY !=0) && fPathWayPoint == null && fPathGoal == null){
                    System.out.println("\nCurrent Robot [position: ("+robot.getPosX()+", "+robot.getPosY()+") direction:"+robot.getDirection()+"]");
                    System.out.println("Fastest path to way point and to goal zone:");
                    if(explored.checkValidCoord(wayPointX,wayPointY)){
                        fPathWayPoint = fastestPath.get(robot, wayPointX,wayPointY);
                        robot.setRobotPos(wayPointX, wayPointY);
                        fPathGoal = fastestPath.get(robot, ArenaConstants.GOAL_X, ArenaConstants.GOAL_Y);
                        robot.setRobotPos(ArenaConstants.START_X, ArenaConstants.START_Y);
                        System.out.println("Done producing path movements\nCurrent Robot [position: ("+robot.getPosX()+", "+robot.getPosY()+") direction:"+robot.getDirection()+"]\n\n");
                    }
                }
                else{
                    System.out.println("\nCurrent Robot [position: ("+robot.getPosX()+", "+robot.getPosY()+") direction:"+robot.getDirection()+"]");
                    System.out.println("Fastest path to goal coords:");
                    fPathGoal = fastestPath.get(robot, ArenaConstants.GOAL_X, ArenaConstants.GOAL_Y);
                    System.out.println("Done producing path movements\nCurrent Robot [position: ("+robot.getPosX()+", "+robot.getPosY()+") direction:"+robot.getDirection()+"]\n\n");
                }
                return 222;
            }
        }
        class Explore extends SwingWorker<Integer, String>{
            protected Integer doInBackground() throws Exception{
                robot.setRobotPos(RbtConstants.START_X, RbtConstants.START_Y);
                robot.setRobotSpeed(robotSpeed);
                explored.repaint();

                Exploration exploration = new Exploration(explored, arena, robot, coverageLimit, timeLimit);
                exploration.execute();

                generateArenaHex(arena);

                new Fastest().execute();

                return 111;
            }
        }

        Thread exploreThread = new Thread(new Runnable() {
            @Override
            public void run() {
                new Explore().execute();
            }
        });

        JButton btnLoad = new JButton("Load Arena");
        standardBtn(btnLoad);
        JButton btnExplore = new JButton("Explore");
        standardBtn(btnExplore);
        JButton btnStop = new JButton("Stop");
        standardBtn(btnStop);
        JButton btnConfig =  new JButton("Config");
        standardBtn(btnConfig);
        JButton btnFastest = new JButton("Fastest");
        standardBtn(btnFastest);

        btnLoad.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File(System.getProperty("user.dir")));

                if(fc.showOpenDialog(appFrame) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fc.getSelectedFile();
                    MapDescriptor.loadArenaObstacle(arena, selectedFile.getAbsolutePath());
                    appFrame.repaint();
                }
            }
        });
        btnPanel.add(btnLoad);


        btnExplore.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                CardLayout cl = ((CardLayout) arenaPanel.getLayout());
                cl.show(arenaPanel, "Explore");
//                new Explore().execute();
                exploreThread.run();
            }
        });
        btnPanel.add(btnExplore);


        btnStop.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                robot.setCalledHome(true);
            }
        });
        btnPanel.add(btnStop);

        btnConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                JDialog configDialog = new JDialog(appFrame, "Config", true);
                configDialog.setSize(400,150);
                configDialog.setLayout(new FlowLayout());
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                configDialog.setLocation(dim.width / 2 - configDialog.getSize().width / 2, dim.height / 2 - configDialog.getSize().height / 2);

                final JTextField tfRobotSpeed = new JTextField(Integer.toString(robotSpeed),2);
                final JTextField tfTimeLimit = new JTextField(Integer.toString(timeLimit),4);
                final JTextField tfCoverageLimit = new JTextField(Integer.toString(coverageLimit), 3);
                final JTextField tfWayPointX = new JTextField(Integer.toString(wayPointX),2);
                final JTextField tfWayPointY = new JTextField(Integer.toString(wayPointY),2);

                JPanel tfPanel1 = new JPanel();
                JPanel tfPanel2 = new JPanel();
                JPanel savePanel = new JPanel();

                JButton btnSaveConfig = new JButton("Save");
                btnSaveConfig.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        super.mousePressed(e);
                        robotSpeed = Integer.parseInt(tfRobotSpeed.getText());
                        timeLimit = Integer.parseInt(tfTimeLimit.getText());
                        coverageLimit = Integer.parseInt(tfCoverageLimit.getText());
                        wayPointX = Integer.parseInt(tfWayPointX.getText());
                        wayPointY = Integer.parseInt(tfWayPointY.getText());
                        if(arena.checkValidCoord(wayPointX,wayPointY)){
                            explored.getCell(wayPointX,wayPointY).setWayPoint(true);
                            arena.getCell(wayPointX, wayPointY).setWayPoint(true);
                        }
                        configDialog.setVisible(false);
                        appFrame.repaint();
                    }
                });

                tfPanel1.add(new JLabel("Robot Speed: "));
                tfPanel1.add(tfRobotSpeed);
                tfPanel1.add(new JLabel("Time Limit: "));
                tfPanel1.add(tfTimeLimit);
                tfPanel1.add(new JLabel("Coverage Limit: "));
                tfPanel1.add(tfCoverageLimit);
                tfPanel2.add(new JLabel("WayPoint x coord: "));
                tfPanel2.add(tfWayPointX);
                tfPanel2.add(new JLabel("WayPoint y coord: "));
                tfPanel2.add(tfWayPointY);
                savePanel.add(btnSaveConfig);
                configDialog.add(tfPanel1);
                configDialog.add(tfPanel2);
                configDialog.add(savePanel);
                configDialog.setVisible(true);
            }
        });
        btnPanel.add(btnConfig);

        btnFastest.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                CardLayout cl = ((CardLayout) arenaPanel.getLayout());
                cl.show(arenaPanel, "Explore");
                appFrame.repaint();
                pressedFastest = true;
                new Fastest().execute();
            }
        });
        btnPanel.add(btnFastest);
    }

    private static void standardBtn(JButton btn){
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
    }
}
