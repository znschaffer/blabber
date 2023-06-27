package blabber;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import blabber.DrawingArea.DrawingArea;
import blabber.MessageArea.Message;
import blabber.MessageArea.MessageArea;

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
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }

    public class AppPane extends JPanel {

        private JButton startServerButton;
        private JButton startClientButton;
        private JButton shutDownButton;

        public MessageArea messageArea;
        public DrawingArea drawArea;

        private Socket socket;
        private ReadMessageWorker readWorker;

        public Connection connection;

        private ServerSocket serverSocket;

        public AppPane() {
            this.setLayout(new BorderLayout());

            this.connection = new Connection();
            drawArea = new DrawingArea(connection);

            startServerButton = new JButton("Start server");
            startClientButton = new JButton("Start client");
            shutDownButton = new JButton("Shutdown");
            shutDownButton.setEnabled(false);

            JPanel actionsPanel = new JPanel();
            actionsPanel.add(startServerButton);
            actionsPanel.add(startClientButton);
            actionsPanel.add(shutDownButton);

            messageArea = new MessageArea(connection);

            add(actionsPanel, BorderLayout.NORTH);
            messageArea.addToJPanel(this);
            add(drawArea, BorderLayout.CENTER);

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
                messageArea.appendMessage(new Message("Connecting to server"));
                socket = new Socket("localhost", 8080);
                connection.setOutput(new ObjectOutputStream(socket.getOutputStream()));
                connection.setInputStream(new ObjectInputStream(socket.getInputStream()));

                messageArea.appendMessage(new Message("Connected to server\n"));

                createMessageWorker();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        protected void createServer() {
            try {
                didStartServer();
                messageArea.appendMessage(new Message("Starting server"));
                serverSocket = new ServerSocket(8080);
                messageArea.appendMessage(new Message("Waiting for client"));
                Socket socket = serverSocket.accept();
                messageArea.appendMessage(new Message("Client connected\n"));
                connection.setOutput(new ObjectOutputStream(socket.getOutputStream()));
                connection.setInputStream(new ObjectInputStream(socket.getInputStream()));
                createMessageWorker();
            } catch (IOException ex) {
                ex.printStackTrace();

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

        protected void createMessageWorker() {
            readWorker = new ReadMessageWorker(connection, new ReadMessageWorker.MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if (message.isDrawing) {
                        drawArea.paintSquare(message.x, message.y);
                    } else if (message.isClear) {
                        drawArea.clear();
                    } else {
                        messageArea.appendMessage(message);
                    }
                }
            });
            readWorker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    System.out.println(readWorker.getState());
                    if (readWorker.getState() == SwingWorker.StateValue.DONE) {
                        shutdown();
                    }
                }
            });
            readWorker.execute();
        }

        protected void shutdown() {
            shutdownServer();
            shutdownSocket();
            messageArea.appendMessage(new Message("\nShutdown completed\n"));
            didShutdown();
        }

        protected void shutdownSocket() {
            connection.close();
            if (readWorker != null && !readWorker.isDone()) {
                readWorker.stopReading();
            }
        }

        protected void shutdownServer() {
            connection.close();
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                }
            }
            if (readWorker != null && !readWorker.isDone()) {
                readWorker.stopReading();
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
