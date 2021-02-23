package net.uku3lig.ukubot.utils;

import java.io.*;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Util {
    public static String formatNum(double number) {
        return new DecimalFormat("#.##").format(number <= 1000 ? number : number / 1000)
                + (number <= 1000 ? "" : "k");
    }

    public static String spaces(long number) {
        DecimalFormatSymbols space = DecimalFormatSymbols.getInstance();
        space.setGroupingSeparator(' ');
        return new DecimalFormat("#,###", space).format(number);
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

    public static long countLinesNew(File f) {
        try (InputStream is = new BufferedInputStream(new FileInputStream(f))){
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            long count = 0;
            while (readChars == 1024) {
                for (int i=0; i<1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                for (int i=0; i<readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        } catch (IOException e) {
            return 0;
        }
    }
}
