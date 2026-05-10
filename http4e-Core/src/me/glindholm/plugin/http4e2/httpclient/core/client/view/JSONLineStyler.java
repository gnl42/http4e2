package me.glindholm.plugin.http4e2.httpclient.core.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

public class JSONLineStyler implements LineStyleListener {

    JSONcanner scanner = new JSONcanner();

    int[] tokenColors;

    Color[] colors;

    public static final int EOF = -1;

    public static final int EOL = 10;

    public static final int WORD = 0;

    public static final int WHITE = 1;

    public static final int KEY = 2;

    public static final int COMMENT = 3;

    public static final int STRING = 5;

    public static final int OTHER = 6;

    public static final int NUMBER = 7;

    public static final int MAXIMUM_TOKEN = 8;

    public JSONLineStyler() {
        initializeColors();
        scanner = new JSONcanner();
    }

    int[] tokenColorsDark;

    Color getColor(final int type, final boolean dark) {
        if (type < 0 || type >= tokenColors.length) {
            return null;
        }
        return dark ? colors[tokenColorsDark[type]] : colors[tokenColors[type]];
    }

    // Light theme colors (indices 0-3)
    // Dark theme colors (indices 4-7)
    void initializeColors() {
        colors = new Color[] {
                // Light theme (index 0-3)
                ResourceUtils.getColor(Styles.BLACK_RGB_TEXT),   // 0: default/word - black
                ResourceUtils.getColor(Styles.KEY),              // 1: key/string - dark blue
                ResourceUtils.getColor(Styles.KEY_GREEN),        // 2: comment/other - dark green
                ResourceUtils.getColor(Styles.KEY_GREEN),        // 3: unused (green)
                // Dark theme (index 4-7)
                ResourceUtils.getColor(new org.eclipse.swt.graphics.RGB(220, 220, 220)),  // 4: default/word - light grey
                ResourceUtils.getColor(new org.eclipse.swt.graphics.RGB(156, 220, 254)),  // 5: key/string - light blue
                ResourceUtils.getColor(new org.eclipse.swt.graphics.RGB(206, 145, 120)),  // 6: string values - orange
                ResourceUtils.getColor(new org.eclipse.swt.graphics.RGB(181, 206, 168))   // 7: numbers/comment - light green
        };

        tokenColors = new int[MAXIMUM_TOKEN];
        tokenColors[WORD] = 1;
        tokenColors[WHITE] = 2;
        tokenColors[KEY] = 0;
        tokenColors[COMMENT] = 2;
        tokenColors[STRING] = 1;
        tokenColors[OTHER] = 2;
        tokenColors[NUMBER] = 1;

        tokenColorsDark = new int[MAXIMUM_TOKEN];
        tokenColorsDark[WORD] = 4;    // light grey
        tokenColorsDark[WHITE] = 4;   // light grey
        tokenColorsDark[KEY] = 5;     // light blue  (quoted keys)
        tokenColorsDark[COMMENT] = 6; // orange
        tokenColorsDark[STRING] = 6;  // orange      (string values after :)
        tokenColorsDark[OTHER] = 4;   // light grey
        tokenColorsDark[NUMBER] = 7;  // light green
    }

    private boolean isDarkBackground(final Color bg) {
        if (bg == null) {
            return false;
        }
        // perceived luminance
        final double lum = 0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue();
        return lum < 128;
    }

    // void disposeColors(){
    // for (int i = 0; i < colors.length; i++) {
    // colors[i].dispose();
    // }
    // }

