package blabber.Room;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import blabber.App;
import blabber.Room.MessageArea.Message;

public class HostRoom extends Room {

    private ArrayList<Message> messages;
    private ArrayList<Connection> connections;
    private ServerSocket serverSocket;

    public HostRoom() {
        // Constructor for Room / UI
        super();

        // Start hosting socket service
        host();
    }

    /**
     * Instantiates a new SwingWorker to asynchryonously check all connected sockets
     * for new messages.
     * 
     */
    protected void createHostWorker() {
        HostWorker hostWorker = new HostWorker();
        hostWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(hostWorker.getState());
                if (hostWorker.getState() == SwingWorker.StateValue.DONE) {
                    try {
                        hostWorker.get();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // shutdown();
                }
            }
        });
        hostWorker.execute();
    }

    /**
     * Starts hosting a socket on localhost port 8080. Keeps watching for connecting
     * sockets and adds them to the connection queue. Starts the hostworker thread
     * as well.
     * 
     */
    protected void host() {
        try {
            serverSocket = new ServerSocket(8080);
            createHostWorker();
            Socket selfSocket = new Socket("localhost", 8080);
            while (true) {
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(socket);
                connections.add(connection);
            }
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends the last message in the queue to all connected peers. Should be changed
     * to log the last one sent and smartly send the 'unread' messages instead. This
     * naive approach should work for now.
     * 
     * @param msg Message to send
     */
    private void broadcast(Message msg) {
        for (Connection connection : connections) {
            try {
                connection.outputStream.writeUTF(msg.content);

            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class HostWorker extends SwingWorker<Void, Message> {

        @Override
        protected void process(List<Message> chunks) {
            for (Message message : chunks) {

                // Queue up message
                messages.add(message);

                // Broadcast it to peers
                broadcast(message);
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            for (Connection connection : connections) {
                try {
                    // Check if inputStream has data waiting
                    if (connection.inputStream.available() != 0) {
                        // Publish that data

                        Message message = new Message(connection.inputStream.readUTF(),
                                connection.socket.getInetAddress().toString());
                        publish(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

    }
}
