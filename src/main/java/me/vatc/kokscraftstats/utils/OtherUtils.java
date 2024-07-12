package me.vatc.kokscraftstats.utils;

import javafx.scene.paint.Color;

public class OtherUtils {

    public static String toHexString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

}
