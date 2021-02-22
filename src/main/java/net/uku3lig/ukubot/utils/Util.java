package net.uku3lig.ukubot.utils;

import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Util {
    public static String formatNum(double number) {
        return new DecimalFormat("#.##").format(number <= 1000 ? number : number / 1000)
                + (number <= 1000 ? "" : "k");
    }

    public static String humanReadableByteCount(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public static String skipOneArgAndJoin(String[] args) {
        return Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
    }
}
