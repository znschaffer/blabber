package blabber.Room.MessageArea;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingWorker;

/**
 * Extends the multi-threaded SwingWorker class, specialized in reading from our
 * input and output streams and doing something with that data.
 * The actual implemenetation is left up to the implemeneted interface
 */
public class MessageWorker extends SwingWorker<Void, String> {

    // All Message Listeners must be able to recieve messages
    public interface MessageListener {
        public void onMessage(String message);
    }

    private DataInputStream dataInputStream;
    private AtomicBoolean continueReading;
    private MessageListener listener;

    public MessageWorker(DataInputStream dataInputStream, MessageListener listener) {
        this.dataInputStream = dataInputStream;
        this.listener = listener;
        continueReading = new AtomicBoolean(true);
    }

    public void stopReading() {
        continueReading.set(false);
        try {
            dataInputStream.close();
        } catch (IOException ex) {
        }
    }

    @Override
    protected void process(List<String> chunks) {
        for (String message : chunks) {
            listener.onMessage(message);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (continueReading.get()) {
            String text = dataInputStream.readUTF();
            publish(text);
        }
        System.out.println("Read is now down...");

        return null;
    }

}
