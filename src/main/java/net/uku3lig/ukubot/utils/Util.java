package net.uku3lig.ukubot.utils;

import java.text.DecimalFormat;

public class Util {
    public static String formatNum(double number) {
        return new DecimalFormat("#.##").format(number <= 1000 ? number : number / 1000)
                + (number <= 1000 ? "" : "k");
    }
}
