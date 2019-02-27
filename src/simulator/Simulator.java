package simulator;

import algorithms.Exploration;
import algorithms.Exploration_Improved;
import arena.Arena;
import arena.ArenaConstants;
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
    private static JPanel arenaPanel = null, btnPanel = null;
    private static Container contentPane;

    private static Arena arena = null, explored = null;

    private static Robot robot;

    private static int timeLimit = 3600;
    private static int coverageLimit = ArenaConstants.ROWS * ArenaConstants.COLS;

    private static Thread threadExplore;

    public static void main(String[] args){
        robot = new Robot(RbtConstants.START_X, RbtConstants.START_Y,1);
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
        arena.setAllUnexplored();
        explored = new Arena(robot);
        explored.setAllUnexplored();
        arenaPanel.add(arena, "Arena");
        arenaPanel.add(explored, "Explore");

        CardLayout cl = (CardLayout) arenaPanel.getLayout();
        cl.show(arenaPanel,"Arena");
    }

    private static void setupButtons(){
        threadExplore = new Thread(new Runnable() {
            @Override
            public void run() {
//                Exploration_Improved exploration = new Exploration_Improved(explored, arena, robot, coverageLimit, timeLimit);
                Exploration exploration = new Exploration(explored, arena, robot, coverageLimit, timeLimit);
                exploration.execute();
                MapDescriptor.generateArenaHex(explored);
            }
        });

        JButton btnLoad = new JButton("Load Arena");
        standardBtn(btnLoad);
        btnLoad.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File(System.getProperty("user.dir")));

                if(fc.showOpenDialog(appFrame) == JFileChooser.APPROVE_OPTION) {
//                    arenaPanel.remove(arena);
//                    arenaPanel.remove(explored);
//                    arena =  new Arena(robot);
//                    arena.setAllUnexplored();
//                    explored = new Arena(robot);
//                    explored.setAllUnexplored();
//                    arenaPanel.add(arena, "Arena");
//                    arenaPanel.add(explored, "Explore");
                    File selectedFile = fc.getSelectedFile();
                    MapDescriptor.loadArenaObstacle(arena, selectedFile.getAbsolutePath());
                    appFrame.repaint();
                }
            }
        });
        btnPanel.add(btnLoad);

        JButton btnExplore = new JButton("Start explore");
        standardBtn(btnExplore);
        btnExplore.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                CardLayout cl = ((CardLayout) arenaPanel.getLayout());
                cl.show(arenaPanel, "Explore");
//                Exploration.execute();

                threadExplore.start();
            }
        });
        btnPanel.add(btnExplore);

        JButton btnStop = new JButton("Stop");
        standardBtn(btnStop);
        btnStop.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (threadExplore!=null){
                    threadExplore.interrupt();
                }
            }
        });
        btnPanel.add(btnStop);
    }

    private static void standardBtn(JButton btn){
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
    }
}
