package ape.application;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Input extends GLFWKeyCallback {
    public static boolean[] keys = new boolean[GLFW_KEY_LAST + 1];
    public static int[] actions = new int[GLFW_KEY_LAST + 1];

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods){
        keys[key] = action != GLFW_RELEASE;
        actions[key] = action;
    }

    public static boolean getKey(int keycode){
        return keys[keycode];
    }

    public static boolean getKeyDown(int keycode){
        return actions[keycode] == GLFW_PRESS;
    }

    public static boolean getKeyUp(int keycode){
        return actions[keycode] == GLFW_RELEASE;
    }

    protected static void update()
    {
        for (int i = 0; i < keys.length; i++)
        {
            actions[i] = -1;
        }
    }
}
