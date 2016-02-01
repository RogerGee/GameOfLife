// GameOfLife.java

import static org.lwjgl.glfw.GLFW.*;

public class GameOfLife
{
    public static void main(String[] args)
    {
        // we require native library dependencies; tell the JVM where
        // to find these
        System.setProperty("org.lwjgl.librarypath","native");

        // initialize the graphics library
        if (glfwInit() == 0) {
            System.err.println("fail glfwInit()");
            System.exit(1);
        }

        // create and run a window instance
        Window w = new Window();
        w.run();

        glfwTerminate();
    }

    // evaluate a single conway instance; 'state' is interpreted as a
    // grid of 9 cells with the center cell being evaluated
    public static boolean gameStep(boolean[] state) {
        // count neighbors
        int no = 0;
        for (int i = 0;i < 9;++i) {
            if (i != 4) {
                no += state[i] ? 1 : 0;
            }
        }

        // any live cell stays alive if it has 2 or three neighbors;
        // it dies otherwise
        if (state[4]) {
            return no >= 2 && no <= 3;
        }

        // a dead cell may come back to life if it has exactly three
        // neighbors
        return no == 3;
    }
}
