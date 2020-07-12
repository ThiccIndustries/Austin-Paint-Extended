package ape.files;

import java.awt.*;
import java.io.*;
import java.lang.*;

public class FileManager {

    public void saveFileFromImage(String filePath, APFile file){
        File fileToSave = new File(filePath);
        FileOutputStream fos = null;

        //convert pixel array into byte array
        byte[] rawPixelData = new byte[576];
        StringBuilder sb = new StringBuilder();
        sb.append("41555354494E5041494E540056322E30"); // "AUSTIN.PAINT.v2.0"
        for(int i = 0; i < 16; i++){
            int r = file.palette[i].getRed();
            int g = file.palette[i].getGreen();
            int b = file.palette[i].getBlue();
            sb.append(String.format("%02X", r) + "" + String.format("%02X", g) + "" + String.format("%02X", b));
        }

        for(int y = 0; y < 32; y++){
            for(int x = 0; x < 16; x++){
                sb.append(Integer.toHexString(file.pixelArray[x * 2][y]) + Integer.toHexString(file.pixelArray[(x * 2) + 1][y]));
            }
        }
        String hexDump = sb.toString();
        for(int i = 0; i < (rawPixelData.length * 2); i+=2){
            rawPixelData[(i / 2)] = (byte)((Character.digit(hexDump.charAt(i), 16) << 4)
                                        + Character.digit(hexDump.charAt(i + 1), 16));
        }
        try {
            fos = new FileOutputStream(fileToSave);
            fos.write(rawPixelData);
        }catch(IOException e){
            System.err.print("Unknown IO Error.");
        }
    }

    public APFile loadPixelArrayFromFile(String filePath){
        APFile fileLoaded = new APFile();
        fileLoaded.pixelArray = new int[32][32];
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
        //import color palette

        for(int i = 0; i < (16 * 3); i+=3){
            fileLoaded.palette[i / 3] = new Color(rawPixelData[i + 16] & 0xff, rawPixelData[i + 16 + 1] & 0xff, rawPixelData[i + 16 +  2] & 0xff);
        }

        //convert raw bytes into int array
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

