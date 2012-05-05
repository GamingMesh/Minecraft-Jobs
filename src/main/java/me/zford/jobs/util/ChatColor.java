package me.zford.jobs.util;

import java.util.HashMap;
import java.util.Map;

public enum ChatColor {
    BLACK('0', 0),
    DARK_BLUE('1', 1),
    DARK_GREEN('2', 2),
    DARK_AQUA('3', 3),
    DARK_RED('4', 4),
    DARK_PURPLE('5', 5),
    GOLD('6', 6),
    GRAY('7', 7),
    DARK_GRAY('8', 8),
    BLUE('9', 9),
    GREEN('a', 10),
    AQUA('b', 11),
    RED('c', 12),
    LIGHT_PURPLE('d', 13),
    YELLOW('e', 14),
    WHITE('f', 15);
    
    private static final char COLOR_CHAR = '\u00A7';
    private final char code;
    private final int intCode;
    private final String toString;
    private final static Map<Integer, ChatColor> intMap = new HashMap<Integer, ChatColor>();
    private final static Map<Character, ChatColor> charMap = new HashMap<Character, ChatColor>();
    private final static Map<String, ChatColor> stringMap = new HashMap<String, ChatColor>();
    
    private ChatColor(char code, int intCode) {
        this.code = code;
        this.intCode = intCode;
        this.toString = new String(new char[] { COLOR_CHAR, code });
    }
    
    public char getChar() {
        return code;
    }
    
    @Override
    public String toString() {
        return toString;
    }
    
    public static ChatColor matchColor(char code) {
        return charMap.get(code);
    }
    
    public static ChatColor matchColor(int code) {
        return intMap.get(code);
    }
    
    public static ChatColor matchColor(String name) {
        return stringMap.get(name.toLowerCase());
    }
    
    static {
        for (ChatColor color : values()) {
            intMap.put(color.intCode, color);
            charMap.put(color.code, color);
            stringMap.put(color.name().toLowerCase(), color);
        }
    }
}
