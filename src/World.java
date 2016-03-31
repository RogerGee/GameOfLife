// World.java

import java.util.BitSet;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import java.io.*;
import static org.lwjgl.opengl.GL11.*;

public class World {
    private double zoom = 10.0; // units in X/Y (i.e. radius) from view location
    private double location[] = {0.0,0.0}; // view location
    private ArrayList<BitSet> grid;
    private ArrayList<BitSet> tmpGrid;

    private static final int DIM = 512;
    private static final double HALF_DIM = DIM / 2.0;
    private static final double MIN_P = -HALF_DIM;
    private static final double MAX_P = HALF_DIM;

    public World() {
        Random rnd = new Random();

        // create default world size with DIMxDIM cells
        int bcount = DIM/8;
        grid = new ArrayList<BitSet>();
        tmpGrid = new ArrayList<BitSet>();
        for (int i = 0;i < DIM;++i) {
            // randomly generate a row of cells
            byte[] rbytes = new byte[bcount];
            rnd.nextBytes(rbytes);
            if (rbytes[3] >= 0)
                rbytes[3] = (byte)~rbytes[bcount-1];

            // create row from random data; create a copy in the
            // temporary grid
            BitSet bs = BitSet.valueOf(rbytes);
            grid.add(bs);
            tmpGrid.add((BitSet)bs.clone());
        }
    }

    public World(FileReader reader) throws IOException {
        BufferedReader input = new BufferedReader(reader);
        ArrayList<BitSet> subset = new ArrayList<BitSet>();

        // read the data from the file which will represent a subset
        // of the overall grid
        int rcount = 0;
        while (rcount < DIM) {
            String line = input.readLine();
            if (line == null)
                break;

            // create a row bitset for the input line
            int ii;
            int startCol, endCol;
            BitSet row = new BitSet(DIM);
            startCol = (int)(HALF_DIM - line.length() / 2);
            endCol = startCol + line.length();
            ii = 0;

            // make sure the columns can fit on the row; if it exceeds
            // the row then we can display only a part of it
            if (startCol < 0) {
                ii += -startCol;
                endCol += startCol;
                startCol = 0;
            }

            // all non-spaces get bits set to one on the row
            for (int i = ii;startCol < endCol;++startCol,++i) {
                if (line.charAt(i) != ' ' && line.charAt(i) != '.')
                    row.set(startCol);
            }

            subset.add(row);
            rcount += 1;
        }

        // place the subset into the overall grid
        int before;
        grid = new ArrayList<BitSet>();
        tmpGrid = new ArrayList<BitSet>();
        before = (int)Math.ceil((DIM - rcount) / 2.0);
        for (int i = 0;i < before;++i,++rcount) {
            grid.add(new BitSet(DIM));
            tmpGrid.add(new BitSet(DIM));
        }
        for (int i = subset.size()-1;i >= 0;--i) { // subset has rows in backwards
            grid.add(subset.get(i));
            tmpGrid.add((BitSet)subset.get(i).clone());
        }
        for (;rcount < DIM;++rcount) {
            grid.add(new BitSet(DIM));
            tmpGrid.add(new BitSet(DIM));
        }
    }

    // adjust the zoom setting
    public void adjustZoom(double amt) {
        double newzoom = zoom + amt;

        if (newzoom + location[0] <= MAX_P && newzoom + location[1] <= MAX_P && newzoom >= 1.0)
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
        // compute number of units from origin to sides; this accounts
        // for the distortion of the projection space
        double lx, ly;
        lx = unitsX / 2.0 * zoom + 2;
        ly = unitsY / 2.0 * zoom + 2;

        // find the bounds of the viewable grid portion
        int startCol, endCol, startRow, endRow;
        startCol = (int)(HALF_DIM + location[0] - lx);
        startRow = (int)(HALF_DIM + location[1] - ly);
        endCol = (int)(startCol + lx*2.0);
        endRow = (int)(startRow + ly*2.0);
        if (startCol < 0)
            startCol = 0;
        if (endCol > DIM)
            endCol = DIM;
        if (startRow < 0)
            startRow = 0;
        if (endRow > DIM)
            endRow = DIM;

        // find the start coordinates of the upper-left of the grid
        // portion by offsetting from the minimum position
        double sx, sy;
        sx = MIN_P + startCol;
        sy = MIN_P + startRow;

        // scale the view to fit the desired zoom level; adjust up by
        // 0.5 to account for one-half of a cell
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glScaled(1/zoom,1/zoom,1.0);
        glTranslated(-location[0],-location[1] + 0.5,0.0);
        glColor3d(0.0,0.0,0.0);

        // render cells: we just draw the viewable portion of the grid
        // for efficiency
        double cy = sy;
        for (int i = startRow;i < endRow;++i) {
            BitSet row = grid.get(i);
            double cx = sx;
            for (int j = startCol;j < endCol;++j) {
                if (row.get(j))
                    renderCell(cx,cy);
                cx += 1.0;
            }
            cy += 1.0;
        }
    }

    // step through an instance of the Game of Life
    public void playGame() {
        boolean[] state = new boolean[9];

        for (int i = 0;i < DIM;++i) {
            BitSet curRow = grid.get(i);
            for (int j = 0;j < DIM;++j) {
                // copy state from temporary grid for single cell;
                // treat invalid positions as dead
                BitSet a = i > 1 ? tmpGrid.get(i-1) : null;
                BitSet b = tmpGrid.get(i);
                BitSet c = i+1 < DIM ? tmpGrid.get(i+1) : null;
                state[0] = j > 1 && a != null && a.get(j-1);
                state[1] = a != null && a.get(j);
                state[2] = j+1 < DIM && a != null && a.get(j+1);
                state[3] = j > 1 && b.get(j-1);
                state[4] = b.get(j);
                state[5] = j+1 < DIM && b.get(j+1);
                state[6] = j > 1 && c != null && c.get(j-1);
                state[7] = c != null && c.get(j);
                state[8] = j+1 < DIM && c != null && c.get(j+1);

                // replace cell with new value from game step
                curRow.set(j,GameOfLife.gameStep(state));
            }
        }

        // copy grid into temporary grid
        for (int i = 0;i < DIM;++i) {
            BitSet bs = tmpGrid.get(i);

            bs.clear();
            bs.or(grid.get(i));
        }
    }

    // render an individual cell
    private static void renderCell(double cx,double cy) {
        double x1, x2, y1, y2;
        x1 = cx - 0.5; x2 = cx + 0.5;
        y1 = cy - 0.5; y2 = cy + 0.5;

        rectangle(x1,y1,x2,y2);
    }

    // draw basic rectangle given opposing corners
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
