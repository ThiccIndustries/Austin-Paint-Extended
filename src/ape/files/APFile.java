package ape.files;

import java.awt.*;

public class APFile {
    public int colorIndex = -1; //negative 1 = from file
    public Color[] palette = new Color[16];
    public int[][] pixelArray = new int[32][32];
    public int backgroundColor;
}
