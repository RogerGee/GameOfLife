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
}
