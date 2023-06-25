package blabber.MessageArea;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import blabber.App.AppPane;

import java.awt.BorderLayout;
import java.io.IOException;

public class MessageArea {

    private JTextArea messageArea;
    private InputField inputField;

    public AppPane parent;

    // constructor
    public MessageArea(AppPane app) {
        parent = app;
        inputField = new InputField(this);
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
        panel.add(inputField.getInput(), BorderLayout.SOUTH);

        panel.add(messageArea, BorderLayout.WEST);
    }

    public void appendMessage(Message message) {
        messageArea.append("[" + message.timestamp + "] ");
        messageArea.append(message.sender + ": ");
        messageArea.append(message.content + "\n");

        this.inputField.getInput().setText("");
    }

    public void sendOutput(String content) {
        try {
            parent.outputStream.writeUTF(content);
        } catch (IOException e) {
        }
    }

}