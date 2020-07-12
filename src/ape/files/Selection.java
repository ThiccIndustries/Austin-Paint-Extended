package ape.files;

public class Selection {
    public int xpoint1 = 5;
    public int xpoint2 = 15;

    public int ypoint1 = 5;
    public int ypoint2 = 15;

    public int[][] pixels;
    public int selectionStage = 0;

    public void CompleteSelection(int[][] _pixelArray){
        System.out.println("Attempting Selection: (" + xpoint1 + "," + ypoint1 + ") (" + xpoint2 + "," + ypoint2 + ")");
        if(selectionStage == 2) {
            if(xpoint1 > xpoint2 || ypoint1 > ypoint2){
                System.out.println("Invalid Selection");
                selectionStage = 0;
                ResetSelection();
            }else {
                pixels = new int[(xpoint2 - xpoint1) + 1][(ypoint2 - ypoint1) + 1];
                for (int x = xpoint1; x <= xpoint2; x++) {
                    for (int y = ypoint1; y <= ypoint2; y++) {
                        pixels[x - xpoint1][y - ypoint1] = _pixelArray[x][y];
                    }
                }
                selectionStage = 3;
            }
        }
    }

    public void ResetSelection(){
        xpoint1 = 0;
        xpoint2 = 0;
        ypoint1 = 0;
        ypoint2 = 0;

        pixels = null;
    }
}
