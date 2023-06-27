package blabber;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingWorker;

import blabber.MessageArea.Message;

public class ReadMessageWorker extends SwingWorker<Void, Message> {

  public interface MessageListener {
    public void onMessage(Message message);
  }

  private Connection connection;
  private AtomicBoolean continueReading;
  private MessageListener listener;

  public ReadMessageWorker(Connection connection, MessageListener listener) {
    this.connection = connection;
    this.listener = listener;
    continueReading = new AtomicBoolean(true);

  }

  @Override
  protected void process(List<Message> chunks) {
    for (Message message : chunks) {
      listener.onMessage(message);
    }
  }

  public void stopReading() {
    continueReading.set(false);
    connection.close();
  }

  @Override
  protected Void doInBackground() throws Exception {
    while (continueReading.get()) {
      try {
        Message message = connection.recieveMessage();
        publish(message);
      } catch (IOException ex) {
        // Stop reading loop when sockets are fried
        stopReading();
      }
    }

    System.out.println("Read is now down...");

    return null;
  }

}
