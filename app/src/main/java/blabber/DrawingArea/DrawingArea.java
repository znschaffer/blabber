package blabber.DrawingArea;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

import blabber.Connection;
import blabber.MessageArea.Message;

public class DrawingArea extends JPanel {

  private int squareX = -10;
  private int squareY = -10;
  private int squareW = 5;
  private int squareH = 5;

  private JButton clearButton = new JButton("Clear");
  private Connection connection;

  public DrawingArea(Connection c) {
    connection = c;
    this.add(clearButton);
    clearButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (connection.isAlive()) {
          clear();
          try {
            connection.sendMessage(new Message(true));
          } catch (IOException e) {
            connection.close();
          }
        }
      }
    });

    this.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (connection.isAlive()) {
          paintSquare(e.getX(), e.getY());
          try {
            connection.sendMessage(new Message(e.getX(), e.getY()));
          } catch (IOException ex) {
            connection.close();
          }
        }
      }
    });

    addMouseMotionListener(new MouseAdapter() {
      public void mouseDragged(MouseEvent e) {
        if (connection.isAlive()) {
          paintSquare(e.getX(), e.getY());
          try {
            connection.sendMessage(new Message(e.getX(), e.getY()));
          } catch (IOException ex) {
            connection.close();
          }
        }
      }
    });

  }

  public void clear() {
    squareX = -10;
    squareY = -10;
    repaint();
  }

  public void paintSquare(int x, int y) {
    if ((squareX != x) || (squareY != y)) {
      squareX = x;
      squareY = y;
      repaint(squareX, squareY, squareW, squareH);
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Color.BLACK);
    g.drawRect(squareX, squareY, squareW, squareH);
    g.fillRect(squareX, squareY, squareW, squareH);
  }

}
