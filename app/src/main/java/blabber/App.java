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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

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

        public class Connection {
            public DataInputStream inputStream;

            public void setInputStream(DataInputStream inputStream) {
                this.inputStream = inputStream;
            }

            public DataInputStream getInput() {
                return this.inputStream;
            }

            public DataOutputStream outputStream;

            public void setOutput(DataOutputStream outputStream) {
                this.outputStream = outputStream;
            }

            public DataOutputStream getOutput() {
                return this.outputStream;
            }

            public void sendText(String text) {
                try {
                    this.outputStream.writeUTF(text);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            public String readText() {
                try {
                    return this.inputStream.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            public void sendDrawOutput(int x, int y) {
                try {
                    this.outputStream.writeUTF("Draw_" + String.valueOf(x) + "_" + String.valueOf(y));
                } catch (IOException e) {
                }
            }

            public void sendClearOutput() {
                try {
                    this.outputStream.writeUTF("Clear");
                } catch (IOException e) {
                }
            }

            public void close() {
                try {
                    this.inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    this.outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

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
                connection.setInputStream(new DataInputStream(socket.getInputStream()));
                connection.setOutput(new DataOutputStream(socket.getOutputStream()));

                messageArea.appendMessage(new Message("Connected to server\n"));

                createMessageWorker();
            } catch (IOException ex) {
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
                connection.setInputStream(new DataInputStream(socket.getInputStream()));
                connection.setOutput(new DataOutputStream(socket.getOutputStream()));
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

        protected void createMessageWorker() {
            readWorker = new ReadMessageWorker(connection, new ReadMessageWorker.MessageListener() {
                @Override
                public void onMessage(String message) {
                    if (message.startsWith("Draw_")) {
                        String[] msgs = message.split("_");
                        drawArea.paintSquare(Integer.parseInt(msgs[1]), Integer.parseInt(msgs[2]));
                    } else if (message.startsWith("Clear")) {
                        drawArea.clear();
                    } else {
                        messageArea.appendMessage(new Message(message, "Received"));
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
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                }
            }
            if (!readWorker.isDone()) {
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
            if (!readWorker.isDone()) {
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
