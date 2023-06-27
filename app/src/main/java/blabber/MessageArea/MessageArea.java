package blabber.MessageArea;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import blabber.App.AppPane;
import blabber.Connection;

public class MessageArea {

    private JTextArea messageArea;
    private InputField inputField;
    private ActionListener listener;
    public AppPane parent;

    // constructor
    public MessageArea(Connection connection) {
        inputField = new InputField();
        listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connection.isAlive()) {
                    try {
                        connection.sendMessage(new Message(inputField.getText(), "Sent"));
                        appendMessage(new Message(inputField.getText(), "Sent"));
                    } catch (IOException ex) {
                        // bail and close connection if sending fails
                        connection.close();
                        appendMessage(new Message("Failed to send"));
                    }
                } else {
                    appendMessage(new Message("You're not connected to anyone! Get some friends ~"));
                }
                inputField.setText(null);
            }
        };
        inputField.addActionListener(listener);
        messageArea = new JTextArea(20, 15);
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
        panel.add(inputField, BorderLayout.SOUTH);
        panel.add(messageArea, BorderLayout.WEST);
    }

    public void appendMessage(Message message) {
        messageArea.append("[" + message.timestamp + "] ");
        messageArea.append(message.sender + ": ");
        messageArea.append(message.content + "\n");

        this.inputField.setText("");
    }

}