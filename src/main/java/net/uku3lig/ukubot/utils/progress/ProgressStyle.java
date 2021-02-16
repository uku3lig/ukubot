package net.uku3lig.ukubot.utils.progress;

public enum ProgressStyle {
    COLORFUL_UNICODE_BLOCK("\r", "\u001b[33m│", "│\u001b[0m", '█', ' ', " ▌"),

    /** Use Unicode block characters to draw the progress bar. */
    UNICODE_BLOCK("\r", "│", "│", '█', ' ', " ▌"),

    /** Use only ASCII characters to draw the progress bar. */
    ASCII("\r", "[", "]", '=', ' ', ">");

    String refreshPrompt;
    String leftBracket;
    String rightBracket;
    char block;
    char space;
    String fractionSymbols;

    ProgressStyle(String refreshPrompt, String leftBracket, String rightBracket, char block, char space, String fractionSymbols) {
        this.refreshPrompt = refreshPrompt;
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;
        this.block = block;
        this.space = space;
        this.fractionSymbols = fractionSymbols;
    }
}
