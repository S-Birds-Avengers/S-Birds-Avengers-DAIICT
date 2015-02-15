
package ab.learn;

import ab.vision.ABObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class VectorQuantizer {

    /**
     *While processing th obatined results in nested loops take care to use the outer loop to change [][*][]
     *and inner loop to change [*][][] in vector for visual accuracy
     */

    private List<ABObject> _pigs;
    private List<ABObject> _wood;
    private List<ABObject> _ice;
    private List<ABObject> _stone;
    private List<ABObject> TNT;
    private ArrayList<Point> _pigPoints;
    private ArrayList<Point> _woodPoints;
    private ArrayList<Point> _icePoints;
    private ArrayList<Point> _stonePoints;
    private ArrayList<Point> TNTPoints;
    private ArrayList<Point> _allPoints;
    private int leftX;
    private int rightX;
    private int topY;
    private int bottomY;

    private int LENGTH =20;
    private double[][][] _vector;

    public VectorQuantizer(List<ABObject> pigs, List<ABObject> wood, List<ABObject> ice, List<ABObject> stone,List<ABObject> TNT) {
        this._pigs = pigs;
        this._wood = wood;
        this._ice = ice;
        this._stone = stone;
        this.TNT = TNT;
        this._vector = new double[LENGTH][LENGTH][5];
    }

    /**
     * Returns the structure from the block information.
     */
    public Rectangle getBoundingStructure() {
        _pigPoints = decomposeToPoints(_pigs);
        _woodPoints = decomposeToPoints(_wood);
        _icePoints = decomposeToPoints(_ice);
        _stonePoints = decomposeToPoints(_stone);
        TNTPoints = decomposeToPoints(TNT);
        _allPoints = new ArrayList<Point>(0);
        _allPoints.addAll(_pigPoints);
        _allPoints.addAll(_woodPoints);
        _allPoints.addAll(_icePoints);
        _allPoints.addAll(_stonePoints);
        _allPoints.addAll(TNTPoints);
        // Find the bounding rectangle
        int minX = 10000;
        int maxX = 0;
        int minY = 10000;
        int maxY = 0;

        for(Point p : _allPoints) {
            int x = (int)p.getX();
            int y = (int)p.getY();
            if(x < minX) minX = x;
            if(x > maxX) maxX = x;
            if(y < minY) minY = y;
            if(y > maxY) maxY = y;
        }

        leftX = minX;
        rightX = maxX;
        topY = minY;
        bottomY = maxY;

//        System.out.println("xmin - "+minX+", xmax - "+maxX+", ymin - "+minY+", ymax - "+maxY);
        return new Rectangle(minX, minY, (maxX - minX + 1), (maxY - minY + 1));
    }
    public int getMinX() {
        return leftX;
    }

    /**
     * Compute density of each block type
     */
    public double[][][] quantize(Rectangle structure) {
        _vector = new double[LENGTH][LENGTH][5];
        addToVector(structure, _pigPoints, 0);
        addToVector(structure, _woodPoints, 1);
        addToVector(structure, _icePoints, 2);
        addToVector(structure, _stonePoints, 3);
        addToVector(structure,TNTPoints,4);
        normalize(structure);
        return _vector;
    }

    public void normalize(Rectangle structure) {
        int blockPixels = (int)(structure.getWidth()*structure.getHeight()/(LENGTH*LENGTH));
        for (int i = 0; i < LENGTH; i++)
            for (int j = 0; j < LENGTH; j++)
                for (int k = 0; k < 5; k++)
                    _vector[i][j][k] = _vector[i][j][k]*100.0/blockPixels;
    }



    private void addToVector(Rectangle structure, List<Point> object, int index) {
        int leftX = (int)structure.getX();
        int topY = (int)structure.getY();
//        System.out.println("topY - "+topY);
        for (Point p: object) {
            int x = (int)((p.getX() - leftX)*LENGTH/structure.getWidth());
            int y = (int)((p.getY() - topY)*LENGTH/structure.getHeight());
            if (x >= LENGTH) x = LENGTH-1;
            if (y >= LENGTH) y = LENGTH-1;
            _vector[x][y][index]++;
        }
    }

    public static ArrayList<Point> decomposeToPoints(List<ABObject> object) {
        ArrayList<Point> objectPoints = new ArrayList<Point>(0);
        for (Rectangle r:object) {
            int x = (int)r.getX();
            int y = (int)r.getY();
            int width = (int)r.getWidth();
            int height = (int)r.getHeight();
            for (int i = x; i < x+width; i++)
                for (int j = y; j < y+height; j++)
                    objectPoints.add(new Point(i,j));
        }
        return objectPoints;
    }

    public void drawGrid(BufferedImage canvas, Color bgColor) {
        Graphics2D g2d = canvas.createGraphics();
        g2d.setColor(bgColor);

        int changeAlongX = (int)((rightX - leftX)/4) + 1;
        int changeAlongY = (int)((bottomY - topY)/4) + 1;
        for(int i=topY;i<bottomY;i += changeAlongY) {
            for(int j=leftX;j<rightX;j += changeAlongX) {
                g2d.drawRect(j,i,changeAlongX,changeAlongY);
            }
        }
    }
}
