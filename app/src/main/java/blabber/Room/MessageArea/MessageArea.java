package blabber.Room.MessageArea;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import blabber.App;

import javax.swing.JScrollPane;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import blabber.Room.*;

public class MessageArea {

    public JScrollPane main;
    public Connection connection;

    private JTextPane messageArea;
    private InputField inputField;

    // constructor
    public MessageArea(Connection baseConnection) {
        connection = baseConnection;

        inputField = new InputField(this);
        messageArea = new JTextPane();
        main = new JScrollPane(messageArea);
    }

    // getters
    public InputField getInputField() {
        return inputField;
    }

    public JTextPane getMessageArea() {
        return messageArea;
    }

    // methods
    public void addToJPanel(JPanel panel) {
        panel.add(messageArea, BorderLayout.CENTER);
        panel.add(inputField.getInput(), BorderLayout.SOUTH);
    }

    public void onSendMessage(Message message) {
        connection.write(message.content);
        appendMessage(message);
    }

    public void appendMessage(Message message) {

        StyledDocument doc = messageArea.getStyledDocument();

        try {
            doc.insertString(doc.getLength(), "[" + message.timestamp + "] ", null);
            doc.insertString(doc.getLength(), message.sender + ": ", null);
            doc.insertString(doc.getLength(), message.content + "\n", null);
        } catch (BadLocationException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}