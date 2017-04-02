package ewewukek.swar;

import java.util.HashMap;
import java.util.Map;

public class Glyph {
    public final int x;
    public final int y;
    public final int w;
    public final int h;

    private static final Map<Character, Glyph> glyphs = new HashMap<Character, Glyph>();

    private Glyph(int x, int y, int w, int h) {
        this.x = x;
        this.y = 255 - y - h;
        this.w = w;
        this.h = h;
    }

    public static Glyph get(char c) {
        return glyphs.get(Character.toUpperCase(c));
    }

    static {
        glyphs.put('A', new Glyph(0, 0, 32, 42));
        glyphs.put('B', new Glyph(32, 0, 32, 42));
        glyphs.put('C', new Glyph(64, 0, 32, 42));
        glyphs.put('D', new Glyph(96, 0, 32, 42));
        glyphs.put('E', new Glyph(128, 0, 32, 42));
        glyphs.put('F', new Glyph(160, 0, 32, 42));
        glyphs.put('G', new Glyph(192, 0, 32, 42));
        glyphs.put('H', new Glyph(0, 42, 32, 42));
        glyphs.put('I', new Glyph(32, 42, 12, 42));
        glyphs.put('J', new Glyph(44, 42, 32, 42));
        glyphs.put('K', new Glyph(76, 42, 32, 42));
        glyphs.put('L', new Glyph(108, 42, 32, 42));
        glyphs.put('M', new Glyph(140, 42, 32, 42));
        glyphs.put('N', new Glyph(172, 42, 32, 42));
        glyphs.put('O', new Glyph(204, 42, 32, 42));
        glyphs.put('P', new Glyph(0, 84, 32, 42));
        glyphs.put('Q', new Glyph(32, 84, 32, 42));
        glyphs.put('R', new Glyph(64, 84, 32, 42));
        glyphs.put('S', new Glyph(96, 84, 32, 42));
        glyphs.put('T', new Glyph(128, 84, 32, 42));
        glyphs.put('U', new Glyph(160, 84, 32, 42));
        glyphs.put('V', new Glyph(192, 84, 32, 42));
        glyphs.put('W', new Glyph(0, 126, 32, 42));
        glyphs.put('X', new Glyph(32, 126, 32, 42));
        glyphs.put('Y', new Glyph(64, 126, 32, 42));
        glyphs.put('Z', new Glyph(96, 126, 32, 42));
        glyphs.put('1', new Glyph(128, 126, 22, 42));
        glyphs.put('2', new Glyph(150, 126, 32, 42));
        glyphs.put('3', new Glyph(182, 126, 32, 42));
        glyphs.put('4', new Glyph(214, 126, 32, 42));
        glyphs.put('5', new Glyph(0, 168, 32, 42));
        glyphs.put('6', new Glyph(32, 168, 32, 42));
        glyphs.put('7', new Glyph(64, 168, 32, 42));
        glyphs.put('8', new Glyph(96, 168, 32, 42));
        glyphs.put('9', new Glyph(128, 168, 32, 42));
        glyphs.put('0', new Glyph(160, 168, 32, 42));
    }
}