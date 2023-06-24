package blabber.Room;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import blabber.App;

public class Connection {
    Socket socket;
    DataInputStream inputStream;
    DataOutputStream outputStream;

    public Connection(Socket socket) {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    // methods

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String text) {
        try {
            this.outputStream.writeUTF(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}