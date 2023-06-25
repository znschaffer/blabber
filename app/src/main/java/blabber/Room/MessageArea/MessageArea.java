package blabber.Room.MessageArea;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import blabber.Room.Room;

import java.awt.BorderLayout;

public class MessageArea {

    private JTextArea messageArea;
    private InputField inputField;

    public Room parent;

    // constructor
    public MessageArea(Room room) {

        parent = room;
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

    public void appendMessage(Message message) {

        messageArea.append("[" + message.timestamp + "] ");
        messageArea.append(message.sender + ": ");
        messageArea.append(message.content + "\n");

        this.inputField.getInput().setText("");

    }

}