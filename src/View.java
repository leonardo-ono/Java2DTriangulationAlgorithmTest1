
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 * mouse left button: set shape point
 * mouse right button: triangulate
 * mouse middle button: clear
 * 
 * @author leonardo
 */
public class View extends JPanel {
    
    private Polygon polygon = new Polygon();
    
    private List<Point> points = new ArrayList<Point>();
    private List<Polygon> triangles = new ArrayList<Polygon>();
    
    private Stroke stroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private Color color = new Color(255, 0, 0, 64);
    private BufferedImage image;
    
    public View() {
        try {
            image = ImageIO.read(getClass().getResource("hadouken_3.png"));
        } catch (IOException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        
        setPreferredSize(new Dimension(600, 480));
        MouseHandler mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 50, 50, image.getWidth() * 4, image.getHeight() * 4, null);
        drawPolygon((Graphics2D) g);
        drawTriangles((Graphics2D) g);
        
    }
    
    private void drawPolygon(Graphics2D g) {
        g.setStroke(stroke);
        for (int p = 0; p < points.size(); p++) {
            Point p1 = points.get(p % points.size());
            Point p2 = points.get((p + 1) % points.size());
            g.setColor(Color.BLUE);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void drawTriangles(Graphics2D g) {
        g.setStroke(stroke);
        for (Polygon triangle : triangles) {
            g.setColor(color);
            g.fillPolygon(triangle);
            g.setColor(Color.RED);
            g.drawPolygon(triangle);
        }
    }
    
    private void triangulatePolygon() {

        boolean clockwise = isClockwise(points);
        int index = 0;
        
        while (points.size() > 2) {
            
            Point p1 = points.get((index + 0) % points.size());
            Point p2 = points.get((index + 1) % points.size());
            Point p3 = points.get((index + 2) % points.size());

            Vec2 v1 = new Vec2(p2.x - p1.x, p2.y - p1.y);
            Vec2 v2 = new Vec2(p3.x - p1.x, p3.y - p1.y);
            double cross = v1.cross(v2);

            Polygon triangle = new Polygon();
            triangle.addPoint(p1.x, p1.y);
            triangle.addPoint(p2.x, p2.y);
            triangle.addPoint(p3.x, p3.y);
            
            //System.out.println("cross = " + cross);
            if (!clockwise && cross >= 0 && validTriangle(triangle, p1, p2, p3, points)) {
                points.remove(p2);
                triangles.add(triangle);
            }
            else if (clockwise && cross <= 0 && validTriangle(triangle, p1, p2, p3, points)) {
                points.remove(p2);
                triangles.add(triangle);
            }
            else {
                index++;
            }

        }
        
        if (points.size() < 3) {
            points.clear();
        }
    }
    
    public boolean validTriangle(Polygon triangle, Point p1, Point p2, Point p3, List<Point> points) {
        for (Point p : points) {
            if (p != p1 && p != p2 && p != p3 && triangle.contains(p)) {
                return false;
            }
        }
        return true;
    }
    
    // very interesting
    // https://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
    // https://en.wikipedia.org/wiki/Shoelace_formula ?
    public boolean isClockwise(List<Point> points) {
        int sum = 0;
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size());
            sum += (p2.x - p1.x) * (p2.y + p1.y);
        }
        return sum >= 0;
    }
    
    public static void main(String[] args) {
        View view = new View();
        JFrame frame = new JFrame();
        frame.add(view);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        view.requestFocus();
    }
    
    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                points.add(new Point(e.getX(), e.getY()));
            }
            else if (SwingUtilities.isRightMouseButton(e)) {
                triangulatePolygon();
            }
            else if (SwingUtilities.isMiddleMouseButton(e)) {
                triangles.clear();
            }
            repaint();
        }
        
    }
    
}
