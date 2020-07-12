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
import java.util.ArrayList;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.glfwInit;

public class ApeApplication {

    private Color[] palette;
    private int paletteIndex = 1;

    private int _windowResolutionScale = 1;
    private String _currentFile; //file that the program is accessing
    private int[][] _pixelArray;
    private final FileManager _fileManager;
    private long _window;
    private Selection selection;

    //input
    public int selectedColorIndex = 1;
    private boolean restart;

    private int UIMode = 0; //draw, edit color
    private int[] mousePixelPosition = new int[2];

    private boolean lockMouseX = false;
    private boolean lockMouseY = true;

    private final LinkedList<PixelChange> undoBuffer = new LinkedList<PixelChange>();

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
            glfwPollEvents();
            mousePixelPosition = UpdateMousePixelPosition(_window, _windowResolutionScale);
            PollInput(_window, _windowResolutionScale, _fileManager);
            renderer.RenderWindow(_pixelArray, _windowResolutionScale, UIMode, glfwGetVideoMode(glfwGetPrimaryMonitor()));
            glfwSwapBuffers(_window);
        }
        glfwTerminate();
        if(restart)
            InitProgram();
    }
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
            if (UIMode == 2 && Mouse.getButtonDown(GLFW_MOUSE_BUTTON_1) && mousePixelPosition[1] < 32) {
                if (selection.selectionStage == 3) {
                    selection.ResetSelection();
                    selection.selectionStage = 0;
                }
                if (selection.selectionStage == 1) {
                    selection.xpoint2 = mousePixelPosition[0];
                    selection.ypoint2 = mousePixelPosition[1];
                    selection.selectionStage = 2;
                    selection.CompleteSelection(_pixelArray);
                }
                if (selection.selectionStage == 0) {
                    selection.xpoint1 = mousePixelPosition[0];
                    selection.ypoint1 = mousePixelPosition[1];
                    selection.selectionStage = 1;
                }
            }
        }
        //Draw Mode
        if(mousePixelPosition[1] < 32 && UIMode == 0) {
            if(Mouse.getButtonDown(GLFW_MOUSE_BUTTON_1)) {
                PixelChange newPixel = new PixelChange();
                undoBuffer.addFirst(newPixel);
            }

            if(Mouse.getButton(GLFW_MOUSE_BUTTON_1)) {
                undoBuffer.getFirst().AddPixel(mousePixelPosition[0], mousePixelPosition[1], _pixelArray[mousePixelPosition[0]][mousePixelPosition[1]]);
                _pixelArray[mousePixelPosition[0]][mousePixelPosition[1]] = selectedColorIndex;
            }
        }

        if(mousePixelPosition[1] > 32 && Mouse.getButton(GLFW_MOUSE_BUTTON_1)){
            selectedColorIndex = mousePixelPosition[0] / 2;
        }


        //Move Selection
        if(UIMode == 2 ){
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
            if(Input.getKeyDown(GLFW_KEY_Z)){
                int[][] flippedPixelArray = new int[selection.xpoint2 - selection.xpoint1 + 1][selection.ypoint2 - selection.ypoint1 + 1];
                for(int x = 0; x <= selection.xpoint2 - selection.xpoint1; x++){
                    for(int y = 0; y <= selection.ypoint2 - selection.ypoint1; y++){
                        flippedPixelArray[(selection.xpoint2 - selection.xpoint1) - x][y] = selection.pixels[x][y];
                    }
                }
                selection.pixels = flippedPixelArray;
            }

            if(Input.getKeyDown(GLFW_KEY_X)){
                int[][] flippedPixelArray = new int[selection.xpoint2 - selection.xpoint1 + 1][selection.ypoint2 - selection.ypoint1 + 1];
                for(int x = 0; x <= selection.xpoint2 - selection.xpoint1; x++){
                    for(int y = 0; y <= selection.ypoint2 - selection.ypoint1; y++){
                        flippedPixelArray[x][(selection.ypoint2 - selection.ypoint1) - y] = selection.pixels[x][y];
                    }
                }
                selection.pixels = flippedPixelArray;
            }

            if(Input.getKeyDown(GLFW_KEY_ENTER)){
                //confirm paste
                PixelChange newPixel = new PixelChange();
                undoBuffer.addFirst(newPixel);

                for(int x = selection.xpoint1; x <= selection.xpoint2; x++){
                    for(int y = selection.ypoint1; y <= selection.ypoint2; y++){
                        if((x < 0 || x > 31) || (y < 0 || y > 31))
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

        //Edit Color Mode
        if(Input.getKeyDown(GLFW_KEY_E)){
            UIMode = (UIMode == 1 ? 0 : 1);
            selection.ResetSelection();
            selection.selectionStage = 0;
        }

        //Selection Mode
        if(Input.getKeyDown(GLFW_KEY_LEFT_SHIFT)){
            UIMode = (UIMode == 2 ? 0 : 2);
        }

        //Draw
        if(Input.getKeyDown(GLFW_KEY_ESCAPE)){
            UIMode = 0;
            selection.ResetSelection();
            selection.selectionStage = 0;
        }

        //Change Scale
        if(Input.getKeyDown(GLFW_KEY_EQUAL)){
            _windowResolutionScale++;
            restart = true;
            glfwDestroyWindow(_window);
        }
        if(Input.getKeyDown(GLFW_KEY_MINUS)){
            _windowResolutionScale--;
            restart = true;
            glfwDestroyWindow(_window);
        }

        //change Palette Right
        if(Input.getKeyDown(GLFW_KEY_RIGHT_BRACKET)){
            paletteIndex++;
            if(paletteIndex > 3)
                paletteIndex = 0;
            loadPalette(paletteIndex);
        }
        //change Palette Left
        if(Input.getKeyDown(GLFW_KEY_LEFT_BRACKET)){
            paletteIndex--;
            if(paletteIndex < 0)
                paletteIndex = 3;
            loadPalette(paletteIndex);
        }

        //Undo
        if(Input.getKey(GLFW_KEY_LEFT_CONTROL) && Input.getKeyDown(GLFW_KEY_Z) && undoBuffer.size() > 0){
            PixelChange pc = undoBuffer.getFirst();
            for(PixelChange.Pixel2i i : pc.pixelChanges){
                _pixelArray[i.x][i.y] = i.colorIndex;
            }
            undoBuffer.removeFirst();
        }
        //Fill
        if(Input.getKey(GLFW_KEY_LEFT_CONTROL) && Input.getKeyDown(GLFW_KEY_F)){
            PixelChange newPixel = new PixelChange();
            undoBuffer.addFirst(newPixel);
            for(int x = 0; x < 32; x++){
                for(int y = 0; y < 32; y++){
                    undoBuffer.getFirst().AddPixel(x,y, _pixelArray[x][y]);
                    _pixelArray[x][y] = selectedColorIndex;
                }
            }
        }

        //Get Manual Hex
        if(UIMode == 1 && Input.getKeyDown(GLFW_KEY_H)){
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
            }catch(NumberFormatException e){
                System.out.println("Hex value not formatted correctly");
            }
            System.out.println(redString + " " + greenString + " " + blueString);
            palette[selectedColorIndex] = new Color(red, green, blue);
        }


        //Open File
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
        //Save File
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
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 512 * windowScale, (512 * windowScale) + (32 * windowScale), 0, 1, -1);
        glMatrixMode(GL_PROJECTION);

        return window;
    }

    public ApeApplication(){
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

    public static void main(String[] args){
        ApeApplication apeApp = new ApeApplication();
    }


    public Selection GetSelection(){
        return selection;
    }
    public Color[] GetPalette(){
        return palette;
    }
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
