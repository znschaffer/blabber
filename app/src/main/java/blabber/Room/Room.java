package blabber.Room;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import blabber.App.AppPane;
import blabber.Room.MessageArea.MessageArea;

public class Room extends JPanel {
    public MessageArea messageArea;
    public AppPane parent;

    public Room(AppPane app) {
        parent = app;

        setLayout(new BorderLayout());
        messageArea = new MessageArea(this);
        messageArea.addToJPanel(this);
    }

}
