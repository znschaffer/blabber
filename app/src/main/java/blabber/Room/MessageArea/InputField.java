package blabber.Room.MessageArea;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class InputField {

    private JTextField input;
    private ActionListener listener;
    private MessageArea parent;

    // constructor
    public InputField(MessageArea messageArea) {
        parent = messageArea;
        input = new JTextField(10);
        listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Message message = new Message(input.getText(), "Sender");
                parent.appendMessage(message);
            }
        };

        input.addActionListener(listener);
    }

    // getters + setters
    public JTextField getInput() {
        return input;
    }

    public void setInput(JTextField input) {
        this.input = input;
    }

}