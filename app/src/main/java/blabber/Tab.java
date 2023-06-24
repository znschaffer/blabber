package blabber;

import javax.swing.*;

public class Tab {
    JTabbedPane pane;
    Queue queue;

    public Tab() {
        queue = new Queue();
        pane = new JTabbedPane();
    }
}