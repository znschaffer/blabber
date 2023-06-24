package blabber.Room.MessageArea;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class InputField {

    private JTextField input;
    private ActionListener listener;

    public InputField() {
        input = new JTextField(10);
        listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = input.getText();
                sendMessage(text);
            }
        };

        input.addActionListener(listener);
    }

    public JTextField getInput() {
        return input;
    }

    public void setInput(JTextField input) {
        this.input = input;
    }

    private void sendMessage(String text) {
        return;
    };

}