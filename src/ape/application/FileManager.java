package ape.application;

import ape.files.APFile;
import ape.files.Palettes;

import java.awt.*;
import java.io.*;
import java.lang.*;

public class FileManager {

    //Save the file
    public void saveFileFromImage(String filePath, APFile file){
        File fileToSave = new File(filePath);
        FileOutputStream fos = null;

        //Convert from various variables into a new array of Bytes
        byte[] rawPixelData = new byte[576];

        /**Doing this with a hex string is EXTREMELY dumb, but I dont care**/
        StringBuilder sb = new StringBuilder();

        /**Really austin?**/
        sb.append("41555354494E5041494E540056322E30"); // "AUSTIN.PAINT.v2.0"

        //Add palette information
        for(int i = 0; i < 16; i++){
            int r = file.palette[i].getRed();
            int g = file.palette[i].getGreen();
            int b = file.palette[i].getBlue();
            sb.append(String.format("%02X", r) + "" + String.format("%02X", g) + "" + String.format("%02X", b));
        }

        //Add pixel information,

        /**you may ask why im not using %02X for this, like I am for everything else, but that doesn't work, and no, i dont know why**/
        for(int y = 0; y < 32; y++){
            for(int x = 0; x < 16; x++){
                sb.append(Integer.toHexString(file.pixelArray[x * 2][y]) + Integer.toHexString(file.pixelArray[(x * 2) + 1][y]));
            }
        }

        /**Why does StringBuilder even need a function to do this, why can i not just cast it**/
        String hexDump = sb.toString();

        //Convert pixel array from one byte per pixel to 4 bits per pixel
        for(int i = 0; i < (rawPixelData.length * 2); i+=2){
            rawPixelData[(i / 2)] = (byte)((Character.digit(hexDump.charAt(i), 16) << 4)
                    + Character.digit(hexDump.charAt(i + 1), 16));
        }
        try {
            //Write file
            fos = new FileOutputStream(fileToSave);
            fos.write(rawPixelData);
        }catch(IOException e){
            System.err.print("Unknown IO Error.");
        }
    }

    //Load a saved file
    public APFile loadPixelArrayFromFile(String filePath){
        APFile fileLoaded = new APFile();
        fileLoaded.pixelArray = new int[32][32];

        //Create a local copy of the palette to prevent destructive changes
        /**yes, this causes a memory leak**/
        System.arraycopy(Palettes.Default, 0, fileLoaded.palette, 0, 16);

        File file = new File(filePath);
        FileInputStream fis = null;
        byte[] rawPixelData = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(rawPixelData);
            fis.close();
        } catch (FileNotFoundException e) {
            System.err.print("File did not exist.");
            return fileLoaded;
        } catch (IOException e) {
            System.err.print("Unknown IO Error.");
        }

        //Get Color Palette Information
        for(int i = 0; i < (16 * 3); i+=3){
            fileLoaded.palette[i / 3] = new Color(rawPixelData[i + 16] & 0xff, rawPixelData[i + 16 + 1] & 0xff, rawPixelData[i + 16 +  2] & 0xff);
        }

        /**Converting from Byte -> Char -> Int is probably the dumbest thing ive ever done but it works**/
        for(int y = 0; y < 32; y++){
            for(int x = 0; x < 16; x++){
                Byte currentByte = rawPixelData[x + (y * 16) + 64];
                char firstPixelHexChar = String.format("%02x", currentByte).charAt(0);
                char secondPixelHexChar = String.format("%02x", currentByte).charAt(1);
                fileLoaded.pixelArray[x * 2][y] = Character.digit(firstPixelHexChar, 16);
                fileLoaded.pixelArray[x * 2 + 1][y] = Character.digit(secondPixelHexChar, 16);
            }
        }
        return fileLoaded;
    }
}

