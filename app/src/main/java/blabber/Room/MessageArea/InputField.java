package blabber.Room.MessageArea;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import blabber.App;
import blabber.App.AppPane;

import java.awt.event.ActionListener;
import java.io.IOException;
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

                try {
                    parent.parent.parent.outputStream.writeUTF(input.getText());
                    parent.appendMessage(new Message(input.getText(), "Sent"));
                    input.setText(null);
                } catch (IOException ex) {
                }

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