    /**
     * Event.detail line start offset (input) Event.text line text (input) LineStyleEvent.styles
     * Enumeration of StyleRanges, need to be in order. (output) LineStyleEvent.background line
     * background color (output)
     */
    @Override
    public void lineGetStyle(final LineStyleEvent event) {
        final List<StyleRange> styles = new ArrayList<>();
        int token;
        StyleRange lastStyle;
        // If the line is part of a block comment, create one style for the
        // entire line.
        // if (inBlockComment(event.lineOffset, event.lineOffset
        // + event.lineText.length())) {
        // styles.addElement(new StyleRange(event.lineOffset, event.lineText
        // .length(), getColor(COMMENT), null));
        // event.styles = new StyleRange[styles.size()];
        // styles.copyInto(event.styles);
        // return;
        // }
        final Color defaultFgColor = ((Control) event.widget).getForeground();
        final Color bgColor = ((Control) event.widget).getBackground();
        final boolean dark = isDarkBackground(bgColor);
        scanner.setRange(event.lineText);
        token = scanner.nextToken();
        while (token != EOF) {
            if (token == OTHER) {
                // do nothing for non-colored tokens
            } else if (token != WHITE) {
                final Color color = getColor(token, dark);
                // Only create a style if the token color is different than the
                // widget's default foreground color and the token's style is
                // not
                // bold. Keywords are bolded.
                if (!color.equals(defaultFgColor) || token == KEY) {
                    final StyleRange style = new StyleRange(scanner.getStartOffset() + event.lineOffset, scanner.getLength(), color, null);
                    if (token == KEY) {
                        style.fontStyle = SWT.BOLD;
                    }
                    if (styles.isEmpty()) {
                        styles.add(style);
                    } else {
                        // Merge similar styles. Doing so will improve
                        // performance.
                        lastStyle = styles.get(styles.size() - 1);
                        if (lastStyle.similarTo(style) && lastStyle.start + lastStyle.length == style.start) {
                            lastStyle.length += style.length;
                        } else {
                            styles.add(style);
                        }
                    }
                }
            } else if (!styles.isEmpty() && (lastStyle = styles.get(styles.size() - 1)).fontStyle == SWT.BOLD) {
                final int start = scanner.getStartOffset() + event.lineOffset;
                lastStyle = styles.get(styles.size() - 1);
                // A font style of SWT.BOLD implies that the last style
                // represents a java keyword.
                if (lastStyle.start + lastStyle.length == start) {
                    // Have the white space take on the style before it to
                    // minimize the number of style ranges created and the
                    // number of font style changes during rendering.
                    lastStyle.length += scanner.getLength();
                }
            }
            token = scanner.nextToken();
        }
        event.styles = new StyleRange[styles.size()];
        styles.toArray(event.styles);
    }

    /**
     * A simple fuzzy scanner for Java
     */
    public class JSONcanner {

        protected HashMap<String, Integer> fgKeys = null;

        protected StringBuilder fBuffer = new StringBuilder();

        protected String fDoc;

        protected int fPos;

        protected int fEnd;

        protected int fStartToken;

        protected boolean fEofSeen = false;

        private final String[] fgKeywords = { "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double",
                "else", "extends", "false", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long",
                "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this",
                "throw", "throws", "transient", "true", "try", "void", "volatile", "while", "undefined" };

        public JSONcanner() {
            initialize();
        }

        /**
         * Returns the ending location of the current token in the document.
         */
        public final int getLength() {
            return fPos - fStartToken;
        }

        /**
         * Initialize the lookup table.
         */
        void initialize() {
            fgKeys = new HashMap<>();
            final Integer k = KEY;
            for (final String fgKeyword : fgKeywords) {
                fgKeys.put(fgKeyword, k);
            }
        }

        /**
         * Returns the starting location of the current token in the document.
         */
        public final int getStartOffset() {
            return fStartToken;
        }

        /**
         * Returns the next lexical token in the document.
         */
        public int nextToken() {
            int c;
            fStartToken = fPos;
            while (true) {
                switch (c = read()) {

                case EOF:
                    return EOF;

                case '"': // key
                    while (true) {
                        c = read();
                        if (c == '"') {
                            return KEY;
                        } else if (c == EOF) {
                            unread(c);
                            return KEY;
                        } else if (c == '\\') {
                            c = read();
                            break;
                        }
                    }

                    // case '[': // value
                    // while (true) {
                    // c = read();
                    // if (c == ']') {
                    // return COMMENT;
                    // }
                    // else if (c == EOF) {
                    // unread(c);
                    // return COMMENT;
                    // }
                    // else if (c == '\\') {
                    // c = read();
                    // break;
                    // }
                    // }

                case ':': // value
                    while (true) {
                        c = read();
                        // if (c == ',') {
                        // return STRING;
                        // } else
                        if (c == EOF) {
                            unread(c);
                            return STRING;
                        } else if (c == '\\') {
                            c = read();
                            break;
                        }
                    }

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    do {
                        c = read();
                    } while (Character.isDigit((char) c));
                    unread(c);
                    return NUMBER;

                default:
                    if (Character.isWhitespace((char) c)) {
                        do {
                            c = read();
                        } while (Character.isWhitespace((char) c));
                        unread(c);
                        return WHITE;
                    }
                    if (Character.isJavaIdentifierStart((char) c)) {
                        fBuffer.setLength(0);
                        do {
                            fBuffer.append((char) c);
                            c = read();
                        } while (Character.isJavaIdentifierPart((char) c));
                        unread(c);
                        final Integer i = fgKeys.get(fBuffer.toString());
                        if (i != null) {
                            return i;
                        }
                        return WORD;
                    }
                    return OTHER;
                }
            }
        }

        /**
         * Returns next character.
         */
        protected int read() {
            if (fPos <= fEnd) {
                return fDoc.charAt(fPos++);
            }
            return EOF;
        }

        public void setRange(final String text) {
            fDoc = text;
            fPos = 0;
            fEnd = fDoc.length() - 1;
        }

        protected void unread(final int c) {
            if (c != EOF) {
                fPos--;
            }
        }
    }

}