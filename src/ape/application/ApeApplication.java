package ape.application;

import ape.files.*;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import javax.swing.*;

import static org.lwjgl.opengl.GL11.*;

import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import java.nio.DoubleBuffer;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.glfwInit;

/**If you find any problems or bugs feel free to not tell me**/
public class ApeApplication {

    private Color[] palette;
    private int paletteIndex = 1;
    private int _windowResolutionScale = 1;
    private String _currentFile;
    private int[][] _pixelArray;
    private final FileManager _fileManager;
    private long _window;
    private Selection selection;

    public int selectedColorIndex = 1;
    private boolean restart;
    private int UIMode = 0; //draw, edit color
    private int[] mousePixelPosition = new int[2];
    private boolean lockMouseX = false;
    private boolean lockMouseY = true;
    private final LinkedList<PixelChange> undoBuffer = new LinkedList<PixelChange>();


    /**Hooray! you've made it to the start,
           it only goes downhill from here**/

    public static void main(String[] args){
        /**Why is main static, well fine, workaround time!**/
        ApeApplication apeApp = new ApeApplication();
    }

    public ApeApplication(){
        /**Starting the program in a constructor? that's dumb**/
        _fileManager = new FileManager();
        //load supplied path or set default.
        _currentFile = System.getProperty("user.home") + "/appdata/local/austin_paint/picture.ap2";
        APFile loadedFile = new APFile();
        loadedFile = _fileManager.loadPixelArrayFromFile(_currentFile);
        _pixelArray = loadedFile.pixelArray;
        palette = loadedFile.palette;
        paletteIndex = -1;
        //_pixelArray = new int[32][32];
        //_fileManager.saveFileFromImage(_currentFile, _pixelArray);
        InitProgram();
    }

    /**this does more than initialization but i dont care**/
    private void InitProgram(){
        selection = new Selection();
        restart = false;
        //create LWJGL Window
        _window = InitNewWindow(_windowResolutionScale);
        Renderer renderer = new Renderer(_window, 0, this);
        Input input = new Input();
        Mouse mouse = new Mouse();
        glfwSetKeyCallback(_window, input);
        glfwSetMouseButtonCallback(_window, mouse);
        glfwSwapInterval(1);
        while(!glfwWindowShouldClose(_window)){
            if(restart)
                break;
            Input.update();
            Mouse.update();
            glfwPollEvents(); /**this shouldn't need to be here but it lags if i dont do it again**/
            mousePixelPosition = UpdateMousePixelPosition(_window, _windowResolutionScale);
            PollInput(_window, _windowResolutionScale, _fileManager);
            renderer.RenderWindow(_pixelArray, _windowResolutionScale, UIMode, glfwGetVideoMode(glfwGetPrimaryMonitor()));
            glfwSwapBuffers(_window);
        }
        glfwTerminate();
        if(restart)
            InitProgram();
    }

