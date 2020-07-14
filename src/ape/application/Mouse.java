package ape.application;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.util.Arrays;

public class Mouse extends GLFWMouseButtonCallback {
    public static boolean[] buttons = new boolean[16];
    public static int[] actions = new int[16];

    @Override
    public void invoke(long window, int button, int action, int mods){
        buttons[button] = action != GLFW_RELEASE;
        actions[button] = action;
    }

    public static boolean getButton(int button){
        return buttons[button];
    }

    public static boolean getButtonDown(int button){
        return actions[button] == GLFW_PRESS;
    }

    public static boolean getButtonUp(int button){
        return actions[button] == GLFW_RELEASE;
    }

    public static void update(){
        /**I refuse this is the best way to implement a getButtonDown and getButtonUp event but the all mighty stackoverflow.com has spoken**/
        Arrays.fill(actions, -1);
    }
}
