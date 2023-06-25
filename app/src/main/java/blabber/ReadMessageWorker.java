package blabber;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingWorker;

import blabber.App.AppPane.Connection;

public class ReadMessageWorker extends SwingWorker<Void, String> {

  public interface MessageListener {

    public void onMessage(String message);
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
  protected void process(List<String> chunks) {
    for (String message : chunks) {
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
      String text = connection.readText();
      publish(text);
    }

    System.out.println("Read is now down...");

    return null;
  }

}
