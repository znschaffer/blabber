package blabber;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import blabber.MessageArea.Message;

public class Connection {
    public ObjectInputStream inputStream;
    public ObjectOutputStream outputStream;
    public Boolean closed = false;

    public void setInputStream(ObjectInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Boolean isAlive() {
        // return this.inputStream != null & this.outputStream != null && !this.closed;
        return true;
    }

    public ObjectInputStream getInput() {
        return this.inputStream;
    }

    public void setOutput(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public ObjectOutputStream getOutput() {
        return this.outputStream;
    }

    public void sendMessage(Message message) throws IOException {
        this.outputStream.writeObject(message);
    }

    public Message recieveMessage() throws IOException, ClassNotFoundException {
        return (Message) this.inputStream.readObject();
    }

    public void close() {
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.closed = true;
    }
}