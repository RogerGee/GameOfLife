// Window.java

import java.nio.IntBuffer;
import java.io.*;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window implements Runnable {
    private long windowId;
    private long windowWidth;
    private long windowHeight;
    private double unitsX; // units in horizontile projection space
    private double unitsY; // units in vertical projection space
    private World world;
    private GLFWCursorPosCallback cbCurPos;
    private GLFWScrollCallback cbCurScroll;
    private GLFWKeyCallback cbKeyPress;
    private boolean pauseGame = false;

    private static final double DIM = 2;
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;

    public Window(String file) throws FileNotFoundException,IOException {
        // create the world object; pass it a file reader if the
        // initial game state is to be taken from a file
        if (file == null)
            world = new World();
        else {
            FileReader reader = new FileReader(file);
            world = new World(reader);
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE,GLFW_FALSE);
        windowId = glfwCreateWindow(DEFAULT_WIDTH,DEFAULT_HEIGHT,"The Game of Life",NULL,NULL);
        if (windowId == NULL) {
            throw new RuntimeException("fail glfwCreateWindow()");
        }
        queryWindowDims();

        cbCurPos = new GLFWCursorPosCallback() {
                @Override
                public void invoke(long window,double cx,double cy) {
                    onWindowPosChange(window,cx,cy);                    
                }
            };
        glfwSetCursorPosCallback(windowId,cbCurPos);

        cbCurScroll = new GLFWScrollCallback() {
                @Override
                public void invoke(long window,double xoffset,double yoffset) {
                    onMouseScroll(window,xoffset,yoffset);
                }
            };
        glfwSetScrollCallback(windowId,cbCurScroll);

        cbKeyPress = new GLFWKeyCallback() {
                @Override
                public void invoke(long window,int key,int scancode,int action,int mods) {
                    onKeyPress(window,key,scancode,action,mods);
                }
            };
        glfwSetKeyCallback(windowId,cbKeyPress);
    }

    // main drawing loop: assume this is called uniquely per each
    // thread
    public void run() {
        // bind the GL context to this thread and setup the context
        // for use
        glfwMakeContextCurrent(windowId);
        GL.createCapabilities();
        glfwSwapInterval(1);
        glClearColor(1.0f,1.0f,1.0f,1.0f);
        adjustProjection();

        double lastTime = System.nanoTime();
        while (glfwWindowShouldClose(windowId) == 0) {
            double time = System.nanoTime();

            glClear(GL_COLOR_BUFFER_BIT);

            world.render(unitsX,unitsY);
            if (time - lastTime >= 100000000 && !pauseGame) {
                world.playGame();
                lastTime = time;
            }

            glfwSwapBuffers(windowId);
            glfwPollEvents();
        }

        glfwDestroyWindow(windowId);
    }

    private void queryWindowDims() {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(windowId,w,h);
        windowWidth = w.get(0);
        windowHeight = h.get(0);
    }

    private void adjustProjection() {
        double ratio = (double)windowWidth / windowHeight;
        double iratio = 1.0 / ratio;

        // adjust the projection to account for distortion caused by
        // the window's aspect ratio
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glScaled(iratio,1.0,1.0);
        unitsX = DIM * ratio;
        unitsY = DIM; // unchanged
    }

    private double lastX = -1, lastY = -1;
    private void onWindowPosChange(long window,double xpos,double ypos) {
        if (glfwGetMouseButton(window,GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
            if (lastX == -1 || lastY == -1) {
                lastX = xpos;
                lastY = ypos;
            }
            else {
                world.adjustView(-(xpos-lastX) * unitsX / windowWidth,(ypos-lastY) * unitsY / windowHeight);
                lastX = xpos;
                lastY = ypos;
            }
        }
        else {
            lastX = -1;
            lastY = -1;
        }
    }

    private void onMouseScroll(long window,double xoffset,double yoffset) {
        // negate offset so negative scrolls scroll out; this is
        // simply a preference
        world.adjustZoom(-yoffset);
    }

    private void onKeyPress(long window,int key,int scancode,int action,int mods) {
        if (key == GLFW_KEY_P && action == GLFW_RELEASE) {
            pauseGame = !pauseGame;
        }
    }
}
