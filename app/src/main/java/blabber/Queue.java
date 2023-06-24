package blabber;

import java.util.ArrayList;

public class Queue {
    ArrayList<Message> messages;

    public Queue() {
        messages = new ArrayList<Message>();
    }

    public void submit(String content, String sender) {
        this.messages.add(new Message(content, sender));
    }

}
