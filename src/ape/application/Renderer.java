package ape.application;

import ape.files.Palettes;
import ape.files.Selection;
import org.lwjgl.glfw.GLFWVidMode;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.opengl.GL11.*;

/**This class isn't commented beyond the Functions, and each and every quad is manually rendered with glVertex2i() statements, so consider yourself warned.**/
public class Renderer {

    private static long _window;
    private static int _uiMode;
    private static ApeApplication apeApp;
    private static int cycle = 0;
    private static int blinkRate;
    private static boolean DISPLAY_BLINKING;

    public Renderer(long window, int UIMode, ApeApplication apeApp){
        _window = window;
        _uiMode = UIMode;
        this.apeApp = apeApp;
    }

    /**Render the entire frame, This is the only function that can be called from outside the class**/
    public void RenderWindow(int[][] pixelArray, int windowScale, int uiMode, GLFWVidMode glfwVidMode){
        blinkRate = glfwVidMode.refreshRate();
        cycle++;
        if(cycle % (blinkRate / 2) == 0)
            DISPLAY_BLINKING = true;
        if(cycle % blinkRate == 0)
            DISPLAY_BLINKING = false;
        _uiMode = uiMode;
        glClear(GL_COLOR_BUFFER_BIT);

        switch(_uiMode){
            case 0:
                glfwSetWindowTitle(_window, "Austin Paint Extended - Draw Mode");
                break;
            case 1:
                glfwSetWindowTitle(_window, "Austin Paint Extended - Palette Mode");
                break;
            case 2:
                glfwSetWindowTitle(_window, "Austin Paint Extended - Selection Mode");
                break;

        }
        RenderPixelGrid(pixelArray, windowScale);
        RenderUI(_window, windowScale);
    }

    /**Render the bitmap grid**/
    private void RenderPixelGrid(int[][] pixelArray, int windowScale){
        int pixelSize = 16 * windowScale;
        for(int x = 0; x < 32; x++){
            for(int y = 0; y < 32; y++){
                Color pixelColor = apeApp.GetPalette()[pixelArray[x][y]];
                glColor3f(pixelColor.getRed() / 255f, pixelColor.getGreen() / 255f, pixelColor.getBlue() / 255f);
                glBegin(GL_QUADS);
                glVertex2i(x * pixelSize, y * pixelSize);
                glVertex2i((x * pixelSize) + pixelSize, (y * pixelSize));
                glVertex2i((x * pixelSize) + pixelSize, (y * pixelSize) + pixelSize);
                glVertex2i(x * pixelSize, (y * pixelSize) + pixelSize);
                glEnd();
            }
        }

    }

