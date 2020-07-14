package ape.application;

import ape.files.Palettes;
import ape.files.Selection;
import org.lwjgl.glfw.GLFWVidMode;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.opengl.GL11.*;

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

    //Render the entire frame, This is the only function that can be called from outside the class
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

    //Render the bitmap grid
    private void RenderPixelGrid(int[][] pixelArray, int windowScale){
        int pixelSize = 16 * windowScale;
        //Loop though the entire bitmap and draw each pixel
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

    //Render the UI
    private void RenderUI(long window, int windowScale){
        int[] mousePosition = apeApp.UpdateMousePixelPosition(window, windowScale); /**Why is this being called from here? i want to remove it but im too scared too**/
        int pixelSize = 16 * windowScale;

        //Render additional UI if needed
        if(_uiMode == 1)
            RenderColorSelector(windowScale);
        if(_uiMode == 2)
            RenderSelection(windowScale);

        //Draw color selector
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

        //Draw cursor
        Color cursorColor = (DISPLAY_BLINKING && _uiMode == 2) ? Palettes.Default[0] : Palettes.Default[2];
        glColor3f(cursorColor.getRed() / 255f, cursorColor.getGreen() / 255f, cursorColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2i((mousePosition[0] * pixelSize),(mousePosition[1] * pixelSize));
            glVertex2i((mousePosition[0] * pixelSize) + pixelSize,(mousePosition[1] * pixelSize));
            glVertex2i((mousePosition[0] * pixelSize) + pixelSize,(mousePosition[1] * pixelSize) + pixelSize);
            glVertex2i((mousePosition[0] * pixelSize),(mousePosition[1] * pixelSize) + pixelSize);
        glEnd();
    }

    //Render the selection bitmap, border, and points
    private void RenderSelection(int windowScale) {
        Selection selection = apeApp.GetSelection();
        int pixelSize = 16 * windowScale;

        //Only one point selected
        if(selection.selectionStage >= 1){
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

        //Two points selected
        if(selection.selectionStage == 2){
            //Render Second Point
            Color UIColor = Palettes.Default[2];
            glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
            glBegin(GL_QUADS);
                glVertex2i(selection.xpoint2 * pixelSize, selection.ypoint2 * pixelSize);
                glVertex2i(selection.xpoint2 * pixelSize + pixelSize, selection.ypoint2 * pixelSize);
                glVertex2i(selection.xpoint2 * pixelSize + pixelSize, selection.ypoint2 * pixelSize + pixelSize);
                glVertex2i(selection.xpoint2 * pixelSize, selection.ypoint2 * pixelSize + pixelSize);
            glEnd();
        }

        //Selection is complete
        if(selection.selectionStage == 3){

            //Render selection bitmap
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


            //render flashing border
            if(DISPLAY_BLINKING) {
                for (int x = selection.xpoint1; x <= selection.xpoint2; x++) {
                    for (int y = selection.ypoint1; y <= selection.ypoint2; y++) {

                        //Skip interior space
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

    }

    //Palette Editing screen
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

        //Render color selectors
        uiOffset = 4 * (pixelSize);
        int uiOffsetY;

        //Loop though the three primary colors and create a slider box for each one
        for(int colorIndex = 0; colorIndex < 3; colorIndex++){
            uiOffsetY = pixelSize * (5 * colorIndex);

            //Draw Slider Border
            Color UIColor = Palettes.Default[2];
            glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
            glBegin(GL_QUADS);
                glVertex2i(uiOffset, uiOffset + uiOffsetY);
                glVertex2i(uiOffset + (pixelSize * 24), uiOffset + uiOffsetY);
                glVertex2i(uiOffset + (pixelSize * 24),  uiOffset + uiOffsetY + (pixelSize * 4));
                glVertex2i(uiOffset, uiOffset + uiOffsetY + (pixelSize * 4));
            glEnd();

            //Get correct color value for fading bar
            Color editingColor = new Color(0,0,0);
            switch(colorIndex){
                case 0:
                    editingColor = new Color(1.0f, 0, 0);
                    break;
                case 1:
                    editingColor = new Color(0, 1.0f, 0);
                    break;
                case 2:
                    editingColor = new Color(0, 0, 1.0f);
                    break;
            }

            //Draw fading color bar
            glBegin(GL_QUADS);
                glColor3f(0.0f, 0.0f, 0.0f);
                glVertex2i(uiOffset + (pixelSize), (uiOffset + pixelSize) + uiOffsetY);
                glColor3f(editingColor.getRed() / 255f, editingColor.getGreen() / 255f, editingColor.getBlue() / 255f);
                glVertex2i(uiOffset + (pixelSize * 23), (uiOffset + pixelSize) + uiOffsetY);
                glVertex2i(uiOffset + (pixelSize * 23), (uiOffset + pixelSize) + uiOffsetY + (pixelSize * 2));
                glColor3f(0.0f, 0.0f, 0.0f);
                glVertex2i(uiOffset + (pixelSize), (uiOffset + pixelSize) + uiOffsetY + (pixelSize * 2));
            glEnd();

            //Get correct value for slider bar
            float colorValue = 0.0f;
            switch(colorIndex){
                case 0:
                    colorValue = (apeApp.GetPalette()[apeApp.selectedColorIndex].getRed() / 255f);
                    break;
                case 1:
                    colorValue = (apeApp.GetPalette()[apeApp.selectedColorIndex].getGreen() / 255f);
                    break;
                case 2:
                    colorValue = (apeApp.GetPalette()[apeApp.selectedColorIndex].getBlue() / 255f);
                    break;
            }

            //Draw slider bar
            float colorPosition = colorValue * (pixelSize * 21) + pixelSize;
            UIColor = Palettes.Default[3];
            glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
            glBegin(GL_QUADS);
                glVertex2f(uiOffset + colorPosition, uiOffset + uiOffsetY);
                glVertex2f(uiOffset + colorPosition + (pixelSize), uiOffset + uiOffsetY);
                glVertex2f(uiOffset + colorPosition + (pixelSize),  uiOffset + uiOffsetY + (pixelSize * 4));
                glVertex2f(uiOffset + colorPosition, uiOffset + uiOffsetY + (pixelSize * 4));
            glEnd();
        }

        //Color Preview Border
        uiOffsetY = 19 * pixelSize;
        Color UIColor = Palettes.Default[2];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2i(uiOffset, uiOffsetY);
            glVertex2i(uiOffset + (24 * pixelSize), uiOffsetY);
            glVertex2i(uiOffset + (24 * pixelSize), uiOffsetY + (9 * pixelSize));
            glVertex2i(uiOffset, uiOffsetY + (9 * pixelSize));
        glEnd();

        //Color Preview Fill
        uiOffset = 5 * pixelSize;
        uiOffsetY = 20 * pixelSize;
        UIColor = apeApp.GetPalette()[apeApp.selectedColorIndex];
        glColor3f(UIColor.getRed() / 255f, UIColor.getGreen() / 255f, UIColor.getBlue() / 255f);
        glBegin(GL_QUADS);
            glVertex2i(uiOffset, uiOffsetY);
            glVertex2i(uiOffset + (22 * pixelSize), uiOffsetY);
            glVertex2i(uiOffset + (22 * pixelSize), uiOffsetY + (7 * pixelSize));
            glVertex2i(uiOffset, uiOffsetY + (7 * pixelSize));
        glEnd();
    }
}
