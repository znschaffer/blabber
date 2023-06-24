package blabber;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import blabber.Room.HostRoom;
import blabber.Storage.Storage;

public class App {

    public static void main(String[] args) {
        new App();
    }

    public App() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.add(new AppPane());
                frame.pack();
                frame.setSize(400, 500);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class AppPane extends JPanel {

        private JTabbedPane tabPane;
        private Storage storage;

        public AppPane() {
            tabPane = new JTabbedPane();
            tabPane.add("Host", new HostRoom());
            add(tabPane);
        }

    }

}