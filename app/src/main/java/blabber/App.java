package blabber;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import blabber.Room.*;
import blabber.Room.MessageArea.Message;

public class App {

    public static void main(String[] args) {
        new App();
    }

    public App() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.add(new AppPane());
                frame.pack();
                frame.setSize(400, 500);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class AppPane extends JPanel {

        private JButton startServerButton;
        private JButton startClientButton;
        private JButton shutDownButton;

        public Room room;

        private Socket socket;
        private ReadMessageWorker readWorker;

        private DataInputStream inputStream;
        public DataOutputStream outputStream;

        private ServerSocket serverSocket;

        public AppPane() {
            room = new Room(this);
            add(room);

            startServerButton = new JButton("Start server");
            startClientButton = new JButton("Start client");
            shutDownButton = new JButton("Shutdown");
            shutDownButton.setEnabled(false);

            JPanel actionsPanel = new JPanel();
            actionsPanel.add(startServerButton);
            actionsPanel.add(startClientButton);
            actionsPanel.add(shutDownButton);

            add(actionsPanel, BorderLayout.NORTH);

            startServerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            createServer();
                        }
                    }).start();
                }
            });
            startClientButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            createClient();
                        }
                    }).start();
                }
            });
            shutDownButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shutdown();
                }
            });

        }

        protected void didStartClient() {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        didStartClient();
                    }
                });
                return;
            }
            startServerButton.setEnabled(false);
            startClientButton.setEnabled(false);
            shutDownButton.setEnabled(true);
        }

        protected void createClient() {
            try {
                didStartClient();
                room.messageArea.appendMessage(new Message("Connecting to server"));
                socket = new Socket("localhost", 8080);
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());

                room.messageArea.appendMessage(new Message("Connected to server\n"));

                createMessageWorker();
            } catch (IOException ex) {
            }
        }

        protected void createServer() {
            try {
                didStartServer();
                room.messageArea.appendMessage(new Message("Starting server"));
                serverSocket = new ServerSocket(8080);
                room.messageArea.appendMessage(new Message("Waiting for client"));
                Socket socket = serverSocket.accept();
                room.messageArea.appendMessage(new Message("Client connected\n"));

                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
                createMessageWorker();
            } catch (IOException ex) {

            }
        }

        protected void didStartServer() {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        didStartServer();
                    }
                });
                return;
            }

            startServerButton.setEnabled(false);
            startClientButton.setEnabled(false);
            shutDownButton.setEnabled(true);
        }

        // Message Worker
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

        protected void createMessageWorker() {
            readWorker = new ReadMessageWorker(inputStream, new ReadMessageWorker.MessageListener() {
                @Override
                public void didRecieveMessage(String message) {
                    room.messageArea.appendMessage(new Message(message, "Received"));
                }
            });
            readWorker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    System.out.println(readWorker.getState());
                    if (readWorker.getState() == SwingWorker.StateValue.DONE) {
                        try {
                            readWorker.get();
                        } catch (InterruptedException ex) {
                        } catch (ExecutionException ex) {
                        }
                        shutdown();
                    }
                }
            });
            readWorker.execute();
        }

        protected void shutdown() {
            shutdownServer();
            shutdownSocket();
            room.messageArea.appendMessage(new Message("\nShutdown completed\n"));
            didShutdown();
        }

        protected void shutdownSocket() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                }
            }
        }

        protected void shutdownServer() {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                }
            }
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                }
            }
        }

        protected void didShutdown() {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        didShutdown();
                    }
                });
                return;
            }
            startServerButton.setEnabled(true);
            startClientButton.setEnabled(true);
            shutDownButton.setEnabled(false);
        }

    }
}
