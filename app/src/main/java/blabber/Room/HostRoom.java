package blabber.Room;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import blabber.App;
import blabber.Room.MessageArea.Message;

public class HostRoom extends Room {

    private ArrayList<Message> messages;
    private ArrayList<Connection> connections = new ArrayList<>();
    private ServerSocket serverSocket;

    public HostRoom() {
        // Constructor for Room / UI
        super();

        // Start hosting socket service
        host();
        super.createClientWorker();
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
     * Instantiates a new SwingWorker to asynchryonously check all connected sockets
     * for new messages.
     * 
     */
    protected void createConnectionWorker() {
        ConnectionWorker connectionWorker = new ConnectionWorker();
        connectionWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(connectionWorker.getState());
                if (connectionWorker.getState() == SwingWorker.StateValue.DONE) {
                    try {
                        connectionWorker.get();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // shutdown();
                }
            }
        });
        connectionWorker.execute();
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

            // Add self to connection pool
            Socket selfSocket = new Socket("localhost", 8080);
            connection = new Connection(selfSocket);

            connections.add(connection);
            createHostWorker();
            createConnectionWorker();
            // Add peers as needed

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

    public class HostWorker extends SwingWorker<Void, Message> {

        public AtomicBoolean continueListening;

        public HostWorker() {
            continueListening = new AtomicBoolean(true);
        }

        public void stopListening() {
            continueListening.set(false);
            try {
                serverSocket.close();
            } catch (IOException ex) {
            }
        }

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
            while (continueListening.get()) {
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
            }

            return null;
        }

    }

    public class ConnectionWorker extends SwingWorker<Void, Connection> {
        private AtomicBoolean continueSearching;

        public ConnectionWorker() {
            continueSearching = new AtomicBoolean(true);
        }

        public void stopSearching() {
            continueSearching.set(false);
            try {
                serverSocket.close();
            } catch (IOException ex) {
            }
        }

        @Override
        protected void process(List<Connection> chunks) {
            for (Connection connection : chunks) {
                connections.add(connection);
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            while (continueSearching.get()) {
                try {
                    Socket socket = serverSocket.accept();
                    Connection connection = new Connection(socket);
                    publish(connection);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

    }
}
