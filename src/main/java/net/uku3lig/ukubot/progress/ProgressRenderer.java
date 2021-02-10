package net.uku3lig.ukubot.progress;

public class ProgressRenderer {
    public static String render(ProgressStyle style, double progress, int bodyLength) {
        StringBuilder builder = new StringBuilder(style.leftBracket)
                .append(repeat(style.block, intPart(progress, bodyLength)));
        if (intPart(progress, bodyLength) < bodyLength) {
            builder.append(style.fractionSymbols.charAt(fractionalPart(progress, bodyLength, style)))
                    .append(repeat(style.space, bodyLength - intPart(progress, bodyLength)));
        }
        return builder.append(style.rightBracket).toString();
    }

    private static int intPart(double progress, int length) {
        return (int)(progress * length);
    }

    private static int fractionalPart(double progress, int length, ProgressStyle style) {
        double p = progress * length;
        double fraction = (p - Math.floor(p)) * style.fractionSymbols.length();
        return (int) Math.floor(fraction);
    }

    private static String repeat(char c, int n) {
        if (n <= 0) return "";
        char[] s = new char[n];
        for (int i = 0; i < n; i++) s[i] = c;
        return new String(s);
    }
}
