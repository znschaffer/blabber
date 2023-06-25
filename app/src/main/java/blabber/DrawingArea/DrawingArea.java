package blabber.DrawingArea;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

public class DrawingArea extends JPanel {

  private int squareX = -10;
  private int squareY = -10;
  private int squareW = 5;
  private int squareH = 5;
  private int rounding = 5;

  private JButton clearButton = new JButton("Clear");

  public DrawingArea() {

    this.add(clearButton);
    clearButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        squareX = -10;
        squareY = -10;
        repaint();
      }
    });

    this.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        paintSquare(e.getX(), e.getY());
      }
    });

    addMouseMotionListener(new MouseAdapter() {
      public void mouseDragged(MouseEvent e) {
        paintSquare(e.getX(), e.getY());
      }
    });

  }

  private void paintSquare(int x, int y) {
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
