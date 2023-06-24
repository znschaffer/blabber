package blabber.Room.MessageArea;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import blabber.App;

import javax.swing.JScrollPane;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;

import blabber.Room.*;

public class MessageArea {

    public Connection connection;

    private JTextArea messageArea;
    private InputField inputField;

    // constructor
    public MessageArea(Connection baseConnection) {
        connection = baseConnection;

        inputField = new InputField(this);
        messageArea = new JTextArea(20, 10);
        messageArea.setEditable(false);

    }

    // getters
    public InputField getInputField() {
        return inputField;
    }

    public JTextArea getMessageArea() {
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

        messageArea.append("[" + message.timestamp + "] ");
        messageArea.append(message.sender + ": ");
        messageArea.append(message.content + "\n");

        this.inputField.getInput().setText("");

    }

}