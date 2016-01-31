// World.java

import java.util.BitSet;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import static org.lwjgl.opengl.GL11.*;

public class World {
    private double zoom = 20.0; // units in X/Y (i.e. radius) from view location
    private double location[] = {0.0,0.0}; // view location
    private ArrayList<BitSet> grid;

    private static final int DIM = 512;
    private static final double HALF_DIM = DIM / 2.0;
    private static final double MIN_P = -HALF_DIM;
    private static final double MAX_P = HALF_DIM;

    public World() {
        Random rnd = new Random();

        // create default world size with DIMxDIM cells
        int bcount = DIM/8;
        grid = new ArrayList<BitSet>();
        for (int i = 0;i < DIM;++i) {
            // randomly generate a row of cells
            byte[] rbytes = new byte[bcount];
            rnd.nextBytes(rbytes);
            if (rbytes[3] >= 0)
                rbytes[3] = (byte)~rbytes[bcount-1];
            grid.add(BitSet.valueOf(rbytes));
        }
    }

    // adjust the zoom setting
    public void adjustZoom(double amt) {
        double newzoom = zoom + amt;

        if (newzoom + location[0] <= MAX_P && newzoom + location[1] <= MAX_P)
            zoom = newzoom;
    }

    // adjust the view offset
    public void adjustView(double cx,double cy) {
        double newx = location[0] + cx*zoom;
        double newy = location[1] + cy*zoom;

        if (newx - zoom >= MIN_P && newx + zoom <= MAX_P)
            location[0] = newx;
        if (newy - zoom >= MIN_P && newy + zoom <= MAX_P)
            location[1] = newy;
    }

    // render the world given the specified projection space
    public void render(double unitsX,double unitsY) {
        // compute metrics for the renderable area; the projection
        // could map a non-square image to the viewport, meaning that
        // our zoom level may allow different numbers of cells to be
        // viewable in each direction
        int upDown = (int)Math.ceil(unitsY / 2.0 * zoom) + 2;
        int leftRight = (int)Math.ceil(unitsX / 2.0 * zoom) + 2;
        int across = leftRight * 2;
        int down = upDown * 2;
        int startRow = (int)(HALF_DIM + location[1]) - upDown;
        int startCol = (int)(HALF_DIM + location[0]) - leftRight;

        // scale the view to fit the desired zoom level
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glScaled(1/zoom,1/zoom,1.0);
        glTranslated(-(location[0] - (int)Math.floor(location[0])),location[1] - (int)Math.floor(location[1]),0.0);

        // draw background for dead cells; it must be one-half-cell
        // width greater in each dimension (0.5)
        double x1, y1;
        x1 = -(leftRight + 0.5);
        y1 = upDown + 0.5;
        glColor3d(1.0,1.0,1.0);
        rectangle(x1,y1,x1+across+0.5,y1-down-0.5);
        glColor3d(0.0,0.0,0.0);

        double cy = upDown;
        for (int i = 0,r = startRow;i < down;++i,++r) {
            if (r >= 0 && r < grid.size()) {
                BitSet row = grid.get(r);
                double cx = -(leftRight);
                for (int j = 0,c = startCol;j < across;++j,++c) {
                    if (c >= 0 && c < row.length() && row.get(c))
                        renderCell(cx,cy);
                    cx += 1.0;
                }
            }
            cy -= 1.0;
        }
    }

    // render an individual cell
    private static void renderCell(double cx,double cy) {
        double x1, x2, y1, y2;
        x1 = cx - 0.5; x2 = cx + 0.5;
        y1 = cy - 0.5; y2 = cy + 0.5;

        rectangle(x1,y1,x2,y2);
    }

    private static void rectangle(double x1,double y1,double x2,double y2) {
        glBegin(GL_QUADS);
        {
            glVertex2d(x1,y1);
            glVertex2d(x2,y1);
            glVertex2d(x2,y2);
            glVertex2d(x1,y2);
            glVertex2d(x1,y1);
        }
        glEnd();
    }
}
