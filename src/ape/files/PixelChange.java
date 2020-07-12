package ape.files;

import java.util.LinkedList;

public class PixelChange {
    public LinkedList<Pixel2i> pixelChanges = new LinkedList<Pixel2i>();

    public void AddPixel(int x, int y, int colorIndex){
        pixelChanges.addFirst(new Pixel2i(x, y, colorIndex));
    }


    public class Pixel2i{
        public int x;
        public int y;
        public int colorIndex;

        public Pixel2i(int x, int y, int colorIndex){
            this.x = x;
            this.y = y;
            this.colorIndex = colorIndex;
        }
    }
}
