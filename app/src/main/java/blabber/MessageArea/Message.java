package blabber.MessageArea;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Message implements MessageInterface {

    public String content;
    public String sender;
    public String timestamp;
    public Boolean isClear = false;
    public Boolean isDrawing = false;
    public Integer x;
    public Integer y;

    public Message(String content, String sender) {
        this.content = content;
        this.sender = sender;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
    }

    public Message(String content) {
        this.content = content;
        this.sender = "STATUS";
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
    }

    public Message(Integer x, Integer y) {
        this.isDrawing = true;
        this.x = x;
        this.y = y;
    }

    public Message(Boolean isClear) {
        this.isClear = true;
    }
}