    /**Should every input function be in the same place like this? No.
            Is it? Yes.**/
    private void PollInput(long window, int windowScale, FileManager fileManager){
        //set pixel
        if(Mouse.getButton(GLFW_MOUSE_BUTTON_1)) {
            if (UIMode == 1) {
                //Change Color Palette color
                if ((mousePixelPosition[0] > 4 && mousePixelPosition[0] < 27)) {
                    //Red
                    if (mousePixelPosition[1] > 4 && mousePixelPosition[1] < 7) {
                        float Red = ((255f / 21f) * (mousePixelPosition[0] - 5));
                        int Redint = (int) Math.round(Red);
                        int Green = palette[selectedColorIndex].getGreen();
                        int Blue = palette[selectedColorIndex].getBlue();
                        palette[selectedColorIndex] = new Color(Redint, Green, Blue);
                    }

                    //Green
                    if (mousePixelPosition[1] > 9 && mousePixelPosition[1] < 12) {
                        int Red = palette[selectedColorIndex].getRed();
                        float Green = ((255f / 21f) * (mousePixelPosition[0] - 5));
                        int Greenint = (int) Math.round(Green);
                        int Blue = palette[selectedColorIndex].getBlue();
                        palette[selectedColorIndex] = new Color(Red, Greenint, Blue);
                    }

                    //Blue
                    if (mousePixelPosition[1] > 14 && mousePixelPosition[1] < 18) {
                        int Red = palette[selectedColorIndex].getRed();
                        int Green = palette[selectedColorIndex].getGreen();
                        float Blue = ((255f / 21f) * (mousePixelPosition[0] - 5));
                        int Blueint = (int) Math.round(Blue);
                        palette[selectedColorIndex] = new Color(Red, Green, Blueint);
                    }

                }


            }

            /**Create new selection**/
            if (UIMode == 2 && Mouse.getButtonDown(GLFW_MOUSE_BUTTON_1) && mousePixelPosition[1] < 32) {
                if (selection.selectionStage == 3) { /**Reset**/
                    selection.ResetSelection();
                    selection.selectionStage = 0;
                }
                if (selection.selectionStage == 1) { /**Second Point**/
                    selection.xpoint2 = mousePixelPosition[0];
                    selection.ypoint2 = mousePixelPosition[1];
                    selection.selectionStage = 2;
                    selection.CompleteSelection(_pixelArray);
                }
                if (selection.selectionStage == 0) { /**First Point**/
                    selection.xpoint1 = mousePixelPosition[0];
                    selection.ypoint1 = mousePixelPosition[1];
                    selection.selectionStage = 1;
                }
            }
        }
        /**Bitmap manip. mode**/
        if(mousePixelPosition[1] < 32 && UIMode == 0) {
            if(Mouse.getButtonDown(GLFW_MOUSE_BUTTON_1)) {
                PixelChange newPixel = new PixelChange(); /**Create a new Undo Buffer for each new mouse release**/
                undoBuffer.addFirst(newPixel);
            }

            if(Mouse.getButton(GLFW_MOUSE_BUTTON_1)) {
                undoBuffer.getFirst().AddPixel(mousePixelPosition[0], mousePixelPosition[1], _pixelArray[mousePixelPosition[0]][mousePixelPosition[1]]); /**Add to existing undo buffer**/
                _pixelArray[mousePixelPosition[0]][mousePixelPosition[1]] = selectedColorIndex;
            }
        }

        /**Select color**/
        if(mousePixelPosition[1] > 32 && Mouse.getButton(GLFW_MOUSE_BUTTON_1)){
            selectedColorIndex = mousePixelPosition[0] / 2;
        }


        /**Copy and Paste selected pixels**/
        if(UIMode == 2 ){

            /**This is dumb**/
            if(Input.getKeyDown(GLFW_KEY_UP)){
                selection.ypoint1--;
                selection.ypoint2--;
            }
            if(Input.getKeyDown(GLFW_KEY_DOWN)){
                selection.ypoint1++;
                selection.ypoint2++;
            }
            if(Input.getKeyDown(GLFW_KEY_RIGHT)){
                selection.xpoint1++;
                selection.xpoint2++;
            }
            if(Input.getKeyDown(GLFW_KEY_LEFT)){
                selection.xpoint1--;
                selection.xpoint2--;
            }

            /** Flip selection horiz. **/
            if(Input.getKeyDown(GLFW_KEY_Z)){
                /** This causes a memory leak... sucks **/
                int[][] flippedPixelArray = new int[selection.xpoint2 - selection.xpoint1 + 1][selection.ypoint2 - selection.ypoint1 + 1];
                for(int x = 0; x <= selection.xpoint2 - selection.xpoint1; x++){
                    for(int y = 0; y <= selection.ypoint2 - selection.ypoint1; y++){
                        flippedPixelArray[(selection.xpoint2 - selection.xpoint1) - x][y] = selection.pixels[x][y];
                    }
                }
                selection.pixels = flippedPixelArray;
            }

            /** Flip selection vertical. **/
            if(Input.getKeyDown(GLFW_KEY_X)){
                /**Two memory leaks for the price of one**/
                int[][] flippedPixelArray = new int[selection.xpoint2 - selection.xpoint1 + 1][selection.ypoint2 - selection.ypoint1 + 1];
                for(int x = 0; x <= selection.xpoint2 - selection.xpoint1; x++){
                    for(int y = 0; y <= selection.ypoint2 - selection.ypoint1; y++){
                        flippedPixelArray[x][(selection.ypoint2 - selection.ypoint1) - y] = selection.pixels[x][y];
                    }
                }
                selection.pixels = flippedPixelArray;
            }

            /**Paste selection**/
            if(Input.getKeyDown(GLFW_KEY_ENTER)){
                //confirm paste
                PixelChange newPixel = new PixelChange();
                undoBuffer.addFirst(newPixel);

                for(int x = selection.xpoint1; x <= selection.xpoint2; x++){
                    for(int y = selection.ypoint1; y <= selection.ypoint2; y++){
                        if((x < 0 || x > 31) || (y < 0 || y > 31)) /** This shouldn't ever happen but it does **/
                            continue;
                        undoBuffer.getFirst().AddPixel(x,y, _pixelArray[x][y]);
                        _pixelArray[x][y] = selection.pixels[x - selection.xpoint1][y - selection.ypoint1];
                    }
                }
                selection.ResetSelection();
                selection.selectionStage = 0;
                UIMode = 0;
            }
        }

        lockMouseX = (Input.getKey(GLFW_KEY_Z) && !Input.getKey(GLFW_KEY_LEFT_CONTROL) && UIMode == 0);
        lockMouseY = (Input.getKey(GLFW_KEY_X) && !Input.getKey(GLFW_KEY_LEFT_CONTROL) && UIMode == 0);

        /**Change into palette mode**/
        if(Input.getKeyDown(GLFW_KEY_E)){
            UIMode = (UIMode == 1 ? 0 : 1);
            selection.ResetSelection();
            selection.selectionStage = 0;
        }

        /**Change into selection mode**/
        if(Input.getKeyDown(GLFW_KEY_LEFT_SHIFT)){
            UIMode = (UIMode == 2 ? 0 : 2);
        }

        /**Change into bitmap mode**/
        if(Input.getKeyDown(GLFW_KEY_ESCAPE)){
            UIMode = 0;
            selection.ResetSelection();
            selection.selectionStage = 0;
        }

        /**Change window scale**/
        if(Input.getKeyDown(GLFW_KEY_EQUAL)){
            _windowResolutionScale++;
            restart = true; /**I really shouldn't have to restart the whole window like this but glOrthoMode: glMatrix has forced my hand**/
            glfwDestroyWindow(_window);
        }
        if(Input.getKeyDown(GLFW_KEY_MINUS)){
            _windowResolutionScale--;
            restart = true; /** ^^^ **/
            glfwDestroyWindow(_window);
        }

        /**Change to next palette.. shouldn't this just be one button**/
        if(Input.getKeyDown(GLFW_KEY_RIGHT_BRACKET)){
            paletteIndex++;
            if(paletteIndex > 3)
                paletteIndex = 0;
            loadPalette(paletteIndex);
        }
        /**Change to previous palette**/
        if(Input.getKeyDown(GLFW_KEY_LEFT_BRACKET)){
            paletteIndex--;
            if(paletteIndex < 0)
                paletteIndex = 3;
            loadPalette(paletteIndex);
        }

        /**Undo**/
        if(Input.getKey(GLFW_KEY_LEFT_CONTROL) && Input.getKeyDown(GLFW_KEY_Z) && undoBuffer.size() > 0){
            PixelChange pc = undoBuffer.getFirst();
            for(PixelChange.Pixel2i i : pc.pixelChanges){ /**Loop though all the changed pixels in the Undo Stack and... well.. undo them**/
                _pixelArray[i.x][i.y] = i.colorIndex;
            }
            undoBuffer.removeFirst();
        }

        /**Fill canvas**/
        if(Input.getKey(GLFW_KEY_LEFT_CONTROL) && Input.getKeyDown(GLFW_KEY_F)){
            PixelChange newPixel = new PixelChange();
            undoBuffer.addFirst(newPixel);
            for(int x = 0; x < 32; x++){
                for(int y = 0; y < 32; y++){
                    undoBuffer.getFirst().AddPixel(x,y, _pixelArray[x][y]); /** The undo buffer generated here only works like 80% of the time, deal with it**/
                    _pixelArray[x][y] = selectedColorIndex;
                }
            }
        }

        /**Input a manual hex code in Palette mode. No I dont know why this is all the way down here**/
        if(UIMode == 1 && Input.getKeyDown(GLFW_KEY_H)){
            /**JOptionPane is for lazy people and I'm feeling lazy**/
            String HexInput = JOptionPane.showInputDialog("Input 6 Digit Hex Code:");
            if(HexInput.length() < 6){
                HexInput = "000000";
            }
            String redString = HexInput.substring(0,2);
            String greenString = HexInput.substring(2,4);
            String blueString = HexInput.substring(4,6);
            int red = 0;
            int green = 0;
            int blue = 0;

            try {
                red = Integer.parseInt(redString, 16);
                green = Integer.parseInt(greenString, 16);
                blue = Integer.parseInt(blueString, 16);
            }catch(Exception e){
                System.out.println("Hex value not formatted correctly");
            }
            System.out.println(redString + " " + greenString + " " + blueString);
            palette[selectedColorIndex] = new Color(red, green, blue);
        }


        /**Open an Austin paint file**/
        if(Input.getKey(GLFW_KEY_LEFT_CONTROL) && Input.getKeyDown(GLFW_KEY_O)) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.getName().endsWith(".ap2")) {
                        return true;
                    }
                    return false;
                }
                @Override
                public String getDescription() {
                    return "Austin Paint 2 Files";
                }
            });
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/appdata/local/austin_paint/"));
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selected = fileChooser.getSelectedFile();
                APFile newFile = fileManager.loadPixelArrayFromFile(_currentFile);
                _pixelArray = newFile.pixelArray;
                palette = newFile.palette;
            }
        }
        /**Save an Austin paint file**/
        if(Input.getKey(GLFW_KEY_LEFT_CONTROL) && Input.getKeyDown(GLFW_KEY_S)){
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if(f.getName().endsWith(".ap2")){
                        return true;
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return "Austin Paint 2 Files";
                }
            });
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/appdata/local/austin_paint/"));
            int result = fileChooser.showSaveDialog(null);
            if(result == JFileChooser.APPROVE_OPTION){
                File selected = fileChooser.getSelectedFile();

                if(!selected.getName().endsWith(".ap2"))
                    selected = new File(fileChooser.getSelectedFile() + ".ap2");
                APFile newFile = new APFile();
                newFile.pixelArray = _pixelArray;
                newFile.palette = palette;

                fileManager.saveFileFromImage(_currentFile, newFile);
            }
        }


    }

    /** why is this not just an Enum?**/
    private void loadPalette(int index){
        if(index == 0){
            System.arraycopy(Palettes.Default, 0, palette, 0, 16);
        }
        if(index == 1){
            System.arraycopy(Palettes.Minecraft, 0, palette, 0, 16);
        }
        if(index == 2){
            System.arraycopy(Palettes.Wads, 0, palette, 0, 16);
        }
        if(index == 3){
            System.arraycopy(Palettes.Grayscale, 0, palette, 0, 16);
        }
    }

    /** Create OpenGL window and create OpenGL capabilities**/
    private long InitNewWindow(int windowScale){
        if(!glfwInit()){
            System.err.println("GLFW Window failed to initialize!");
            System.exit(1);
        }

        long window = glfwCreateWindow(512 * windowScale, (512 * windowScale) + (32 * windowScale), "Austin Paint Extended - Draw Mode", 0 , 0);
        glfwShowWindow(window);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        glMatrixMode(GL_PROJECTION); /** "GL_PROJECTION is obsolete, use VBOs instead!" Do I look like I care? **/
        glLoadIdentity();
        glOrtho(0, 512 * windowScale, (512 * windowScale) + (32 * windowScale), 0, 1, -1);
        glMatrixMode(GL_PROJECTION); /**You may ask, why is this here twice, thats a stupid question. A better question is why does it crash when its not.**/

        return window;
    }

    /** Just make these public do i not trust myself or something? (yes) **/
    public Selection GetSelection(){
        return selection;
    }
    public Color[] GetPalette(){
        return palette;
    }

    /**This should really be in the Mouse class**/
    public int[] UpdateMousePixelPosition(long window, int windowScale) {
        int[] mousePos = mousePixelPosition;
        DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(window, mouseX, mouseY);
        if(!lockMouseY)
            mousePos[0] = (int)Math.floor(mouseX.get(0)) / (16 * windowScale);
        if(!lockMouseX)
            mousePos[1] = (int)Math.floor(mouseY.get(0)) / (16 * windowScale);

        //clamp mouse position
        if(mousePos[0] < 0) mousePos[0] = 0;
        if(mousePos[0] > 31) mousePos[0] = 31;
        if(mousePos[1] < 0) mousePos[1] = 0;
        if(mousePos[1] > 33) mousePos[1] = 33;

        return mousePos;
    }
}
