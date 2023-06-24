package blabber.Room;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import blabber.App;
import blabber.Room.MessageArea.Message;
import blabber.Room.MessageArea.MessageArea;

public class Room extends JPanel {
    protected MessageArea messageArea;
    protected Connection connection;

    public Room() {
        setLayout(new BorderLayout());
        messageArea = new MessageArea(connection);
        messageArea.addToJPanel(this);
    }

    public class ClientWorker extends SwingWorker<Void, Message> {

        public AtomicBoolean continueListening;

        public ClientWorker() {
            continueListening = new AtomicBoolean(true);
        }

        public void stopListening() {
            continueListening.set(false);
            try {
                connection.socket.close();
            } catch (IOException ex) {
            }
        }

        @Override
        protected void process(List<Message> chunks) {
            for (Message message : chunks) {
                messageArea.appendMessage(message);
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            while (continueListening.get()) {
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

    /**
     * Instantiates a new SwingWorker to asynchronously check all connected sockets
     * for new messages.
     */
    protected void createClientWorker() {
        ClientWorker clientWorker = new ClientWorker();
        clientWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(clientWorker.getState());
                if (clientWorker.getState() == SwingWorker.StateValue.DONE) {
                    try {
                        clientWorker.get();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // shutdown();
                }
            }
        });
        clientWorker.execute();
    }
}

// if (!EventQueue.isDispatchThread()) {
// EventQueue.invokeLater(new Runnable() {
// @Override
// public void run() {
// appendMessage(type, message);
// }
// });
// return;
// }

// startServerButton.addActionListener(new ActionListener() {
// @Override
// public void actionPerformed(ActionEvent e) {
// new Thread(new Runnable() {
// @Override
// public void run() {
// createServer();
// }
// }).start();
// }
// });

// startClientButton.addActionListener(new ActionListener() {
// @Override
// public void actionPerformed(ActionEvent e) {
// new Thread(new Runnable() {
// @Override
// public void run() {
// String result = JOptionPane.showInputDialog("Enter IP Address:");
// createClient(result);
// }
// }).start();
// }
// });

// shutDownButton.addActionListener(new ActionListener() {
// @Override
// public void actionPerformed(ActionEvent e) {
// shutdown();
// }
// });