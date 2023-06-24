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

public class MessageArea {

    public JScrollPane main;

    private JTextPane messageArea;
    private InputField inputField;

    // constructor
    public MessageArea() {
        inputField = new InputField();
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
        panel.add(messageArea);
        panel.add(inputField.getInput(), BorderLayout.SOUTH);
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