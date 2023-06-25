package blabber;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingWorker;

public class ReadMessageWorker extends SwingWorker<Void, String> {

  public interface MessageListener {

    public void didRecieveMessage(String message);
  }

  private DataInputStream dataInputStream;
  private AtomicBoolean continueReading;
  private MessageListener listener;

  public ReadMessageWorker(DataInputStream dataInputStream, MessageListener listener) {
    this.dataInputStream = dataInputStream;
    this.listener = listener;
    continueReading = new AtomicBoolean(true);

  }

  @Override
  protected void process(List<String> chunks) {
    for (String message : chunks) {
      listener.didRecieveMessage(message);
    }
  }

  public void stopReading() {
    continueReading.set(false);
    try {
      dataInputStream.close();
    } catch (IOException ex) {
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
