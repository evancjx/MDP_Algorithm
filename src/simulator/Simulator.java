package simulator;

import algorithms.Exploration;
import algorithms.FastestPath;
import arena.Arena;
import arena.ArenaConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import robot.Robot;
import robot.RbtConstants;
import robot.RbtConstants.DIRECTION;
import utils.CommMgr;
import utils.MapDescriptor;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

import static robot.RbtConstants.MOVEMENT.*;
import static utils.MapDescriptor.generateArenaHex;

public class Simulator {
    private static JFrame appFrame = null;
    private static JPanel arenaPanel = null, btnPanel = null, textPanel = null;
    private static JLabel explorationStatus, fastestPathStatus;
    private static Container contentPane;

    private static Arena arena = null, explored = null;
    private static Robot robot;
    private static int wayPointX = 0, wayPointY = 0;
    private static ArrayList<RbtConstants.MOVEMENT> fPathWayPoint, fPathGoal;

    private static int arenaExplored;

    private static int coverageLimit = ArenaConstants.ROWS * ArenaConstants.COLS;
    private static int timeLimit = 180, robotSpeed = 20; //Number of steps per second

    private static CommMgr commMgr;
    private static boolean realRun = true;

    public static void main(String[] args){
//        Week 8 FastestPath
//        fPathWayPoint = new ArrayList<>();
//        fPathWayPoint.add(FORWARD);
//        fPathWayPoint.add(FORWARD);
//        fPathWayPoint.add(RIGHT);
//        fPathWayPoint.add(FORWARD);fPathWayPoint.add(FORWARD);fPathWayPoint.add(FORWARD);
//        fPathWayPoint.add(FORWARD);fPathWayPoint.add(FORWARD);fPathWayPoint.add(FORWARD);fPathWayPoint.add(FORWARD);
//        fPathWayPoint.add(LEFT);
//        fPathWayPoint.add(FORWARD);fPathWayPoint.add(FORWARD);fPathWayPoint.add(FORWARD);fPathWayPoint.add(FORWARD);
//        fPathWayPoint.add(FORWARD);
//        fPathWayPoint.add(RIGHT);
//        fPathWayPoint.add(FORWARD);fPathWayPoint.add(FORWARD);
//        fPathGoal = new ArrayList<>();
//        fPathGoal.add(RIGHT);
//        fPathGoal.add(FORWARD);fPathGoal.add(FORWARD);
//        fPathGoal.add(LEFT);
//        fPathGoal.add(FORWARD);fPathGoal.add(FORWARD);fPathGoal.add(FORWARD);fPathGoal.add(FORWARD);fPathGoal.add(FORWARD);
//        fPathGoal.add(FORWARD);fPathGoal.add(FORWARD);fPathGoal.add(FORWARD);fPathGoal.add(FORWARD);fPathGoal.add(FORWARD);
//        fPathGoal.add(RIGHT);
//        fPathGoal.add(FORWARD);

//        fPathWayPoint = new ArrayList<>();
//        fPathWayPoint.add(RIGHT);
//        fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD);
//        fPathWayPoint.add(LEFT);
//        fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD);
//        fPathWayPoint.add(FORWARD);
//        fPathWayPoint.add(LEFT);
//        fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD); fPathWayPoint.add(FORWARD);
//        fPathGoal = new ArrayList<>();
//        fPathGoal.add(RIGHT);
//        fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD);
//        fPathGoal.add(LEFT);
//        fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD);
//        fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD);
//        fPathGoal.add(RIGHT);
//        fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD);
//        fPathGoal.add(FORWARD);
//        fPathGoal.add(LEFT);
//        fPathGoal.add(FORWARD); fPathGoal.add(FORWARD); fPathGoal.add(FORWARD);

        realOrSimulation();
        robot = new Robot(ArenaConstants.START_X, ArenaConstants.START_Y, DIRECTION.UP, realRun,  false);
        createDisplay();
        if(realRun) {
            //Setup communication
            commMgr = CommMgr.getCommMgr();
            if(!commMgr.setConnection()){
                setExplorationStatus("No Connection to RPi");
                return;
            }

            setExplorationStatus("Waiting for Robot position, direction and Way point...");
            while(true){
                //wait for message
                JSONObject jObj = new JSONObject(commMgr.receiveMsg());
                System.out.println(jObj);
                for (int i = 0; i < jObj.names().length(); i++) {
                    switch (jObj.names().get(i).toString()){
                        case "waypoint":
                            JSONArray wayPoint = jObj.getJSONArray("waypoint");
                            wayPointX = wayPoint.getInt(0);
                            wayPointY = wayPoint.getInt(1);
                            break;
                        case "robotPosition":
                            JSONArray robotPositionArr = jObj.getJSONArray("robotPosition");
                            robot.setRobotPos(robotPositionArr.getInt(0),robotPositionArr.getInt(1));
                            robot.setDirection(DIRECTION.getDirection(robotPositionArr.getInt(2)));
                            //Start calibration, send calibrate command
                            setExplorationStatus("Robot doing calibration...");
                            CommMgr.getCommMgr().sendMsg("C",CommMgr.MSG_TYPE_ARDUINO);
                            //Wait for calibration to be done
                            System.out.println("Watiting for calirbation to be done");
                            while(!CommMgr.getCommMgr().receiveMsg().equals("Done"));
                            setExplorationStatus("Done with calibration. Waiting for next command...");
                            break;
                        case "EX_START":
                            //Start exploration if command is sent
                            arenaExplored = 0; //false
                            CardLayout cl = (CardLayout) arenaPanel.getLayout();
                            cl.show(arenaPanel, "Explore");
                            try{
                                arenaExplored = new Explore().doInBackground();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            //wait arena to be explored
                            setExplorationStatus("Waiting for arena to be explored...");
                            while(arenaExplored != 111);
                            //Arena explored, waiting for fastest path calibration command
                            robot.setRobotExplored(true);
                            setFastestPathStatus("<html>" + fastestPathStatus.getText() +
                                    "<br/>Waiting for calibration..." +
                                    "<br/>Please wait for 15 seconds for fastest path calibration");
                            //wait for Fastest path calibration to be done
                            try {
                                TimeUnit.MILLISECONDS.sleep(15000);
                            } catch (InterruptedException e) {
                                System.out.println("Waiting for 15 seconds");
                            }
                            //send fastest path calibration
                            commMgr.sendMsg("X", CommMgr.MSG_TYPE_ARDUINO);
                            while(!commMgr.receiveMsg().equals("FastestPathCalibrationDone"));
                            //wait for next command
                            setExplorationStatus("Done with calibration. Waiting for next command...");
                            break;
                        case "FP_START":
                            try {
                                new Fastest().doInBackground();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }

            //wait for message
//            setExplorationStatus("Waiting for Robot position, direction and Way point...");
//            JSONObject startParameters = receiveJSONobject("robotPosition");
//
//            //Get robot start position and way point coordinates
//            if (startParameters.has("waypoint")){
//                JSONArray wayPoint = (JSONArray) startParameters.get("waypoint");
//                wayPointX = wayPoint.getInt(0);
//                wayPointY = wayPoint.getInt(1);
//            }
//            if (startParameters.has("robotPosition")){
//                JSONArray robotPositionArr = (JSONArray) startParameters.get("robotPosition");
//                robot.setRobotPos(robotPositionArr.getInt(0),robotPositionArr.getInt(1));
//                robot.setDirection(DIRECTION.getDirection(robotPositionArr.getInt(2)));
//            }
//
//            //Start calibration, send calibrate command
//            setExplorationStatus("Robot doing calibration...");
//            CommMgr.getCommMgr().sendMsg("C",CommMgr.MSG_TYPE_ARDUINO);
//
//            //Wait for calibration to be done
//            System.out.println("Watiting for calirbation to be done");
//            while(!CommMgr.getCommMgr().receiveMsg().equals("Done"));
//
//            //wait for message
//            setExplorationStatus("Done with calibration. Waiting for next command...");
//            JSONObject exploreCommand = receiveJSONobject("EX_START");
//
//            //Start exploration if command is sent
//            if (exploreCommand.has("EX_START")) {
//                CardLayout cl = (CardLayout) arenaPanel.getLayout();
//                cl.show(arenaPanel, "Explore");
//                try{
//                    arenaExplored = new Explore().doInBackground();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//
//            //wait arena to be explored
//            setExplorationStatus("Waiting for arena to be explored...");
//            while(arenaExplored != 111);
//
//            //Arena explored, waiting for fastest path calibration command
//            robot.setRobotExplored(true);
//            String currentMsg = fastestPathStatus.getText();
//            setFastestPathStatus("<html>" + currentMsg +
//                    "<br/>Waiting for calibration..." +
//                    "<br/>Please send calibration command from android");
//
//            //wait for Fastest path calibration to be done
//            try {
//                TimeUnit.MILLISECONDS.sleep(15000);
//            } catch (InterruptedException e) {
//                System.out.println("Waiting for 30 seconds before Fastest path calibrate");
//            }
//            commMgr.sendMsg("X", CommMgr.MSG_TYPE_ARDUINO);
//            while(!commMgr.receiveMsg().equals("FastestPathCalibrationDone"));
//            JSONObject fastestCommand = receiveJSONobject("FP_START");
//            if (fastestCommand.has("FP_START")) {
//                try {
//                    new Fastest().doInBackground();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
        else setExplorationStatus("Load Map");
    }

    private static JSONObject receiveJSONobject(String json_key){
        String tmp = null;
        JSONObject jObj = new JSONObject();
        while (!jObj.has(json_key)){
            while (tmp == null) tmp = commMgr.receiveMsg();
            try{
                jObj = new JSONObject(tmp);
            } catch (Exception e){
                setFastestPathStatus("Wrong Command... not in JSON OBJECT?");
            }
            tmp = null;
        }
        return jObj;
    }

    public static void refresh(){
        appFrame.repaint();
    }
    private static void createDisplay(){
        appFrame = new JFrame();
        appFrame.setTitle("MDP Group 1 Simulator");
        if(realRun)
            appFrame.setSize(new Dimension(690, 750));
        else
            appFrame.setSize(new Dimension(690, 750));
        appFrame.setResizable(false);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        appFrame.setLocation(dim.width / 4 - appFrame.getSize().width / 2, dim.height / 2 - appFrame.getSize().height / 2);

        arenaPanel = new JPanel(new CardLayout());
        arenaPanel.setPreferredSize(new Dimension(480, 600));
        textPanel = new JPanel(new GridLayout());
        textPanel.setPreferredSize(new Dimension(500, 10));
        btnPanel = new JPanel(new GridLayout());

        contentPane = appFrame.getContentPane();
        contentPane.add(arenaPanel, BorderLayout.PAGE_START);
        contentPane.add(textPanel, BorderLayout.CENTER);
        contentPane.add(btnPanel, BorderLayout.PAGE_END);

        setupArena();
        setupText();
        if (!realRun) setupActions();

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
    private static void setupText(){
        textPanel.setBackground(Color.lightGray);
        explorationStatus = new JLabel();
        fastestPathStatus = new JLabel();

        textPanel.add(explorationStatus, BorderLayout.NORTH);
        textPanel.add(fastestPathStatus, BorderLayout.SOUTH);
    }

    private static void fastestPath(){
        FastestPath fastestPath = new FastestPath(explored);
        String status;
        if((wayPointX != 0 || wayPointY !=0) && fPathWayPoint == null && fPathGoal == null){
            System.out.println("\nFastest path to way point and to goal zone:");
            if(explored.checkValidCoord(wayPointX,wayPointY)){
                fPathWayPoint = fastestPath.get(robot, wayPointX,wayPointY);
                robot.setRobotPos(wayPointX, wayPointY);
                fPathGoal = fastestPath.get(robot, ArenaConstants.GOAL_X, ArenaConstants.GOAL_Y);
                robot.setRobotPos(ArenaConstants.START_X, ArenaConstants.START_Y);
            }
            status = "Done calculating fastest path, to way point and to goal zone.";
        }
        else{
            System.out.println("\nFastest path to goal coords:");
            fPathGoal = fastestPath.get(robot, ArenaConstants.GOAL_X, ArenaConstants.GOAL_Y);
            status = "Done calculating fastest path, to goal zone.";
        }
        setFastestPathStatus(status);
        System.out.println(status);
    }
    static class Fastest extends SwingWorker<Integer, String> {
        protected Integer doInBackground() throws Exception {
            explored.repaint();

            FastestPath fastestPath = new FastestPath(explored);
            if (fPathWayPoint != null){
                fastestPath.executeMovements(fPathWayPoint, robot);
                printRobotPosition();
                System.out.println("Reached way point");
                if(robot.setDirection(DIRECTION.UP) != null) CommMgr.waitForAcknowledgement("Moved");
                printRobotPosition();
            }
            if (fPathGoal != null){
                fastestPath.executeMovements(fPathGoal, robot);
            }
            return 222;
        }
    }
    static class Explore extends SwingWorker<Integer, String>{
        protected Integer doInBackground() throws Exception{
            robot.setRobotPos(RbtConstants.START_X, RbtConstants.START_Y);
            robot.setRobotSpeed(robotSpeed);
            explored.repaint();

            Exploration exploration = new Exploration(explored, arena, robot, coverageLimit, timeLimit, realRun);
            exploration.execute();

            if(realRun){// FINAL Map Descriptor
                String[] mapValues = generateArenaHex(explored);
                CommMgr commMgr = CommMgr.getCommMgr();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("explored", mapValues[0]);
                jsonObject.put("obstacle", mapValues[1]);
                commMgr.sendMsg(jsonObject.toString(), CommMgr.MSG_TYPE_ANDROID);
            }
            fastestPath();
            return 111;
        }
    }
    private static void setupActions(){
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
                    arenaPanel.removeAll();
                    arena =  new Arena(robot);
                    arena.clearArena();
                    explored = new Arena(robot);
                    explored.clearArena();
                    MapDescriptor.loadArenaObstacle(arena, selectedFile.getAbsolutePath());
                    arenaPanel.add(arena, "Arena");
                    arenaPanel.add(explored, "Explore");
                    appFrame.repaint();
                    setExplorationStatus("<html>Start exploring or input way point at Config.<br/> Click on the button below...</html>");
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
                Simulator.setExplorationStatus("Waiting for arena to be explored...");
                new Explore().execute();
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
                configDialog();
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
                new Fastest().execute();
            }
        });
        btnPanel.add(btnFastest);

    }

    private static void standardBtn(JButton btn){
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
    }

    private static void realOrSimulation(){
        JDialog initialDialog = new JDialog(appFrame, "Config", true);
        initialDialog.setSize(400,80);
        initialDialog.setLayout(new FlowLayout());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        initialDialog.setLocation(dim.width / 2 - initialDialog.getSize().width / 2, dim.height / 2 - initialDialog.getSize().height / 2);

        JRadioButton realRunButton = new JRadioButton("Real Run");
        realRunButton.setSelected(true);
        realRunButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                realRun = true;
                initialDialog.setVisible(false);
            }
        });
        JRadioButton simulationButton = new JRadioButton("Simulation");
        simulationButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                realRun = false;
                initialDialog.setVisible(false);
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(realRunButton);
        group.add(simulationButton);

        initialDialog.add(realRunButton);
        initialDialog.add(simulationButton);
        initialDialog.setVisible(true);
    }

    private static void configDialog(){
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

    public static void printRobotPosition(){
        System.out.println("\nRobot [position: ("+robot.getPosX()+", "+robot.getPosY()+") direction:"+robot.getDirection()+"]");
    }

    public static void setExplorationStatus(String message){
        explorationStatus.setText(message);
    }
    public static void setFastestPathStatus(String message){
        fastestPathStatus.setText(message);
    }
}
