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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

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
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class AppPane extends JPanel {

        private ReadMessageWorker readWorker;

        private DataInputStream inputStream;
        private DataOutputStream outputStream;

        private ServerSocket serverSocket;
        private Socket socket;

        private JTextField messageField;
        private JTextPane messageArea;

        private JButton startServerButton;
        private JButton startClientButton;
        private JButton shutDownButton;

        public AppPane() {
            setLayout(new BorderLayout());

            messageField = new JTextField(10);
            messageArea = new JTextPane();

            messageField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String text = messageField.getText();

                        outputStream.writeUTF(text);
                        appendMessage(MessageType.MESSAGE, new Message(text));
                        messageField.setText(null);
                    } catch (IOException ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(AppPane.this, "Could not send message", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            add(new JScrollPane(messageArea));
            add(messageField, BorderLayout.SOUTH);

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

        protected void createClient() {
            try {
                didStartClient();
                appendMessage(MessageType.STATUS, new Message("Connecting to server"));
                socket = new Socket("localhost", 8080);
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
                appendMessage(MessageType.STATUS, new Message("Connected to server\n"));
                createMessageWorker();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Could not create client socket", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        protected void createServer() {
            try {
                didStartServer();
                appendMessage(MessageType.STATUS, new Message("Starting server"));
                serverSocket = new ServerSocket(8080);
                appendMessage(MessageType.STATUS, new Message("Waiting for client"));
                Socket socket = serverSocket.accept();
                appendMessage(MessageType.STATUS, new Message("Client connected client\n"));
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
                createMessageWorker();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Could not create server socket", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        protected void createMessageWorker() {
            readWorker = new ReadMessageWorker(inputStream, new ReadMessageWorker.MessageListener() {
                @Override
                public void didRecieveMessage(String message) {
                    appendMessage(MessageType.MESSAGE, new Message(message));
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
                            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                            JOptionPane.showMessageDialog(AppPane.this, "Stopped reading due to error", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        shutdown();
                    }
                }
            });
            readWorker.execute();
        }

        protected void appendMessage(MessageType type, Message message) {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        appendMessage(type, message);
                    }
                });
                return;
            }

            StyledDocument doc = messageArea.getStyledDocument();
            StyleConfig style = new StyleConfig();
            style.applyStyles();

            try {
                doc.insertString(doc.getLength(), "[" + message.timestamp + "] ", style.timestampStyle);
                doc.insertString(doc.getLength(), message.sender + ": ", style.userStyle);
                doc.insertString(doc.getLength(), message.content + "\n", null);
            } catch (BadLocationException ex) {

            }
        }

        protected void shutdown() {
            shutdownServer();
            shutdownSocket();
            appendMessage(MessageType.MESSAGE, new Message("\nShutdown completed\n"));
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

    }

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
}