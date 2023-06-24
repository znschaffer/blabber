package blabber.Room;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import blabber.App;

public class ConnectedRoom extends Room {
    private Socket socket;
    DataInputStream inputStream;
    DataOutputStream outputStream;

    public ConnectedRoom() {

    }

    protected void connect(String addr) {
        try {
            socket = new Socket(addr, 8080);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            createMessageWorker();
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
