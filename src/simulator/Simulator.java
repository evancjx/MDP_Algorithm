package simulator;

import algorithms.Exploration;
import algorithms.Exploration_Improved;
import algorithms.FastestPath;
import arena.Arena;
import arena.ArenaConstants;
import robot.Robot;
import robot.RbtConstants;
import robot.RbtConstants.DIRECTION;
import utils.MapDescriptor;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
    private static int robotSpeed = 200; //Number of steps per second

    public static void main(String[] args){
        robot = new Robot(RbtConstants.START_X, RbtConstants.START_Y, DIRECTION.UP);
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
        class Explore extends SwingWorker<Integer, String>{
            protected Integer doInBackground() throws Exception{
                robot.setRobotPos(RbtConstants.START_X, RbtConstants.START_Y);
                robot.setRobotSpeed(robotSpeed);
                explored.repaint();

                Exploration exploration = new Exploration(explored, arena, robot, coverageLimit, timeLimit);
                exploration.execute();

                generateArenaHex(arena);

                return 111;
            }
        }
        class Fastest extends SwingWorker<Integer, String>{
            protected Integer doInBackground() throws Exception{
                robot.setRobotPos(RbtConstants.START_X, RbtConstants.START_Y);
                explored.repaint();

                FastestPath fastestPath = new FastestPath(explored, robot);
                fastestPath.run(RbtConstants.GOAL_X, RbtConstants.GOAL_Y);

                return 222;
            }
        }

        JButton btnLoad = new JButton("Load Arena");
        standardBtn(btnLoad);
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

        JButton btnExplore = new JButton("Explore");
        standardBtn(btnExplore);
        btnExplore.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                CardLayout cl = ((CardLayout) arenaPanel.getLayout());
                cl.show(arenaPanel, "Explore");
                new Explore().execute();
            }
        });
        btnPanel.add(btnExplore);

        JButton btnStop = new JButton("Stop");
        standardBtn(btnStop);
        btnStop.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

            }
        });
//        btnPanel.add(btnStop);

        JButton btnConfig =  new JButton("Config");
        standardBtn(btnConfig);
        btnConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                JDialog configDialog = new JDialog(appFrame, "Config", true);
                configDialog.setSize(400,100);
                configDialog.setLayout(new FlowLayout());
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                configDialog.setLocation(dim.width / 2 - configDialog.getSize().width / 2, dim.height / 2 - configDialog.getSize().height / 2);

                final JTextField tfRobotSpeed = new JTextField(Integer.toString(robotSpeed),2);
                final JTextField tfTimeLimit = new JTextField(Integer.toString(timeLimit),4);
                final JTextField tfCoverageLimit = new JTextField(Integer.toString(coverageLimit), 3);

                JButton btnSaveConfig = new JButton("Save");
                btnSaveConfig.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        super.mousePressed(e);
                        robotSpeed = Integer.parseInt(tfRobotSpeed.getText());
                        timeLimit = Integer.parseInt(tfTimeLimit.getText());
                        coverageLimit = Integer.parseInt(tfCoverageLimit.getText());
                        configDialog.setVisible(false);
                    }
                });

                configDialog.add(new JLabel("Robot Speed: "));
                configDialog.add(tfRobotSpeed);
                configDialog.add(new JLabel("Time Limit: "));
                configDialog.add(tfTimeLimit);
                configDialog.add(new JLabel("Coverage Limit: "));
                configDialog.add(tfCoverageLimit);
                configDialog.add(btnSaveConfig);
                configDialog.setVisible(true);
            }
        });
        btnPanel.add(btnConfig);

        JButton btnFastest = new JButton("Fastest");
        standardBtn(btnFastest);
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
}
