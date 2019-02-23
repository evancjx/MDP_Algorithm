package simulator;

import algorithms.Exploration;
import arena.Arena;
import robot.Robot;
import robot.RbtConstants;
import utils.MapDescriptor;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.*;

public class Simulator {
    private static JFrame appFrame = null;
    private static JPanel arenaPanel = null;
    private static JPanel btnPanel = null;
    private static Container contentPane;

    private static Arena arena = null;
    private static Arena explored = null;

    private static Robot robot;

    private static int timeLimit = 3600;
    private static int coverageLimit = 300;

    public static void main(String[] args){
        robot = new Robot(RbtConstants.START_X, RbtConstants.START_Y,1);
        createDisplay();
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
        arenaPanel.setBackground(Color.LIGHT_GRAY);


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
        arena.setAllUnexplored();
        explored = new Arena(robot);
        explored.setAllUnexplored();
        arenaPanel.add(arena, "Arena");
        arenaPanel.add(explored, "Explore");

        CardLayout cl = (CardLayout) arenaPanel.getLayout();
        cl.show(arenaPanel,"Arena");
    }

    private static void setupButtons(){
        JButton btnLoad = new JButton("Load Arena");
        standardBtn(btnLoad);
        btnLoad.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File(System.getProperty("user.dir")));

                if(fc.showOpenDialog(appFrame) == JFileChooser.APPROVE_OPTION) {
                    arena.clearArena();
                    File selectedFile = fc.getSelectedFile();
                    MapDescriptor.loadArenaObstacle(arena, selectedFile.getAbsolutePath());
                    System.out.println("Here");
                    appFrame.repaint();
                }
            }
        });
        btnPanel.add(btnLoad);

        class Explore extends SwingWorker<Integer, String>{
            protected Integer doInBackground() throws Exception {
                int x, y;

                x = RbtConstants.START_X;
                y = RbtConstants.START_Y;

                robot.setRobotPos(x, y);
//                explored.repaint();
                appFrame.repaint();

                Exploration exploration;
                exploration = new Exploration(explored, arena, robot, coverageLimit, timeLimit);

                exploration.execute();

                return 111;
            }
        }


        JButton btnExplore = new JButton("Start explore");
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
    }

    private static void standardBtn(JButton btn){
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
    }

    public static void refresh(){
        appFrame.repaint();
    }
}
