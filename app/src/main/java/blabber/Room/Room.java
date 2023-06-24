package blabber.Room;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import blabber.App;
import blabber.Room.MessageArea.*;
import blabber.Room.MessageArea.MessageType;

public class Room extends JPanel {
    protected MessageArea messageArea;

    public Room() {
        setLayout(new BorderLayout());

        messageArea = new MessageArea();
        messageArea.addToJPanel(this);

        // startServerButton.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // new Thread(new Runnable() {
        // @Override
        // public void run() {
        // createServer();
        // }
        // }).start();
        // }
        // });

        // startClientButton.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // new Thread(new Runnable() {
        // @Override
        // public void run() {
        // String result = JOptionPane.showInputDialog("Enter IP Address:");
        // createClient(result);
        // }
        // }).start();
        // }
        // });

        // shutDownButton.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // shutdown();
        // }
        // });
    }

    protected void appendMessage(MessageType type, Message message) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    appendMessage(type, message);
                }
            });
            return;
        }

        messageArea.appendMessage(message);
    }
}
