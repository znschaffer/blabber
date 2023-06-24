package blabber;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class StyleConfig {
    StyleContext context;

    Style timestampStyle;
    Color timeColor;
    Font timeStampFont;

    Style userStyle;
    Color userColor;
    Font userFont;

    Font messageFont;

    public StyleConfig() {
        context = new StyleContext();
        timestampStyle = context.addStyle("timestamp", null);
        userStyle = context.addStyle("user", null);

        timeColor = Color.RED;
        userColor = Color.BLUE;

        messageFont = new Font("Courier", Font.PLAIN, 12);
        userFont = new Font("Courier", Font.PLAIN, 12);
        timeStampFont = new Font("Courier", Font.BOLD, 12);

    }

    public void applyStyles() {
        StyleConstants.setForeground(this.timestampStyle, this.timeColor);
        StyleConstants.setForeground(this.userStyle, this.userColor);
    }
}