    /**Render the UI**/
    private void RenderUI(long window, int windowScale){
        int[] mousePosition = apeApp.UpdateMousePixelPosition(window, windowScale);
        int pixelSize = 16 * windowScale;
        if(_uiMode == 1)
            RenderColorSelector(windowScale);
        if(_uiMode == 2)
            RenderSelection(windowScale);

        int uiSize = (32 * windowScale);
        for(int i = 0; i < 16; i++){
            int uiHeight = (apeApp.selectedColorIndex == i) ? 32 : 33;
            Color pixelColor = apeApp.GetPalette()[i];
            glColor3f(pixelColor.getRed() / 255f, pixelColor.getGreen() / 255f, pixelColor.getBlue() / 255f);
            glBegin(GL_QUADS);
                glVertex2i((i * uiSize), (uiHeight * pixelSize));
                glVertex2i((i * uiSize) + uiSize, (uiHeight * pixelSize));
                glVertex2i((i * uiSize) + uiSize, (uiHeight * pixelSize) + uiSize);
                glVertex2i((i * uiSize), (uiHeight * pixelSize) + uiSize);
            glEnd();
        }

        //draw cursor
        Color cursorColor = (DISPLAY_BLINKING && _uiMode == 2) ? Palettes.Default[0] : Palettes.Default[2];
        glColor3f(cursorColor.getRed() / 255f, cursorColor.getGreen() / 255f, cursorColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2i((mousePosition[0] * pixelSize),(mousePosition[1] * pixelSize));
            glVertex2i((mousePosition[0] * pixelSize) + pixelSize,(mousePosition[1] * pixelSize));
            glVertex2i((mousePosition[0] * pixelSize) + pixelSize,(mousePosition[1] * pixelSize) + pixelSize);
            glVertex2i((mousePosition[0] * pixelSize),(mousePosition[1] * pixelSize) + pixelSize);
        glEnd();
        Color renderColor = apeApp.GetPalette()[1];

    }

    /**Render the selection border, and points**/
    private void RenderSelection(int windowScale) {
        Selection selection = apeApp.GetSelection();
        int pixelSize = 16 * windowScale;
        if(selection.selectionStage == 3){
            for(int x = selection.xpoint1; x <= selection.xpoint2; x++){
                for(int y = selection.ypoint1; y <= selection.ypoint2; y++){
                    Color pixelColor = apeApp.GetPalette()[selection.pixels[x - selection.xpoint1][y - selection.ypoint1]];
                    glColor3f(pixelColor.getRed() / 255f, pixelColor.getGreen() / 255f, pixelColor.getBlue() / 255f);
                    glBegin(GL_QUADS);
                        glVertex2i(x * pixelSize, y * pixelSize);
                        glVertex2i(x * pixelSize + pixelSize, y * pixelSize);
                        glVertex2i(x * pixelSize + pixelSize, y * pixelSize + pixelSize);
                        glVertex2i(x * pixelSize, y * pixelSize + pixelSize);
                    glEnd();
                }
            }

            //selection border
            if(DISPLAY_BLINKING) {
                for (int x = selection.xpoint1; x <= selection.xpoint2; x++) {
                    for (int y = selection.ypoint1; y <= selection.ypoint2; y++) {
                        //dont render interior
                        if ((x != selection.xpoint1 && x != selection.xpoint2) && (y != selection.ypoint1 && y != selection.ypoint2))
                            continue;
                        Color UIColor = Palettes.Default[(y + x) % 2 == 0 ? 0 : 2];
                        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
                        glBegin(GL_QUADS);
                            glVertex2i(x * pixelSize, y * pixelSize);
                            glVertex2i(x * pixelSize + pixelSize, y * pixelSize);
                            glVertex2i(x * pixelSize + pixelSize, y * pixelSize + pixelSize);
                            glVertex2i(x * pixelSize, y * pixelSize + pixelSize);
                        glEnd();
                    }
                }
            }

        }
        if(selection.selectionStage == 2){
            //Render First Point
            Color UIColor = Palettes.Default[2];
            glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
            glBegin(GL_QUADS);
                glVertex2i(selection.xpoint1 * pixelSize, selection.ypoint1 * pixelSize);
                glVertex2i(selection.xpoint1 * pixelSize + pixelSize, selection.ypoint1 * pixelSize);
                glVertex2i(selection.xpoint1 * pixelSize + pixelSize, selection.ypoint1 * pixelSize + pixelSize);
                glVertex2i(selection.xpoint1 * pixelSize, selection.ypoint1 * pixelSize + pixelSize);
            glEnd();
            //Render Second Point
            glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
            glBegin(GL_QUADS);
                glVertex2i(selection.xpoint2 * pixelSize, selection.ypoint2 * pixelSize);
                glVertex2i(selection.xpoint2 * pixelSize + pixelSize, selection.ypoint2 * pixelSize);
                glVertex2i(selection.xpoint2 * pixelSize + pixelSize, selection.ypoint2 * pixelSize + pixelSize);
                glVertex2i(selection.xpoint2 * pixelSize, selection.ypoint2 * pixelSize + pixelSize);
            glEnd();
        }
        if(selection.selectionStage == 1){
            //Render First Point
            Color UIColor = Palettes.Default[2];
            glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
            glBegin(GL_QUADS);
                glVertex2i(selection.xpoint1 * pixelSize, selection.ypoint1 * pixelSize);
                glVertex2i(selection.xpoint1 * pixelSize + pixelSize, selection.ypoint1 * pixelSize);
                glVertex2i(selection.xpoint1 * pixelSize + pixelSize, selection.ypoint1 * pixelSize + pixelSize);
                glVertex2i(selection.xpoint1 * pixelSize, selection.ypoint1 * pixelSize + pixelSize);
            glEnd();
        }
    }

    /**The palette editing screen, by opening this function you wave all liability from any strokes or aneurysms that occur as a result of reading it.**/
    private static void RenderColorSelector(int windowScale) {
        int pixelSize = windowScale * 16;
        //Color Selector
        int uiOffset = 2 * (pixelSize);

        //Window Border
        for(int x = 0; x < 28; x ++){
            for(int y = 0; y < 28; y++){
                //checkboard pattern
                Color UIColor = Palettes.Default[(y + x) % 2 == 0 ? 0 : 2];
                if((x > 0 && x < 27) && (y > 0 && y < 27))
                    UIColor = Palettes.Default[0];

                glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
                glBegin(GL_QUADS);
                    glVertex2i((x * pixelSize) + uiOffset, (y*pixelSize) + uiOffset);
                    glVertex2i((x * pixelSize) + uiOffset + pixelSize, (y*pixelSize) + uiOffset);
                    glVertex2i((x * pixelSize) + uiOffset + pixelSize, (y*pixelSize) + uiOffset + pixelSize);
                    glVertex2i((x * pixelSize) + uiOffset, (y*pixelSize) + uiOffset + pixelSize);
                glEnd();
            }
        }

        //Red Color Selector
        uiOffset = 4 * (pixelSize);
        Color UIColor = Palettes.Default[2];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2i(uiOffset, uiOffset);
            glVertex2i(uiOffset + (pixelSize * 24), uiOffset);
            glVertex2i(uiOffset + (pixelSize * 24),  uiOffset + (pixelSize * 4));
            glVertex2i(uiOffset, uiOffset + (pixelSize * 4));
        glEnd();

        glBegin(GL_QUADS);
            glColor3f(0.0f, 0.0f, 0.0f);
            glVertex2i(uiOffset + (pixelSize), (uiOffset + pixelSize));
            glColor3f(1.0f, 0.0f, 0.0f);
            glVertex2i(uiOffset + (pixelSize * 23), (uiOffset + pixelSize));
            glVertex2i(uiOffset + (pixelSize * 23), (uiOffset + pixelSize) + (pixelSize * 2));
            glColor3f(0.0f, 0.0f, 0.0f);
            glVertex2i(uiOffset + (pixelSize), (uiOffset + pixelSize) + (pixelSize * 2));
        glEnd();

        //SelectedColor
        float colorPosition = (apeApp.GetPalette()[apeApp.selectedColorIndex].getRed() / 255f) * (pixelSize * 21) + pixelSize;
        UIColor = Palettes.Default[3];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2f(uiOffset + colorPosition, uiOffset);
            glVertex2f(uiOffset + colorPosition + (pixelSize), uiOffset);
            glVertex2f(uiOffset + colorPosition + (pixelSize),  uiOffset + (pixelSize * 4));
            glVertex2f(uiOffset + colorPosition, uiOffset + (pixelSize * 4));
        glEnd();



        //Green Color Selector
        int UIOffsetY = pixelSize * 5;
        UIColor = Palettes.Default[2];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2i(uiOffset, uiOffset + UIOffsetY);
            glVertex2i(uiOffset + (pixelSize * 24), uiOffset + UIOffsetY);
            glVertex2i(uiOffset + (pixelSize * 24),  uiOffset + UIOffsetY + (pixelSize * 4));
            glVertex2i(uiOffset, uiOffset + UIOffsetY + (pixelSize * 4));
        glEnd();

        glBegin(GL_QUADS);
            glColor3f(0.0f, 0.0f, 0.0f);
            glVertex2i(uiOffset + (pixelSize), (uiOffset + pixelSize) + UIOffsetY);
            glColor3f(0.0f, 1.0f, 0.0f);
            glVertex2i(uiOffset + (pixelSize * 23), (uiOffset + pixelSize) + UIOffsetY);
            glVertex2i(uiOffset + (pixelSize * 23), (uiOffset + pixelSize) + UIOffsetY + (pixelSize * 2));
            glColor3f(0.0f, 0.0f, 0.0f);
            glVertex2i(uiOffset + (pixelSize), (uiOffset + pixelSize) + UIOffsetY + (pixelSize * 2));
        glEnd();

        //SelectedColor
        colorPosition = (apeApp.GetPalette()[apeApp.selectedColorIndex].getGreen() / 255f) * (pixelSize * 21) + pixelSize;
        UIColor = Palettes.Default[3];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2f(uiOffset + colorPosition, uiOffset + UIOffsetY);
            glVertex2f(uiOffset + colorPosition + (pixelSize), uiOffset + UIOffsetY);
            glVertex2f(uiOffset + colorPosition + (pixelSize),  uiOffset + UIOffsetY + (pixelSize * 4));
            glVertex2f(uiOffset + colorPosition, uiOffset + UIOffsetY + (pixelSize * 4));
        glEnd();


        //Blue Color Selector
        UIOffsetY = pixelSize * 10;
        UIColor = Palettes.Default[2];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2i(uiOffset, uiOffset + UIOffsetY);
            glVertex2i(uiOffset + (pixelSize * 24), uiOffset + UIOffsetY);
            glVertex2i(uiOffset + (pixelSize * 24),  uiOffset + UIOffsetY + (pixelSize * 4));
            glVertex2i(uiOffset, uiOffset + UIOffsetY + (pixelSize * 4));
        glEnd();

        glBegin(GL_QUADS);
            glColor3f(0.0f, 0.0f, 0.0f);
            glVertex2i(uiOffset + (pixelSize), (uiOffset + pixelSize) + UIOffsetY);
            glColor3f(0.0f, 0.0f, 1.0f);
            glVertex2i(uiOffset + (pixelSize * 23), (uiOffset + pixelSize) + UIOffsetY);
            glVertex2i(uiOffset + (pixelSize * 23), (uiOffset + pixelSize) + UIOffsetY + (pixelSize * 2));
            glColor3f(0.0f, 0.0f, 0.0f);
            glVertex2i(uiOffset + (pixelSize), (uiOffset + pixelSize) + UIOffsetY + (pixelSize * 2));
        glEnd();

        //SelectedColor
        colorPosition = (apeApp.GetPalette()[apeApp.selectedColorIndex].getBlue() / 255f) * (pixelSize * 21) + pixelSize;
        UIColor = Palettes.Default[3];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2f(uiOffset + colorPosition, uiOffset + UIOffsetY);
            glVertex2f(uiOffset + colorPosition + (pixelSize), uiOffset + UIOffsetY);
            glVertex2f(uiOffset + colorPosition + (pixelSize),  uiOffset + UIOffsetY + (pixelSize * 4));
            glVertex2f(uiOffset + colorPosition, uiOffset + UIOffsetY + (pixelSize * 4));
        glEnd();

        //Color Preview
        uiOffset = 4 * pixelSize;
        UIOffsetY = 19 * pixelSize;
        UIColor = Palettes.Default[2];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2i(uiOffset, UIOffsetY);
            glVertex2i(uiOffset + (24 * pixelSize), UIOffsetY);
            glVertex2i(uiOffset + (24 * pixelSize), UIOffsetY + (9 * pixelSize));
            glVertex2i(uiOffset, UIOffsetY + (9 * pixelSize));
        glEnd();

        uiOffset = 5 * pixelSize;
        UIOffsetY = 20 * pixelSize;
        UIColor = apeApp.GetPalette()[apeApp.selectedColorIndex];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2i(uiOffset, UIOffsetY);
            glVertex2i(uiOffset + (22 * pixelSize), UIOffsetY);
            glVertex2i(uiOffset + (22 * pixelSize), UIOffsetY + (7 * pixelSize));
            glVertex2i(uiOffset, UIOffsetY + (7 * pixelSize));
        glEnd();
    }
}
